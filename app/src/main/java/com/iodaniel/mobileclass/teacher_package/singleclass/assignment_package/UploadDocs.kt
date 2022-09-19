package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.DatePicker
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.ExerciseType
import com.iodaniel.mobileclass.data_class.Question
import com.iodaniel.mobileclass.databinding.FragmentUploadDocsBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.MediaSupport
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.teacher_package.singleclass.DataAndPositionViewModel
import java.util.*

class UploadDocs : Fragment(), ProgressBarController, MediaSupport, View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private lateinit var binding: FragmentUploadDocsBinding
    private val storageRef = FirebaseStorage.getInstance().reference
    private val dataAndPositionViewModel by activityViewModels<DataAndPositionViewModel>()
    private lateinit var dialog: Dialog
    private var fileName = ""
    private lateinit var progressBarController: ProgressBarController
    private var docRef = FirebaseDatabase.getInstance().reference
    private var listOfMedia: ArrayList<Map<String, String>> = arrayListOf()
    private lateinit var mediaSupport: MediaSupport
    private lateinit var cn: InternetConnection
    private val calender: Calendar = Calendar.getInstance()
    private var dueDate: String = "No due date..."
    private val months = arrayListOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    private val acceptedTypes: ArrayList<String> = arrayListOf("mp4", "3gp", "mp3", "aac", "wav", "pdf", "jpg", "png", "jpeg")
    private var exercisePosition = 0
    private var courseCardDataJson = ""
    private var courseCardData: CourseCardData? = null
    private var pmeRef = FirebaseDatabase.getInstance().reference

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        try {
            if (it.data!!.data == null) return@ActivityResultCallback
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val dataUri = it.data!!.data
                fileName = "Attachment"
                val file_name = com.iodaniel.mobileclass.util.Util.getFileName(dataUri!!)
                val contentResolver = requireActivity().contentResolver
                val mime = MimeTypeMap.getSingleton()
                if (mime.getExtensionFromMimeType(contentResolver?.getType(dataUri))!! in acceptedTypes) {
                    val map = mapOf("name" to file_name, "url" to dataUri.toString())
                    listOfMedia.add(map)
                    binding.uploadAttachmentFileView.visibility = View.VISIBLE
                    binding.uploadAttachmentFileAttachmentText.text = listOfMedia.size.toString()
                }
            }
        } catch (e: Exception) {
            println("GetExternal Uri Exception ******************* ${e.printStackTrace()}")
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUploadDocsBinding.inflate(inflater, container, false)
        dataAndPositionViewModel.dataAndPosition.observe(viewLifecycleOwner) {
            cn = InternetConnection(requireContext())
            dialog = Dialog(requireContext())
            exercisePosition = it.second
            courseCardData = it.first
            pmeRef = pmeRef.child(getString(R.string.pme_ref))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(courseCardData!!.courseCode)
                .child(exercisePosition.toString())
                .child("exercise")
                .child("questions")

            docRef = docRef
                .child("doc_question")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("exercise")
                .child(courseCardData!!.dateCreated)
                .push()
        }

        progressBarController = this
        binding.uploadQuestionCancel.setOnClickListener(this)
        binding.uploadQuestionUpload.setOnClickListener(this)
        binding.uploadAttachment.setOnClickListener(this)
        binding.uploadQuestionBackArrow.setOnClickListener(this)
        binding.uploadQuestionSetDeadline.setOnClickListener(this)
        binding.uploadQuestionDateCancel.setOnClickListener(this)
        mediaSupport = this
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.upload_question_cancel -> {
                listOfMedia = arrayListOf()
                binding.uploadAttachmentFileView.visibility = View.GONE
                Snackbar.make(binding.root, "Cleared all attachments", Snackbar.LENGTH_LONG).show()
            }
            R.id.upload_question_upload -> {
                if (cn != null) {
                    cn.setCustomInternetListener(object :
                        InternetConnection.CheckInternetConnection {
                        override fun isConnected() {
                            uploadQueDoc()
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
            R.id.upload_question_back_arrow -> {
                requireActivity().onBackPressed()
            }
            R.id.upload_attachment -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                pickFileLauncher.launch(intent)
            }
            R.id.upload_question_set_deadline -> {
                val day = calender.get(Calendar.DATE)
                val month = calender.get(Calendar.MONTH)
                val year = calender.get(Calendar.YEAR)
                val dateDialogPicker = DatePickerDialog(requireContext(), this, year, month, day)
                dateDialogPicker.show()
            }
            R.id.upload_question_date_cancel -> {
                dueDate = "No due date..."
                binding.uploadQuestionDateText.text = "Set deadline..."
                binding.uploadQuestionRealDate.text = ""
                Snackbar.make(binding.root, "Cleared deadline", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun upload(arrayDownloadUris: ArrayList<String>) {
        val snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        val question = binding.uploadQuestionQuestion.text.toString().trim()
        val extraNote = binding.uploadQuestionExtraNote.text.toString().trim()
        val datetime = Calendar.getInstance().timeInMillis.toString()
        val docQuestion = Question(
            docQuestion = question,
            extraNote = extraNote,
            exerciseType = ExerciseType.DOC_QUESTION,
            timeCreated = datetime,
            docStorageLinks = listOfMedia,
        )

        if (question == "") return
        progressBarController.showProgressBar()
        pmeRef.get().addOnSuccessListener {
            if (it.exists()) {
                val d = Gson().fromJson(Gson().toJson(it.value), ArrayList::class.java)
                val questionList: ArrayList<Question> = arrayListOf()
                for (i in d) {
                    val que = Gson().fromJson(Gson().toJson(i), Question::class.java)
                    questionList.add(que)
                }
                questionList.add(docQuestion)
                val uploaded = Util.functionTimeout(30_000, finalUpload(questionList))
                if (uploaded) snackBar.setText("Uploaded!").show() else snackBar.setText("Error Timeout!!! Retry").show()
                progressBarController.hideProgressBar()
            } else {
                val uploaded = Util.functionTimeout(30_000, finalUpload(arrayListOf(docQuestion)))
                if (uploaded) snackBar.setText("Uploaded!").show() else snackBar.setText("Error Timeout!!! Retry").show()
                progressBarController.hideProgressBar()
            }
        }.addOnFailureListener {
            snackBar.setText("Error Timeout!!! Retry").show()
            progressBarController.hideProgressBar()
        }
    }

    private fun finalUpload(questionList: ArrayList<Question>) {
        val snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        pmeRef.setValue(questionList).addOnSuccessListener {
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

    private fun uploadQueDoc() {
        val question = binding.uploadQuestionQuestion.text.toString().trim()
        val extraNote = binding.uploadQuestionExtraNote.text.toString().trim()
        if (question == "") return
        if (fileName == "") {
            Snackbar.make(binding.root, "Select a file\nOr use Direct question option", Snackbar.LENGTH_LONG).show()
            return
        }
        progressBarController.showProgressBar()

        val arrayDownloadUris = arrayListOf<String>()

        for (file in listOfMedia) {
            val event = Calendar.getInstance().timeInMillis.toString()
            val fileUri = Uri.parse(file["url"])
            val extension = Util.getExtension(fileUri, requireContext())
            val finalStorageRef = storageRef.child(storagePath).child(courseCardData!!.instructorInChargeUID)
                .child(courseCardData!!.courseCode)
                .child("${file["name"]}.$extension")
            val uploadTask = finalStorageRef.putFile(fileUri)

            uploadTask.continueWith { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                finalStorageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    finalStorageRef.downloadUrl.addOnSuccessListener {
                        val downloadUri = it.toString()
                        arrayDownloadUris.add(downloadUri)
                        if (arrayDownloadUris.size == listOfMedia.size) {
                            upload(arrayDownloadUris)
                        }
                    }
                }
            }.addOnFailureListener {
                progressBarController.hideProgressBar()
                val txt = "Upload Failed. Please Try again"
                Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                return@addOnFailureListener
            }
        }
    }

    override fun showProgressBar() {
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val paramWrap = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.setLayout(paramWrap, paramWrap)
        dialog.show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun videoPlayer(uri: Uri) = try {
        binding.uploadAttachmentFileView.visibility = View.VISIBLE
        binding.uploadAttachmentFileAttachmentText.text = listOfMedia.size.toString()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun pdfReader(uri: Uri) = try {
        binding.uploadAttachmentFileView.visibility = View.VISIBLE
        binding.uploadAttachmentFileAttachmentText.text = listOfMedia.size.toString()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun musicReader(uri: Uri) {
        binding.uploadAttachmentFileView.visibility = View.VISIBLE
        binding.uploadAttachmentFileAttachmentText.text = listOfMedia.size.toString()
    }

    override fun imageReader(uri: Uri) = try {
        binding.uploadAttachmentFileView.visibility = View.VISIBLE
        binding.uploadAttachmentFileAttachmentText.text = listOfMedia.size.toString()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun makeMediaPlayersInvisible() {

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dueDate = "$dayOfMonth+.$month+.$year"
        binding.uploadQuestionDateText.text = "Due on:"
        binding.uploadQuestionRealDate.text = "$dayOfMonth, ${months[month]}, $year"
        val txt = "Day: $dayOfMonth, Month: ${months[month]}, Year: $year"
        Snackbar.make(binding.root, txt, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val SOLUTION_TO_COURSE_EXERCISE = "SOLUTION_TO_COURSE_EXERCISE"
        const val storagePath = "plans_module_exercise"
    }
}