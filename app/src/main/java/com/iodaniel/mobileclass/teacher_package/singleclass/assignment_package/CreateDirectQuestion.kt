package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.databinding.FragmentCreateDirectQuestionBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.teacher_package.classes.AssignmentQuestion
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.teacher_package.classes.Material
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

class CreateDirectQuestion : Fragment(), ProgressBarController, OnClickListener,
    DatePickerDialog.OnDateSetListener {

    private lateinit var binding: FragmentCreateDirectQuestionBinding
    private lateinit var dialog: Dialog
    private lateinit var progressBarController: ProgressBarController
    private var directQuestionRef = FirebaseDatabase.getInstance().reference
    private lateinit var cn: InternetConnection
    private val calender: Calendar = Calendar.getInstance()
    private var dueDate: String = "No due date..."
    private lateinit var material: Material
    private val months = arrayListOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )

    override fun onStart() {
        super.onStart()
        cn = InternetConnection(requireContext())
        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        val classInfo: ClassInfo = Json.decodeFromString(json!!)

        val materialJson = bundle.getString("materialJson")
        material = Gson().fromJson(materialJson, Material::class.java)

        directQuestionRef = directQuestionRef
            .child("direct_question")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(classInfo.classCode)
            .child(material.dateCreated)
            .push()
    }

    private fun uploadTask(hash: AssignmentQuestion) {
        val snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        directQuestionRef.setValue(hash).addOnCompleteListener {
            requireActivity().runOnUiThread {
                requireActivity().onBackPressed()
                progressBarController.hideProgressBar()
                snackBar.setText("Uploaded successfully").show()
            }
        }.addOnFailureListener {
            requireActivity().runOnUiThread {
                progressBarController.hideProgressBar()
                snackBar.setText("Error occurred!!!").show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateDirectQuestionBinding.inflate(inflater, container, false)
        dialog = Dialog(requireContext())
        progressBarController = this
        binding.directQuestionSetDeadline.setOnClickListener(this)
        binding.directQuestionUpload.setOnClickListener(this)
        binding.directQuestionDateCancel.setOnClickListener(this)
        return binding.root
    }

    override fun showProgressBar() {
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun networkTimeoutFunction(delay: Long, networkFunction: Unit) {
        val scope = CoroutineScope(Dispatchers.IO)
        val uploadTask = scope.async { networkFunction }
        val counterTask = scope.async { delay(delay) }
        if (counterTask.isCompleted && !uploadTask.isCompleted) {
            requireActivity().runOnUiThread {
                progressBarController.hideProgressBar()
                uploadTask.cancel("Error Timeout!!! Retry")
            }
        }
    }

    private fun upload() {
        val snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        val question = binding.directQuestionQuestion.text.toString().trim()
        val extraNote = binding.directQuestionExtraNote.text.toString().trim()
        val datetime = Calendar.getInstance().time.time.toString()
        val hash = AssignmentQuestion()
        hash.question = question
        hash.extraNote = extraNote
        hash.dueDate = dueDate
        hash.datetime = datetime
        hash.questionType = getString(R.string.DIRECTQUESTION)

        if (question == "") return
        progressBarController.showProgressBar()
        val uploaded = Util.functionTimeout(30_000, uploadTask(hash))
        if (uploaded) snackBar.setText("Uploaded!")
            .show() else snackBar.setText("Error Timeout!!! Retry").show()
        progressBarController.hideProgressBar()
    }

    private fun checkNetwork() {
        if (cn != null) {
            cn.setCustomInternetListener(object : InternetConnection.CheckInternetConnection {
                override fun isConnected() {
                    upload()
                }

                override fun notConnected() {
                    val txt = "No active internet!!! Retry"
                    Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                }
            })
        } else Snackbar.make(binding.root, "Retry", Snackbar.LENGTH_LONG).show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.direct_question_set_deadline -> {
                val day = calender.get(Calendar.DATE)
                val month = calender.get(Calendar.MONTH)
                val year = calender.get(Calendar.YEAR)
                val dateDialogPicker = DatePickerDialog(requireContext(), this, year, month, day)
                dateDialogPicker.show()
            }
            R.id.direct_question_upload -> checkNetwork()
            R.id.direct_question_date_cancel -> {
                dueDate = "No due date..."
                binding.directQuestionDateText.text = "Set deadline..."
                binding.directQuestionRealDate.text = ""
                Snackbar.make(binding.root, "Cleared deadline", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dueDate = "$dayOfMonth.$month.$year"
        binding.directQuestionDateText.text = "Due on:"
        binding.directQuestionRealDate.text = "$dayOfMonth, ${months[month]}, $year"
    }
}