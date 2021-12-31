package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.databinding.FragmentCreateDirectQuestionBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion

class CreateDirectQuestion(val classInfo: ClassInfo) : Fragment(),
    ProgressBarController {

    private lateinit var binding: FragmentCreateDirectQuestionBinding
    private lateinit var dialog: Dialog
    private lateinit var progressBarController: ProgressBarController
    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
        .child("direct_question")
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child(classInfo.classCode)
        .push()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateDirectQuestionBinding.inflate(inflater, container, false)
        dialog = Dialog(requireContext())
        progressBarController = this

        binding.directQuestionUpload.setOnClickListener {
            val question = binding.directQuestionQuestion.text.toString().trim()
            val extraNote = binding.directQuestionExtraNote.text.toString().trim()

            val hash = MultiChoiceQuestion(question = question, extraNote = extraNote)

            if (question == "") return@setOnClickListener
            progressBarController.showProgressBar()

            multiChoiceRef.setValue(hash).addOnCompleteListener {
                requireActivity().onBackPressed()
                progressBarController.hideProgressBar()
                Snackbar.make(binding.root, "Uploaded successfully", Snackbar.LENGTH_LONG)
                    .show()
            }.addOnFailureListener {
                progressBarController.hideProgressBar()
                Snackbar.make(binding.root, "Error occurred!!!", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        return binding.root
    }

    override fun showProgressBar() {
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }
}