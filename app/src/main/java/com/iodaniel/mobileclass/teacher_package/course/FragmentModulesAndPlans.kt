package com.iodaniel.mobileclass.teacher_package.course

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.ModuleData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.databinding.FragmentModulesAndPlansBinding
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.util.BackgroundHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentModulesAndPlans : Fragment(), View.OnClickListener, BackgroundHelper {
    private lateinit var binding: FragmentModulesAndPlansBinding
    private val modulesAndPlansAdapter = ModulesAndPlansAdapter()
    private var pmeRef = FirebaseDatabase.getInstance().reference
    private var connectionRef = FirebaseDatabase.getInstance().reference
    private val plansAndModulesList: ArrayList<PlanModulesExercise> = arrayListOf()
    private var courseCardData: CourseCardData? = null
    private var courseCardDataJson = ""
    private var bundle = Bundle()
    private val listener = Listener()
    private val connectionListener = ConnectionListener()
    private var fetchedData = false
    private val fragmentEditPlans = FragmentEditPlans()
    private var networkButEmpty = false
    private var pDialog: Dialog? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    inner class Listener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                modulesAndPlansAdapter.notifyItemRangeRemoved(0, plansAndModulesList.size)
                plansAndModulesList.clear()
                for ((index, i) in snapshot.children.withIndex()) {
                    val value = Gson().toJson(i.value)
                    val data: PlanModulesExercise = Gson().fromJson(value, PlanModulesExercise::class.java)
                    plansAndModulesList.add(data)
                    modulesAndPlansAdapter.notifyItemInserted(index)
                }
                modulesAndPlansAdapter.dataset = plansAndModulesList
                fetchedData = true
                this@FragmentModulesAndPlans.notEmpty()
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    inner class ConnectionListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (!fetchedData) this@FragmentModulesAndPlans.empty() else this@FragmentModulesAndPlans.notEmpty()
            networkButEmpty = true
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModulesAndPlansBinding.inflate(inflater, container, false)

        courseCardDataJson = requireArguments().getString(getString(R.string.manage_course_data_intent))!!
        courseCardData = Gson().fromJson(courseCardDataJson, CourseCardData::class.java)
        requireActivity().setActionBar(binding.modulesAndPlansToolbar)
        requireActivity().actionBar!!.title = courseCardData!!.courseName

        bundle.putString(getString(R.string.manage_course_data_intent), courseCardDataJson)
        fragmentEditPlans.arguments = bundle
        connectionRef = connectionRef.child(getString(R.string.network_value))
        pmeRef = pmeRef.child(getString(R.string.pme_ref)).child(FirebaseAuth.getInstance().currentUser!!.uid).child(courseCardData!!.courseCode)
        pDialog = Util.progressDialog("Please wait...", requireContext(), requireActivity())
        pDialog?.show()
        scope.launch {
            delay(12_000)
            if (activity != null) requireActivity().runOnUiThread {
                if (networkButEmpty && !fetchedData) this@FragmentModulesAndPlans.empty()
                if (fetchedData) this@FragmentModulesAndPlans.notEmpty()
            }
        }

        binding.modulesAndPlansCreatePlan.setOnClickListener(this)
        modulesAndPlansAdapter.activity = requireActivity()
        modulesAndPlansAdapter.dataset = plansAndModulesList
        modulesAndPlansAdapter.courseCardDataJson = courseCardDataJson
        binding.modulesAndPlansRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.modulesAndPlansRv.adapter = modulesAndPlansAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pmeRef.addValueEventListener(listener)
        connectionRef.addValueEventListener(connectionListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionRef.removeEventListener(connectionListener)
        pmeRef.removeEventListener(listener)
        pDialog?.dismiss()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.modules_and_plans_create_plan -> requireActivity().supportFragmentManager
                .beginTransaction().addToBackStack("create plans")
                .setCustomAnimations(R.anim.slide_out, R.anim.slide_in)
                .replace(R.id.modules_and_plans_root, fragmentEditPlans)
                .commit()
        }
    }

    override fun empty() {
        binding.modulesAndPlansEmptyRoot.visibility = View.VISIBLE
        binding.modulesAndPlansNoNetworkRoot.visibility = View.GONE
        pDialog?.dismiss()
    }

    override fun notEmpty() {
        binding.modulesAndPlansEmptyRoot.visibility = View.GONE
        binding.modulesAndPlansNoNetworkRoot.visibility = View.GONE
        pDialog?.dismiss()
    }

    override fun noInternet() {
        binding.modulesAndPlansNoNetworkRoot.visibility = View.VISIBLE
        binding.modulesAndPlansEmptyRoot.visibility = View.GONE
        pDialog?.dismiss()
    }
}

class ModulesAdapter : RecyclerView.Adapter<ModulesAdapter.ViewHolder>() {
    var courseCardDataJson: String = ""
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var plansAndModulesList: ArrayList<PlanModulesExercise>
    var modulePosition = 0
    var data: ModuleData = ModuleData()
    private val imagesExtList: ArrayList<String> = arrayListOf("jpg", "png", "jpeg")
    var dataset: ArrayList<MutableMap<String, String>> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: Chip = itemView.findViewById(R.id.row_module_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        dataset = data.uris
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

class ModulesAndPlansAdapter : RecyclerView.Adapter<ModulesAndPlansAdapter.ViewHolder>() {
    var courseCardDataJson: String = ""
    private lateinit var context: Context
    lateinit var activity: Activity
    var dataset: ArrayList<PlanModulesExercise> = arrayListOf()
    private val rvPool = RecyclerView.RecycledViewPool()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val planTitle: TextView = itemView.findViewById(R.id.row_module_plans_header_text)
        val count: TextView = itemView.findViewById(R.id.row_module_plans_header_count)
        val rvModules: RecyclerView = itemView.findViewById(R.id.row_module_plans_rv)
        val layoutEdit: LinearLayout = itemView.findViewById(R.id.row_module_plans_root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_modules_plans, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.planTitle.text = datum.plan
        val count = (position + 1).toString()
        holder.count.text = count
        val lm = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        lm.isItemPrefetchEnabled = true
        lm.initialPrefetchItemCount = datum.modules.uris.size
        val modulesAdapter = ModulesAdapter()

        modulesAdapter.data = datum.modules
        modulesAdapter.dataset = datum.modules.uris
        modulesAdapter.plansAndModulesList = dataset
        modulesAdapter.context = context
        holder.rvModules.adapter = modulesAdapter
        holder.rvModules.layoutManager = lm
        holder.rvModules.setRecycledViewPool(rvPool)

        holder.layoutEdit.setOnClickListener {
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
                .replace(R.id.modules_and_plans_root, fragment)
                .commit()
        }
    }

    override fun getItemCount() = dataset.size
}