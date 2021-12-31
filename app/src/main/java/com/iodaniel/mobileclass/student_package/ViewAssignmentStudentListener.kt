package com.iodaniel.mobileclass.student_package

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener
import com.google.android.material.chip.Chip
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentViewAssignmentStudentBinding
import com.iodaniel.mobileclass.student_package.MultiChoiceQuestionAdapter.ScrollClickHelpers
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ViewAssignmentStudentListener(
    var data: MultiChoiceQuestion? = null,
    var multipleChoiceQuestions: ArrayList<HashMap<*, *>> = arrayListOf(),
) : Fragment(), OnClickListener, ScrollClickHelpers, AssignmentViewTypeListener {

    private lateinit var binding: FragmentViewAssignmentStudentBinding
    private var adapter: MultiChoiceQuestionAdapter = MultiChoiceQuestionAdapter()
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private lateinit var assignmentViewTypeListener: AssignmentViewTypeListener
    lateinit var scrollClickHelpers: ScrollClickHelpers

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentViewAssignmentStudentBinding.inflate(inflater, container, false)
        binding.viewAssignmentBackArrow.setOnClickListener(this)
        scrollClickHelpers = this
        assignmentViewTypeListener = this

        if (multipleChoiceQuestions.isEmpty()) {
            assignmentViewTypeListener.questionsOnlyView()
            binding.viewQuestionQuestion.text = data!!.question
            binding.viewQuestionExtraNote.text = data!!.extraNote
            if (data!!.instructions != "") {
                binding.viewQuestionAssignmentInstruction.visibility = View.VISIBLE
                binding.viewQuestionAssignmentInstruction.text = data!!.instructions
            }
        } else if (multipleChoiceQuestions.isNotEmpty()) {
            assignmentViewTypeListener.multiChoiceView()
            PagerSnapHelper().attachToRecyclerView(binding.rvMultipleChoiceStudent)
            binding.rvMultipleChoiceStudent.adapter = adapter
            binding.rvMultipleChoiceStudent.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter.dataset = multipleChoiceQuestions
            smoothScroller = LinearSmoothScroller(requireContext())
            adapter.scrollClickHelpers = scrollClickHelpers
            adapter.activity = requireActivity()

            binding.rvMultipleChoiceStudent.addOnItemTouchListener(object :
                SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    return rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
                }
            })
        }
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.view_assignment_back_arrow -> requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun smoothScrollHelper(position: Int) {
        smoothScroller.targetPosition = position
        binding.rvMultipleChoiceStudent.layoutManager?.startSmoothScroll(smoothScroller)
    }

    override fun multiChoiceView() {
        binding.viewAssignmentToolbarText.text = "Multiple Choice Questions"
        binding.viewAssignmentResultQuestionRoot.visibility = View.GONE
        binding.viewAssignmentSingleQuestionSection.visibility = View.GONE
        binding.viewAssignmentMultipleQuestionRoot.visibility = View.VISIBLE
    }

    override fun questionsOnlyView() {
        binding.viewAssignmentToolbarText.text = "Direct Questions"
        binding.viewAssignmentResultQuestionRoot.visibility = View.GONE
        binding.viewAssignmentMultipleQuestionRoot.visibility = View.GONE
        binding.viewAssignmentSingleQuestionSection.visibility = View.VISIBLE
    }

    override fun fileQuestionsView() {}

    override fun resultMultiChoiceView() {
        binding.viewAssignmentToolbarText.text = "Result"
        binding.viewAssignmentSingleQuestionSection.visibility = View.GONE
        binding.viewAssignmentMultipleQuestionRoot.visibility = View.GONE
        binding.viewAssignmentResultQuestionRoot.visibility = View.VISIBLE
    }
}

class MultiChoiceQuestionAdapter : RecyclerView.Adapter<MultiChoiceQuestionAdapter.ViewHolder>() {

    lateinit var dataset: ArrayList<HashMap<*, *>>
    private var solutionSubmitted: ArrayList<String> = arrayListOf()
    private var solutions: ArrayList<String> = arrayListOf()
    lateinit var activity: Activity
    lateinit var scrollClickHelpers: ScrollClickHelpers

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioGroup: RadioGroup = itemView.findViewById(R.id.multi_choice_radio_group)
        val question: TextView = itemView.findViewById(R.id.multi_choice_question)
        val instruction: TextView = itemView.findViewById(R.id.multi_choice_instruction)

        val radioA: RadioButton = itemView.findViewById(R.id.multi_choice_a)
        val radioB: RadioButton = itemView.findViewById(R.id.multi_choice_b)
        val radioC: RadioButton = itemView.findViewById(R.id.multi_choice_c)
        val radioD: RadioButton = itemView.findViewById(R.id.multi_choice_d)
        val radioE: RadioButton = itemView.findViewById(R.id.multi_choice_e)

        val next: Button = itemView.findViewById(R.id.multi_choice_row_next_button)
        val previous: Button = itemView.findViewById(R.id.multi_choice_row_prev_button)
        val submit: Chip = itemView.findViewById(R.id.multi_choice_row_submit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        solutions = arrayListOf()
        for (i in 0 until dataset.size) solutions.add(dataset[i]["solution"] as String)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.multiple_choice_questions_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            holder.submit.visibility = if (position == dataset.size - 1) View.VISIBLE else View.GONE

            val datum = dataset[position]
            holder.question.text = datum["question"] as String
            holder.instruction.text = datum["instructions"] as String
            val options = (datum["options"] as ArrayList<*>)

            holder.radioA.text = options[0].toString()
            holder.radioB.text = options[1].toString()
            holder.radioC.text = options[2].toString()
            holder.radioD.text = options[3].toString()
            if (options.size > 4) {
                holder.radioE.visibility = View.VISIBLE
                holder.radioE.text = options[4].toString()
            } else {
                holder.radioE.visibility = View.GONE
            }

            holder.radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (position < dataset.size) {
                    scrollClickHelpers.smoothScrollHelper(position + 1)
                }

                when (checkedId) {
                    R.id.multi_choice_a -> {
                        solutionSubmitted.add("A")
                    }
                    R.id.multi_choice_b -> {
                        solutionSubmitted.add("B")
                    }
                    R.id.multi_choice_c -> {
                        solutionSubmitted.add("C")
                    }
                    R.id.multi_choice_d -> {
                        solutionSubmitted.add("D")
                    }
                    R.id.multi_choice_e -> {
                        solutionSubmitted.add("E")
                    }
                }
            }

            holder.next.setOnClickListener {
                if (position < dataset.size - 1) scrollClickHelpers.smoothScrollHelper(position + 1)
            }

            holder.previous.setOnClickListener {
                if (position > 0) {
                    solutionSubmitted.removeLast()
                    scrollClickHelpers.smoothScrollHelper(position - 1)
                }
            }

            holder.submit.setOnClickListener {
                submit()
            }
        } catch (e: Exception) {
            println("EXCEPTION ******************************** ${e.printStackTrace()}")
        }
    }

    private fun submit() {
        try {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val result = evaluateAssessment()
                val activityX = (activity as FragmentActivity)
                activity.runOnUiThread {
                    activityX.supportFragmentManager.popBackStack()
                    activityX.supportFragmentManager.beginTransaction()
                        .addToBackStack("review_of_answers")
                        .replace(R.id.a_class_frame_student, FragmentResult(result = result, overall = dataset.size))
                        .commit()
                }
            }
        } catch (e: Exception) {
            println("EXCEPTION ******************************** ${e.printStackTrace()}")
        }
    }

    private fun evaluateAssessment(): Int {
        var numberOfCorrectQuestion = 0
        for (i in 0 until dataset.size) if (solutions[i] == solutionSubmitted[i]) {
            numberOfCorrectQuestion++
        }
        return numberOfCorrectQuestion
    }

    override fun getItemCount(): Int = dataset.size

    interface ScrollClickHelpers {
        fun smoothScrollHelper(position: Int)
    }
}

class FragmentResult(val result: Int, val overall: Int) : Fragment() {

    private lateinit var binding: FragmentViewAssignmentStudentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAssignmentStudentBinding.inflate(inflater, container, false)
        binding.viewAssignmentResultQuestionRoot.visibility = View.VISIBLE
        binding.viewAssignmentBackArrow.setOnClickListener{
            requireActivity().onBackPressed()
        }
        val score = ((result.toFloat() / overall.toFloat())*100).toInt().toString() + "%"
        binding.viewAssignmentScore.text = score
        binding.viewAssignmentProgressBar.progress = (score.split("%")[0]).toInt()
        return binding.root
    }
}