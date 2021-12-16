package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentCreateMultiChoiceBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo

class CreateMultiChoice(private val classInfo: ClassInfo) : Fragment(), View.OnClickListener {

    private var stTypeRef = FirebaseDatabase.getInstance().reference.child("users")
    private lateinit var binding: FragmentCreateMultiChoiceBinding
    private var alpha = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateMultiChoiceBinding.inflate(inflater, container, false)
        binding.multiChoiceSubmit.setOnClickListener(this)
        binding.multiChoiceQuestionFileChooser.setOnClickListener(this)
        binding.addNewOption.setOnClickListener(this)
        return binding.root
    }

    private fun initRef(){
        stTypeRef = stTypeRef
            .child("teacher")
            .child("assignments")
            .child(classInfo.classCode)
            .push()
    }

    private fun submit() {
        val instructions = binding.multiChoiceQuestionAssignmentInstruction.text.toString().trim()
        val question = binding.multiChoiceQuestion.text.toString().trim()
        val option1 = binding.option1.text.toString().trim()
        val solution = binding.multiChoiceQuestionSolution.text.toString().trim()
        val link = binding.multiChoiceExtraLink.text.toString().trim()
        if (question == "") return
        if (option1 == "") return
        if (solution == "") return




    }

    private fun createOption() {
        try {
            val alphabets = "BCDEFGHIJKLMNOPQRSTUVWXYZ"
            val op = "(${alphabets[alpha]})"

            val options = EditText(activity)
            options.layoutParams = ViewGroup.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
            options.hint = "Option ${alphabets[alpha]}"
            options.setPadding(60, 0, 80, 20)
            options.gravity = Gravity.CENTER

            val optionsText = TextView(activity)
            optionsText.layoutParams =
                ViewGroup.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT)
            optionsText.textSize = 22F
            optionsText.text = op

            val linearLayout = LinearLayout(activity)
            linearLayout.layoutParams =
                ViewGroup.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT)
            linearLayout.addView(optionsText, 0)
            linearLayout.addView(options, 1)

            val viewIndex = binding.options.childCount
            binding.options.addView(linearLayout, viewIndex)
        } catch (e: Exception) {
            println("************************** ${e.printStackTrace()}")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.multi_choice_submit -> {
            }
            R.id.multi_choice_question_file_chooser -> {
            }
            R.id.add_new_option -> {
                createOption()
                alpha += 1
            }
        }
    }
}