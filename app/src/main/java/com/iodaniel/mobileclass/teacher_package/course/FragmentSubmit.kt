package com.iodaniel.mobileclass.teacher_package.course

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.databinding.FragmentSubmitBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.repository.CourseUploadRepo
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface
import com.iodaniel.mobileclass.util.BackgroundHelper
import com.iodaniel.mobileclass.viewModel.SubmitCourseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentSubmit : Fragment(), BackgroundHelper, View.OnClickListener, ClassMaterialUploadInterface.ProgressBarController {
    private lateinit var binding: FragmentSubmitBinding
    private var pmeRef = FirebaseDatabase.getInstance().reference
    private var connectionRef = FirebaseDatabase.getInstance().reference
    private val listener = Listener()
    private val pMEAdapter = PMEAdapter()
    private val submitCourseViewModel = SubmitCourseViewModel()
    private val connectionListener = ConnectionListener()
    private val pmeList: ArrayList<PlanModulesExercise> = arrayListOf()
    private val dialog by lazy { Dialog(requireContext()) }
    private var courseCardData: CourseCardData? = null
    private lateinit var courseUploadRepo: CourseUploadRepo
    private var courseCardDataJson = ""
    private var bundle = Bundle()
    private var fetchedData = false
    private var networkButEmpty = false
    private var pDialog: Dialog? = null
    private var allMedia = 0
    private var modulesWithContent = 0
    private val scope = CoroutineScope(Dispatchers.IO)

    inner class Listener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                pMEAdapter.notifyItemRangeRemoved(0, pmeList.size)
                pmeList.clear()
                for ((index, i) in snapshot.children.withIndex()) {
                    val value = Gson().toJson(i.value)
                    val data: PlanModulesExercise = Gson().fromJson(value, PlanModulesExercise::class.java)
                    pmeList.add(data)
                    pMEAdapter.notifyItemInserted(index)
                }
                pMEAdapter.dataset = pmeList

                for (exe in pmeList) {
                    allMedia += exe.modules.uris.size
                }
                for (i in pmeList) {
                    if (i.modules.uris.isNotEmpty() || i.modules.content != "") modulesWithContent += 1
                }
                //submitCourseViewModel.setExercises()
                submitCourseViewModel.setPlans(pmeList.size.toString())
                submitCourseViewModel.setMedias(allMedia.toString())
                submitCourseViewModel.setModules(modulesWithContent.toString())
                fetchedData = true
                this@FragmentSubmit.notEmpty()
            }
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }

    inner class ConnectionListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (!fetchedData) this@FragmentSubmit.empty() else this@FragmentSubmit.notEmpty()
            networkButEmpty = true
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSubmitBinding.inflate(inflater, container, false)
        pDialog = Util.progressDialog("Getting data", requireContext(), requireActivity())
        pDialog?.show()
        courseUploadRepo = CourseUploadRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)
        courseCardDataJson = requireArguments().getString(getString(R.string.manage_course_data_intent))!!
        courseCardData = Gson().fromJson(courseCardDataJson, CourseCardData::class.java)
        requireActivity().setActionBar(binding.submitToolbar)
        requireActivity().actionBar!!.title = "Summary"
        binding.submitButton.setOnClickListener(this)
        pmeRef = pmeRef.child(getString(R.string.pme_ref)).child(FirebaseAuth.getInstance().currentUser!!.uid).child(courseCardData!!.courseCode)
        bundle.putString(getString(R.string.manage_course_data_intent), courseCardDataJson)

        scope.launch {
            delay(10_000)
            if (fetchedData) this@FragmentSubmit.notEmpty() else this@FragmentSubmit.noInternet()
        }
        submitCourseViewModel.noPlans.observe(viewLifecycleOwner) { binding.submitPlansCount.text = it }
        submitCourseViewModel.noModules.observe(viewLifecycleOwner) { binding.submitModulesCount.text = it }
        submitCourseViewModel.noExercises.observe(viewLifecycleOwner) { binding.submitExerciseCount.text = it }
        submitCourseViewModel.noMedias.observe(viewLifecycleOwner) { binding.submitMediaCount.text = it }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pmeRef.addValueEventListener(listener)
        scope.launch {
            delay(2_000)
            connectionRef.addValueEventListener(connectionListener)
        }
        pMEAdapter.dataset = pmeList
        pMEAdapter.activity = requireActivity()
        pMEAdapter.courseCardDataJson = courseCardDataJson
        binding.submitRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.submitRv.adapter = pMEAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        pmeRef.removeEventListener(listener)
        connectionRef.removeEventListener(connectionListener)
        pDialog?.dismiss()
    }

    override fun empty() {
        binding.submitEmptyRoot.visibility = View.VISIBLE
        binding.submitPlansDataRoot.visibility = View.GONE
        binding.submitNoNetworkRoot.visibility = View.GONE
        pDialog?.dismiss()
    }

    override fun notEmpty() {
        binding.submitPlansDataRoot.visibility = View.VISIBLE
        binding.submitEmptyRoot.visibility = View.GONE
        binding.submitNoNetworkRoot.visibility = View.GONE
        pDialog?.dismiss()
    }

    override fun noInternet() {
        binding.submitNoNetworkRoot.visibility = View.VISIBLE
        binding.submitPlansDataRoot.visibility = View.GONE
        binding.submitEmptyRoot.visibility = View.GONE
        pDialog?.dismiss()
    }

    override fun showProgressBar() {
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        val paramWrap = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.setLayout(paramWrap, paramWrap)
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun onClick(v: View?) {
        val snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        when (v?.id) {
            R.id.submit_button -> {
                if (pmeList.isEmpty()) {
                    snackBar.setText("No plans created. Create plans.").setAction("Create plan") {
                        val fragment = FragmentEditPlans()
                        fragment.arguments = bundle
                        requireActivity().supportFragmentManager.beginTransaction()
                            .addToBackStack("modules")
                            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                            .replace(R.id.edit_course_root, fragment)
                            .commit()
                    }.show()
                    return
                }
                if (modulesWithContent < pmeList.size) {
                    snackBar.setText("Lesson plans cannot be empty. Create content in empty plans and proceed").show()
                    return
                }
                snackBar.setText("You are a good boy!").show()
                courseUploadRepo.uploadCompletelySavedCourse(requireActivity(), courseCardData!!.courseCode, this)
            }
        }
    }
}

class PMEAdapter : RecyclerView.Adapter<PMEAdapter.ViewHolder>() {
    private lateinit var context: Context
    lateinit var activity: Activity
    var courseCardDataJson: String = ""
    var dataset: ArrayList<PlanModulesExercise> = arrayListOf()
    private val rvMaterialPool = RecyclerView.RecycledViewPool()
    private val rvExercisePool = RecyclerView.RecycledViewPool()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val count: TextView = itemView.findViewById(R.id.row_submit_header_count)
        val plan: TextView = itemView.findViewById(R.id.row_submit_header_text)
        val rvMaterial: RecyclerView = itemView.findViewById(R.id.row_submit_materials_rv)
        val rvExercise: RecyclerView = itemView.findViewById(R.id.row_exercise_rv)
        val noExercise: LinearLayout = itemView.findViewById(R.id.row_no_exercise_root)
        val addExerciseChip: Chip = itemView.findViewById(R.id.row_add_exercise)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_pme_submit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.addExerciseChip.setOnClickListener {
            val fragment = FragmentExercise()
            val bundle = Bundle()
            bundle.putString(context.getString(R.string.manage_course_data_intent), courseCardDataJson)
            val json = Gson().toJson(dataset)
            bundle.putInt("module_position", holder.adapterPosition)
            bundle.putString("plansAndModulesList", json)
            fragment.arguments = bundle
            (activity as AppCompatActivity).supportFragmentManager
                .beginTransaction()
                .addToBackStack("edit")
                .replace(R.id.submit_root, fragment)
                .commit()
        }
        holder.itemView.setOnClickListener{
            val fragment = FragmentEditModules()
            val bundle = Bundle()
            bundle.putString(context.getString(R.string.manage_course_data_intent), courseCardDataJson)
            val json = Gson().toJson(dataset)
            bundle.putInt("module_position", holder.adapterPosition)
            bundle.putString("plansAndModulesList", json)
            fragment.arguments = bundle
            (activity as AppCompatActivity).supportFragmentManager
                .beginTransaction()
                .addToBackStack("edit")
                .replace(R.id.submit_root, fragment)
                .commit()
        }

        holder.plan.text = datum.plan
        val count = (position + 1).toString()
        holder.count.text = count

        val lmM = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        lmM.isItemPrefetchEnabled = true
        lmM.initialPrefetchItemCount = datum.modules.uris.size
        val moduleChildAdapter = ModuleChildAdapter()
        moduleChildAdapter.dataset = datum.modules.uris
        holder.rvMaterial.setRecycledViewPool(rvMaterialPool)
        holder.rvMaterial.layoutManager = lmM
        holder.rvMaterial.adapter = moduleChildAdapter

        val lmE = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        lmE.isItemPrefetchEnabled = true
        lmE.initialPrefetchItemCount = datum.modules.uris.size
        val exerciseChildAdapter = ExerciseChildAdapter()
        //exerciseChildAdapter.dataset = datum.exercise.uris
        holder.rvExercise.setRecycledViewPool(rvExercisePool)
        holder.rvExercise.layoutManager = lmE
        holder.rvExercise.adapter = exerciseChildAdapter

        holder.noExercise.visibility = View.VISIBLE
    }

    override fun getItemCount() = dataset.size
}

class ModuleChildAdapter : RecyclerView.Adapter<ModuleChildAdapter.ViewHolder>() {
    private lateinit var context: Context
    var dataset: ArrayList<MutableMap<String, String>> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: Chip = itemView.findViewById(R.id.row_module_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_module, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.chip.text = datum["filename"]
    }

    override fun getItemCount() = dataset.size
}

class ExerciseChildAdapter : RecyclerView.Adapter<ExerciseChildAdapter.ViewHolder>() {
    private lateinit var context: Context
    var dataset: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: Chip = itemView.findViewById(R.id.row_module_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_module, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.chip.text = datum
    }

    override fun getItemCount() = dataset.size
}