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
import com.iodaniel.mobileclass.data_class.ExerciseType
import com.iodaniel.mobileclass.data_class.Question
import com.iodaniel.mobileclass.databinding.FragmentOwnedAllExercisesBinding
import com.iodaniel.mobileclass.shared_classes.FragmentStudentViewAssignment
import com.iodaniel.mobileclass.viewModel.PMEViewModel
import com.iodaniel.mobileclass.viewModel.QuestionTransferViewModel

class FragmentOwnedAllExercises : Fragment() {
    private lateinit var binding: FragmentOwnedAllExercisesBinding
    private val singleExerciseAdapter = SingleExerciseAdapter()
    private val questionTransferViewModel by activityViewModels<QuestionTransferViewModel>()
    private val pMEViewModel by activityViewModels<PMEViewModel>()
    private var dataset = arrayListOf<Question>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnedAllExercisesBinding.inflate(inflater, container, false)
        requireActivity().setActionBar(binding.allExercisePlanToolbar)

        pMEViewModel.singlePlanModulesExercise.observe(viewLifecycleOwner) {
            requireActivity().actionBar!!.title = it.plan
            dataset = it.exercise.questions
            if (dataset.isEmpty()) binding.noExercise.visibility = View.VISIBLE

            println("Monitor ****************************** $dataset")
            singleExerciseAdapter.activity = requireActivity()
            singleExerciseAdapter.dataset = dataset
            singleExerciseAdapter.questionTransferViewModel = questionTransferViewModel
            binding.exerciseRv.adapter = singleExerciseAdapter
            binding.exerciseRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = resources.getColor(R.color.app_primary_color)
    }
}

class SingleExerciseAdapter : RecyclerView.Adapter<SingleExerciseAdapter.ViewHolder>() {
    lateinit var questionTransferViewModel: QuestionTransferViewModel
    var courseCardDataJson: String = ""
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var dataset: ArrayList<Question>
    var exercisePosition = 0

    init {
        setHasStableIds(true)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseNumber: TextView = itemView.findViewById(R.id.exercise_number)
        val exerciseType: TextView = itemView.findViewById(R.id.exercise_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_owned_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        val displayNo = "Exercise ${position + 1}"
        holder.exerciseNumber.text = displayNo
        when (datum.exerciseType) {
            ExerciseType.NORMAL_QUESTION -> holder.exerciseType.visibility = View.GONE
            ExerciseType.DOC_QUESTION -> holder.exerciseType.text = "Contains file"
            ExerciseType.MULTI_QUESTION -> holder.exerciseType.text = "Multiple choice"
        }
        holder.itemView.setOnClickListener {
            questionTransferViewModel.setQuestion(datum)
            (activity as AppCompatActivity).supportFragmentManager.beginTransaction().addToBackStack("question")
                .replace(R.id.owned_course_root, FragmentStudentViewAssignment())
                .commit()
        }
    }

    override fun getItemCount() = dataset.size

    override fun getItemId(position: Int) = position.toLong()
}