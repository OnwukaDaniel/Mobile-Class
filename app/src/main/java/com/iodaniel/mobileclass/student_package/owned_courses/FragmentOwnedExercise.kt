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
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.databinding.FragmentOwnedExerciseBinding
import com.iodaniel.mobileclass.viewModel.CourseCardViewModel
import com.iodaniel.mobileclass.viewModel.PMEViewModel

class FragmentOwnedExercise : Fragment() {
    private lateinit var binding: FragmentOwnedExerciseBinding
    private lateinit var courseCardData: CourseCardData
    private val pMEViewModel by activityViewModels<PMEViewModel>()
    private val courseCardViewModel by activityViewModels<CourseCardViewModel>()
    private var plansAndModulesList: ArrayList<PlanModulesExercise> = arrayListOf()
    private val selectModuleExerciseParentAdapter = SelectModuleExerciseParentAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnedExerciseBinding.inflate(inflater, container, false)
        pMEViewModel.planModulesExercise.observe(viewLifecycleOwner) {
            plansAndModulesList = it
            plansAndModulesList[0].exercise.questions
            selectModuleExerciseParentAdapter.pMEViewModel = pMEViewModel
            selectModuleExerciseParentAdapter.activity = requireActivity()
            selectModuleExerciseParentAdapter.dataset = plansAndModulesList
            binding.selectRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.selectRv.adapter = selectModuleExerciseParentAdapter
        }
        return binding.root
    }
}

class SelectModuleExerciseParentAdapter : RecyclerView.Adapter<SelectModuleExerciseParentAdapter.ViewHolder>() {
    lateinit var pMEViewModel: PMEViewModel
    var courseCardDataJson: String = ""
    private lateinit var context: Context
    lateinit var activity: Activity
    var dataset: ArrayList<PlanModulesExercise> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val count: TextView = itemView.findViewById(R.id.select_header_count)
        val planText: TextView = itemView.findViewById(R.id.select_header_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_owned_select_module, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.planText.text = datum.plan
        val count = (position + 1).toString()
        holder.count.text = count
        holder.itemView.setOnClickListener {
            pMEViewModel.setSinglePME(dataset[holder.absoluteAdapterPosition])
            (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                .addToBackStack("exercises")
                .replace(R.id.owned_course_root, FragmentOwnedAllExercises())
                .commit()
        }
    }

    override fun getItemCount() = dataset.size
}