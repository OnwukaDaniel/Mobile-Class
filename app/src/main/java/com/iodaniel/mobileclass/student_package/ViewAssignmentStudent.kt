package com.iodaniel.mobileclass.student_package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentViewAssignmentStudentBinding
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion

class ViewAssignmentStudent(
    var data: MultiChoiceQuestion? = null,
    var multipleChoiceQuestions: ArrayList<HashMap<*, *>> = arrayListOf(),
) : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentViewAssignmentStudentBinding
    private lateinit var adapter: MultiChoiceQuestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentViewAssignmentStudentBinding.inflate(inflater, container, false)
        binding.viewAssignmentBackArrow.setOnClickListener(this)

        if (multipleChoiceQuestions.isEmpty()) {
            binding.multipleChoiceText.visibility = View.GONE
            binding.viewQuestionQuestion.text = data!!.question
            binding.viewQuestionExtraNote.text = data!!.extraNote
            if (data!!.instructions != "") {
                binding.viewQuestionAssignmentInstruction.visibility = View.VISIBLE
                binding.viewQuestionAssignmentInstruction.text = data!!.instructions
            }
        } else if (multipleChoiceQuestions.isNotEmpty()) {
            binding.multiChoiceSingleQuestionSection.visibility = View.GONE
            adapter = MultiChoiceQuestionAdapter()
            binding.rvMultipleChoiceStudent.adapter = adapter
            binding.rvMultipleChoiceStudent.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter.dataset = multipleChoiceQuestions
        }
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.view_assignment_back_arrow -> {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }
}

class MultiChoiceQuestionAdapter : RecyclerView.Adapter<MultiChoiceQuestionAdapter.ViewHolder>() {

    lateinit var dataset: ArrayList<HashMap<*, *>>

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioGroup: RadioGroup = itemView.findViewById(R.id.multi_choice_radio_group)
        val question: TextView = itemView.findViewById(R.id.multi_choice_question)
        val instruction: TextView = itemView.findViewById(R.id.multi_choice_instruction)

        val radioA: RadioButton = itemView.findViewById(R.id.multi_choice_a)
        val radioB: RadioButton = itemView.findViewById(R.id.multi_choice_b)
        val radioC: RadioButton = itemView.findViewById(R.id.multi_choice_c)
        val radioD: RadioButton = itemView.findViewById(R.id.multi_choice_d)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.multiple_choice_questions_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("DATA SIZE ********************************** ${dataset.size}")

        try {
            println("DATA AT POSITION 1 ********************************** ${dataset[position]}")
            val datum = dataset[position]
            holder.question.text = datum["question"] as String
            holder.instruction.text = datum["instructions"] as String

            holder.radioA.text = (datum["options"] as ArrayList<*>)[0].toString()
            holder.radioB.text = (datum["options"] as ArrayList<*>)[1].toString()
            holder.radioC.text = (datum["options"] as ArrayList<*>)[2].toString()
            holder.radioD.text = (datum["options"] as ArrayList<*>)[3].toString()

            holder.radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.multi_choice_a -> {
                        //evaluate()
                    }
                    R.id.multi_choice_b -> {
                        //evaluate()
                    }
                    R.id.multi_choice_c -> {
                        //evaluate()
                    }
                    R.id.multi_choice_d -> {
                        //evaluate()
                    }
                }
            }
        } catch (e: Exception) {
            println("EXCEPTION ******************************** ${e.printStackTrace()}")
        }
    }

    override fun getItemCount(): Int = dataset.size
}