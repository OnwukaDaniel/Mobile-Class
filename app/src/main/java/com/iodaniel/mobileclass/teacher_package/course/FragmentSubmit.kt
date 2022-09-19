package com.iodaniel.mobileclass.teacher_package.course

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.ExerciseData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.data_class.Question
import com.iodaniel.mobileclass.databinding.FragmentSubmitBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.liveDataClasses.ValueEventLiveData
import com.iodaniel.mobileclass.repository.CourseUploadRepo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface
import com.iodaniel.mobileclass.teacher_package.singleclass.AddExerciseFragment
import com.iodaniel.mobileclass.util.BackgroundHelper
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.viewModel.SubmitCourseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FragmentSubmit : Fragment(), BackgroundHelper, View.OnClickListener, ClassMaterialUploadInterface.ProgressBarController {
    private lateinit var binding: FragmentSubmitBinding
    private var pmeRef = FirebaseDatabase.getInstance().reference
    private val pMEAdapter = PMEAdapter()
    private val submitCourseViewModel = SubmitCourseViewModel()
    private val pmeList: ArrayList<PlanModulesExercise> = arrayListOf()
    private val dialog by lazy { Dialog(requireContext()) }
    private var courseCardData: CourseCardData? = null
    private lateinit var courseUploadRepo: CourseUploadRepo
    private lateinit var valueEventLiveData: ValueEventLiveData
    private var courseCardDataJson = ""
    private var bundle = Bundle()
    private var courseFetched = false
    private var allMedia = 0
    private var exerciseCount = 0
    private var modulesWithContent = 0
    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSubmitBinding.inflate(inflater, container, false)
        courseUploadRepo = CourseUploadRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)
        courseCardDataJson = requireArguments().getString(getString(R.string.manage_course_data_intent))!!
        courseCardData = Gson().fromJson(courseCardDataJson, CourseCardData::class.java)
        bundle.putString(getString(R.string.manage_course_data_intent), courseCardDataJson)
        requireActivity().setActionBar(binding.submitToolbar)
        requireActivity().actionBar!!.title = "Summary"
        startShimmer()
        binding.submitButton.setOnClickListener(this)
        binding.submitCreatePlanChip.setOnClickListener(this)
        pmeRef = pmeRef.child(getString(R.string.pme_ref)).child(FirebaseAuth.getInstance().currentUser!!.uid).child(courseCardData!!.courseCode)
        valueEventLiveData = ValueEventLiveData(pmeRef)
        valueEventLiveData.observe(viewLifecycleOwner) {
            when (it.second) {
                ChildEventTemplate.onDataChange -> {
                    pmeList.clear()
                    for ((index, i) in it.first.children.withIndex()) {
                        val value = Gson().toJson(i.value)
                        val data: PlanModulesExercise = Gson().fromJson(value, PlanModulesExercise::class.java)
                        pmeList.add(data)
                        pMEAdapter.notifyDataSetChanged()
                    }
                    pMEAdapter.dataset = pmeList

                    for (exe in pmeList) allMedia += exe.modules.uris.size
                    for (i in pmeList) if (i.modules.uris.isNotEmpty() || i.modules.content != "") modulesWithContent += 1
                    for (i in pmeList) if (i.exercise.questions.isNotEmpty()) exerciseCount += 1

                    submitCourseViewModel.setPlans(pmeList.size.toString())
                    submitCourseViewModel.setMedias(allMedia.toString())
                    submitCourseViewModel.setExercises(exerciseCount.toString())
                    submitCourseViewModel.setModules(modulesWithContent.toString())
                }
            }
        }
        submitCourseViewModel.noPlans.observe(viewLifecycleOwner) { binding.submitPlansCount.text = it }
        submitCourseViewModel.noModules.observe(viewLifecycleOwner) { binding.submitModulesCount.text = it }
        submitCourseViewModel.noExercises.observe(viewLifecycleOwner) { binding.submitExerciseCount.text = it }
        submitCourseViewModel.noMedias.observe(viewLifecycleOwner) { binding.submitMediaCount.text = it }
        delayWait(60)
        return binding.root
    }

    private fun delayWait(input: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val range = (0..input).toList().reversed()
            val flow = range.asSequence().asFlow().onEach { delay(1_000) }
            flow.collect {
                if (it + 1 == input) {
                    if (activity != null && isAdded) requireActivity().runOnUiThread {
                        if (!courseFetched) {
                            //Snackbar.make(binding.root, "Network time out", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun startShimmer() {
        binding.submitShimmer.visibility = View.VISIBLE
        binding.submitDataRoot.visibility = View.GONE
        binding.submitShimmer.startShimmer()
    }

    private fun stopShimmer() {
        binding.submitShimmer.visibility = View.GONE
        binding.submitDataRoot.visibility = View.VISIBLE
        binding.submitShimmer.stopShimmer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pMEAdapter.dataset = pmeList
        pMEAdapter.activity = requireActivity()
        pMEAdapter.courseCardDataJson = courseCardDataJson
        binding.submitRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.submitRv.adapter = pMEAdapter
        FirebaseDatabase.getInstance().reference.get().addOnSuccessListener {
            courseFetched = true
            stopShimmer()
            if (pmeList.isEmpty()) empty() else notEmpty()
        }
    }

    override fun empty() {
        binding.submitEmptyRoot.visibility = View.VISIBLE
        binding.submitPlansDataRoot.visibility = View.GONE
        binding.submitNoNetworkRoot.visibility = View.GONE
    }

    override fun notEmpty() {
        binding.submitPlansDataRoot.visibility = View.VISIBLE
        binding.submitEmptyRoot.visibility = View.GONE
        binding.submitNoNetworkRoot.visibility = View.GONE
    }

    override fun noInternet() {
        binding.submitNoNetworkRoot.visibility = View.VISIBLE
        binding.submitPlansDataRoot.visibility = View.GONE
        binding.submitEmptyRoot.visibility = View.GONE
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
            R.id.submit_create_plan_chip -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .addToBackStack("modules")
                    .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                    .replace(R.id.edit_course_root, FragmentEditPlans())
                    .commit()
            }
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
        holder.plan.text = datum.plan
        val count = "${position + 1}."
        holder.count.text = count

        holder.addExerciseChip.setOnClickListener {
            val singlePMEJson = Gson().toJson(datum)
            val fragment = AddExerciseFragment()
            val bundle = Bundle()
            bundle.putString("courseCardDataJson", courseCardDataJson)
            bundle.putInt("position", holder.absoluteAdapterPosition)
            bundle.putString("plansAndModules", singlePMEJson)
            fragment.arguments = bundle
            (activity as AppCompatActivity).supportFragmentManager
                .beginTransaction()
                .addToBackStack("edit")
                .replace(R.id.submit_root, fragment)
                .commit()

            //val json = Gson().toJson(datum.exercise)
            //val intent = Intent(activity, ActivityExercises::class.java)
            //intent.putExtra("data", json)
            //intent.putExtra("courseCardDataJson", courseCardDataJson)
            //intent.putExtra("position", holder.absoluteAdapterPosition)
            //context.startActivity(intent)
            //(activity as AppCompatActivity).overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        holder.itemView.setOnClickListener {
            val fragment = FragmentEditModules()
            val bundle = Bundle()
            bundle.putString(context.getString(R.string.manage_course_data_intent), courseCardDataJson)
            val json = Gson().toJson(dataset)
            bundle.putInt("module_position", holder.absoluteAdapterPosition)
            bundle.putString("plansAndModulesList", json)
            fragment.arguments = bundle
            (activity as AppCompatActivity).supportFragmentManager
                .beginTransaction()
                .addToBackStack("edit")
                .replace(R.id.submit_root, fragment)
                .commit()
        }

        when (datum.exercise.questions.isNotEmpty()) {
            true -> {
                holder.rvExercise.visibility = View.VISIBLE
                holder.noExercise.visibility = View.GONE
            }
            else -> {
                holder.rvExercise.visibility = View.GONE
                holder.noExercise.visibility = View.VISIBLE
            }
        }

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
        exerciseChildAdapter.dataset = datum.exercise.questions
        exerciseChildAdapter.courseCardDataJson = courseCardDataJson
        exerciseChildAdapter.activity = activity
        exerciseChildAdapter.planModulesExercise = datum
        exerciseChildAdapter.exerciseData = datum.exercise
        holder.rvExercise.setRecycledViewPool(rvExercisePool)
        holder.rvExercise.layoutManager = lmE
        holder.rvExercise.adapter = exerciseChildAdapter
    }

    override fun getItemCount() = dataset.size
}

class ExerciseChildAdapter : RecyclerView.Adapter<ExerciseChildAdapter.ViewHolder>() {
    lateinit var planModulesExercise: PlanModulesExercise
    lateinit var exerciseData: ExerciseData
    lateinit var activity: Activity
    lateinit var courseCardDataJson: String
    private lateinit var context: Context
    var dataset: ArrayList<Question> = arrayListOf()

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
        val displayNo = "Exercise ${position + 1}"
        holder.chip.text = displayNo
        holder.chip.setOnClickListener {
            val json = Gson().toJson(planModulesExercise)
            val intent = Intent(activity, ActivityExercises::class.java)
            intent.putExtra("data", json)
            intent.putExtra("courseCardDataJson", courseCardDataJson)
            intent.putExtra("position", holder.absoluteAdapterPosition)
            context.startActivity(intent)
            (activity as AppCompatActivity).overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
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