package com.iodaniel.mobileclass.teacher_package.singleclass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.databinding.ActivityViewAssignmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ViewAssignment(
    var data: MultiChoiceQuestion? = null,
    var multipleChoiceQuestions: ArrayList<MultiChoiceQuestion> = arrayListOf(),
) : Fragment() {

    private lateinit var binding: ActivityViewAssignmentBinding

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
        }
        return binding.root
    }
}