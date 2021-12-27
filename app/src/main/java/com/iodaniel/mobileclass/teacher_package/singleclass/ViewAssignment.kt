package com.iodaniel.mobileclass.teacher_package.singleclass

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
import com.iodaniel.mobileclass.databinding.ActivityViewAssignmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ViewAssignment(
    var data: MultiChoiceQuestion? = null,
    var multipleChoiceQuestions: ArrayList<HashMap<*, *>> = arrayListOf(),
) : Fragment() {

    private lateinit var binding: ActivityViewAssignmentBinding
    private lateinit var adapter: MultiChoiceQuestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = ActivityViewAssignmentBinding.inflate(inflater, container, false)

        if (multipleChoiceQuestions.isEmpty()) {
            binding.viewQuestionQuestion.setText(data!!.question)
            binding.viewQuestionExtraNote.setText(data!!.extraNote)
            if (data!!.instructions != "") {
                binding.viewQuestionAssignmentInstruction.visibility = View.VISIBLE
                binding.viewQuestionAssignmentInstruction.setText(data!!.instructions)
            }
        } else if (multipleChoiceQuestions.isNotEmpty()) {
            println("DATA ********************************** $multipleChoiceQuestions")
            binding.multiChoiceSingleQuestionSection.visibility = View.GONE
            adapter = MultiChoiceQuestionAdapter()
            binding.rvMultipleChoice.adapter = adapter
            binding.rvMultipleChoice.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter.dataset = multipleChoiceQuestions
        }

        /*binding.multiChoiceRowPrevButton.setOnClickListener {
            runBlocking{ launch(Dispatchers.Main){ binding.rvMultipleChoice.scrollToPosition(1) } }
        }

        binding.multiChoiceRowNextButton.setOnClickListener {
            runBlocking{ launch(Dispatchers.Main){ binding.rvMultipleChoice.scrollToPosition(0) } }
        }*/



        return binding.root
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

        try{
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
        } catch (e: Exception){
            println("EXCEPTION ******************************** ${e.printStackTrace()}")
        }
    }

    override fun getItemCount(): Int = dataset.size
}