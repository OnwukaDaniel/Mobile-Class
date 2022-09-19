package com.iodaniel.mobileclass.student_package.owned_courses

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.ModuleData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.databinding.FragmentContentBinding
import com.iodaniel.mobileclass.liveDataClasses.ValueEventLiveData
import com.iodaniel.mobileclass.util.ChildEventTemplate.onDataChange
import com.iodaniel.mobileclass.viewModel.CourseCardViewModel
import com.iodaniel.mobileclass.viewModel.PMEViewModel

class FragmentContent : Fragment() {
    private lateinit var binding: FragmentContentBinding
    private lateinit var courseCardData: CourseCardData
    private val contentParentAdapter = ContentParentAdapter()
    private var pmeRef = FirebaseDatabase.getInstance().reference
    private val myAuth = FirebaseAuth.getInstance().currentUser!!.uid
    private val pMEViewModel by activityViewModels<PMEViewModel>()
    private val courseCardViewModel by activityViewModels<CourseCardViewModel>()
    private val plansAndModulesList: ArrayList<PlanModulesExercise> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentContentBinding.inflate(inflater, container, false)
        contentParentAdapter.activity = requireActivity()
        contentParentAdapter.dataset = plansAndModulesList
        binding.contentRv.adapter = contentParentAdapter
        binding.contentRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        courseCardViewModel.courseCard.observe(viewLifecycleOwner) { course ->
            courseCardData = course
            pmeRef = pmeRef
                .child(getString(R.string.pme_ref))
                .child(courseCardData.instructorInChargeUID)
                .child(courseCardData.courseCode)
            val dataSnapshotLiveData = ValueEventLiveData(pmeRef)
            dataSnapshotLiveData.observe(viewLifecycleOwner) {
                when (it.second) {
                    onDataChange -> {
                        for (i in it.first.children) {
                            val value = Gson().toJson(i.value)
                            val data: PlanModulesExercise = Gson().fromJson(value, PlanModulesExercise::class.java)
                            if (data !in plansAndModulesList) {
                                plansAndModulesList.add(data)
                                contentParentAdapter.notifyItemInserted(plansAndModulesList.size)
                            }
                        }
                        pMEViewModel.setPME(plansAndModulesList)
                    }
                }
            }
        }
        return binding.root
    }

    companion object {
    }
}

class ContentAdapter : RecyclerView.Adapter<ContentAdapter.ViewHolder>() {
    var courseCardDataJson: String = ""
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var plansAndModulesList: ArrayList<PlanModulesExercise>
    var modulePosition = 0
    var data: ModuleData = ModuleData()
    var dataset: ArrayList<MutableMap<String, String>> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.module_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        dataset = data.uris
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_owned_module, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.content.text = datum["filename"]
        holder.itemView.setOnClickListener {
            val fragment = FragmentOwnedSingleModule()
            val bundle = Bundle()
            bundle.putInt("modulePosition", modulePosition)
            fragment.arguments = bundle
            (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                .addToBackStack("content")
                .replace(R.id.owned_course_root, fragment).commit()
        }
    }

    override fun getItemCount() = dataset.size
}

class ContentParentAdapter : RecyclerView.Adapter<ContentParentAdapter.ViewHolder>() {
    var courseCardDataJson: String = ""
    private lateinit var context: Context
    lateinit var activity: Activity
    var dataset: ArrayList<PlanModulesExercise> = arrayListOf()
    private val rvPool = RecyclerView.RecycledViewPool()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val count: TextView = itemView.findViewById(R.id.header_count)
        val planText: TextView = itemView.findViewById(R.id.header_text)
        val rvModules: RecyclerView = itemView.findViewById(R.id.plans_rv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_owned_modules_plans, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.planText.text = datum.plan
        val count = (position + 1).toString()
        holder.count.text = count
        val lm = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        lm.isItemPrefetchEnabled = true
        lm.initialPrefetchItemCount = datum.modules.uris.size
        val contentAdapter = ContentAdapter()

        contentAdapter.data = datum.modules
        contentAdapter.dataset = datum.modules.uris
        contentAdapter.plansAndModulesList = dataset
        contentAdapter.context = context
        contentAdapter.activity = activity

        contentAdapter.modulePosition = holder.absoluteAdapterPosition
        holder.rvModules.adapter = contentAdapter
        holder.rvModules.layoutManager = lm
        holder.rvModules.setRecycledViewPool(rvPool)
        holder.itemView.setOnClickListener {
            val fragment = FragmentOwnedSingleModule()
            val bundle = Bundle()
            bundle.putInt("modulePosition", holder.absoluteAdapterPosition)
            fragment.arguments = bundle
            (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                .addToBackStack("content")
                .replace(R.id.owned_course_root, fragment).commit()
        }
    }

    override fun getItemCount() = dataset.size
}