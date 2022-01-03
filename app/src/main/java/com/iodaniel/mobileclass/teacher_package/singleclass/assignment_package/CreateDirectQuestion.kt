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
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection.CheckInternetConnection
import com.iodaniel.mobileclass.databinding.FragmentCreateDirectQuestionBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CreateDirectQuestion : Fragment(), ProgressBarController {

    private lateinit var binding: FragmentCreateDirectQuestionBinding
    private lateinit var dialog: Dialog
    private lateinit var progressBarController: ProgressBarController
    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
    private lateinit var cn: InternetConnection

    override fun onStart() {
        super.onStart()
        cn = InternetConnection(requireContext())
        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        val classInfo: ClassInfo = Json.decodeFromString(json!!)

        multiChoiceRef = multiChoiceRef
            .child("direct_question")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(classInfo.classCode)
            .push()

        binding.directQuestionUpload.setOnClickListener {
            if (cn != null) {
                cn.setCustomInternetListener(object : CheckInternetConnection {
                    override fun isConnected() {
                        upload()
                    }

                    override fun notConnected() {
                        val txt = "No active internet!!! Retry"
                        Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                    }
                })
            } else {
                Snackbar.make(binding.root, "Retry", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun upload(){
        val question = binding.directQuestionQuestion.text.toString().trim()
        val extraNote = binding.directQuestionExtraNote.text.toString().trim()
        val hash = MultiChoiceQuestion(question = question, extraNote = extraNote)

        if (question == "") return
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateDirectQuestionBinding.inflate(inflater, container, false)
        dialog = Dialog(requireContext())
        progressBarController = this
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