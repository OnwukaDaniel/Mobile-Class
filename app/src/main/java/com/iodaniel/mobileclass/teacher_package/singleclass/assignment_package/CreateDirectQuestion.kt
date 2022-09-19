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
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.ExerciseType
import com.iodaniel.mobileclass.data_class.Question
import com.iodaniel.mobileclass.databinding.FragmentCreateDirectQuestionBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.teacher_package.classes.Material
import com.iodaniel.mobileclass.teacher_package.singleclass.DataAndPositionViewModel
import kotlinx.coroutines.*
import java.util.*

class CreateDirectQuestion : Fragment(), ProgressBarController, OnClickListener,
    DatePickerDialog.OnDateSetListener {

    private lateinit var binding: FragmentCreateDirectQuestionBinding
    private lateinit var dialog: Dialog
    private lateinit var progressBarController: ProgressBarController
    private var directQuestionRef = FirebaseDatabase.getInstance().reference
    private var cn: InternetConnection? = null
    private val calender: Calendar = Calendar.getInstance()
    private var dueDate: String = "No due date..."
    private lateinit var material: Material
    private var exercisePosition = 0
    private var courseCardDataJson = ""
    private var courseCardData: CourseCardData? = null
    private var pmeRef = FirebaseDatabase.getInstance().reference
    private val dataAndPositionViewModel by activityViewModels<DataAndPositionViewModel>()
    private val months = arrayListOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )

    private fun uploadTask(questions: ArrayList<Question>) {
        val snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        pmeRef.setValue(questions).addOnSuccessListener {
            if (activity != null && isAdded) requireActivity().runOnUiThread {
                snackBar.setText("Uploaded successfully").show()
                requireActivity().onBackPressed()
                progressBarController.hideProgressBar()
            }
        }.addOnFailureListener {
            if (activity != null && isAdded) requireActivity().runOnUiThread {
                snackBar.setText("Error occurred!!!").show()
                progressBarController.hideProgressBar()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateDirectQuestionBinding.inflate(inflater, container, false)
        dialog = Dialog(requireContext())
        cn = InternetConnection(requireContext())
        dataAndPositionViewModel.dataAndPosition.observe(viewLifecycleOwner) {
            exercisePosition = it.second
            courseCardData = it.first
            pmeRef = pmeRef.child(getString(R.string.pme_ref))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(courseCardData!!.courseCode)
                .child(exercisePosition.toString())
                .child("exercise")
                .child("questions")
            progressBarController = this
            binding.directQuestionSetDeadline.setOnClickListener(this)
            binding.directQuestionUpload.setOnClickListener(this)
            binding.directQuestionDateCancel.setOnClickListener(this)
        }
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
            if (activity != null && isAdded) requireActivity().runOnUiThread {
                progressBarController.hideProgressBar()
                uploadTask.cancel("Error Timeout!!! Retry")
            }
        }
    }

    private fun upload() {
        val snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        val question = binding.directQuestionQuestion.text.toString().trim()
        val extraNote = binding.directQuestionExtraNote.text.toString().trim()
        val datetime = Calendar.getInstance().timeInMillis.toString()
        val singleQuestion = Question(
            singleQuestion = question,
            extraNote = extraNote,
            exerciseType = ExerciseType.NORMAL_QUESTION,
            timeCreated = datetime,
        )

        if (question == "") return
        progressBarController.showProgressBar()
        pmeRef.get().addOnSuccessListener {
            if (it.exists()){
                val d = Gson().fromJson(Gson().toJson(it.value), ArrayList::class.java)
                val questionList: ArrayList<Question> = arrayListOf()
                for (i in d) {
                    val que = Gson().fromJson(Gson().toJson(i), Question::class.java)
                    questionList.add(que)
                }
                questionList.add(singleQuestion)
                val uploaded = Util.functionTimeout(30_000, uploadTask(questionList))
                if (uploaded) snackBar.setText("Uploaded!")
                    .show() else snackBar.setText("Error Timeout!!! Retry").show()
                progressBarController.hideProgressBar()
            } else {
                val uploaded = Util.functionTimeout(30_000, uploadTask(arrayListOf(singleQuestion)))
                if (uploaded) snackBar.setText("Uploaded!")
                    .show() else snackBar.setText("Error Timeout!!! Retry").show()
                progressBarController.hideProgressBar()
            }
        }
    }

    private fun checkNetwork() {
        if (cn != null) {
            cn!!.setCustomInternetListener(object : InternetConnection.CheckInternetConnection {
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