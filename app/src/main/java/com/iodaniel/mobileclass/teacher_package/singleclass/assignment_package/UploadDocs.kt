package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.databinding.FragmentUploadDocsBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.MediaSupport
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

class UploadDocs: Fragment(), ProgressBarController,
    MediaSupport, View.OnClickListener {

    private lateinit var binding: FragmentUploadDocsBinding
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var dialog: Dialog
    private var fileName = ""
    private lateinit var progressBarController: ProgressBarController
    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
    private var listOfMedia: ArrayList<String> = arrayListOf()
    private lateinit var mediaSupport: MediaSupport
    private lateinit var classInfo: ClassInfo
    private lateinit var cn: InternetConnection

    override fun onStart() {
        super.onStart()
        cn = InternetConnection(requireContext())
    }

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                try {
                    if (it.data!!.data == null) return@ActivityResultCallback
                    if (it.resultCode == AppCompatActivity.RESULT_OK) {
                        val dataUri = it.data!!.data
                        fileName = "Attachment"
                        val contentResolver = requireActivity().contentResolver
                        val mime = MimeTypeMap.getSingleton()
                        when (mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!) {
                            "mp4" -> {
                                listOfMedia.add(dataUri.toString())
                                mediaSupport.videoPlayer(dataUri!!)
                            }
                            "3gp" -> {
                                listOfMedia.add(dataUri.toString())
                                mediaSupport.videoPlayer(dataUri!!)
                            }
                            "mp3" -> {
                                listOfMedia.add(dataUri.toString())
                                mediaSupport.musicReader(dataUri!!)
                            }
                            "aac" -> {
                                listOfMedia.add(dataUri.toString())
                                mediaSupport.musicReader(dataUri!!)
                            }
                            "wav" -> {
                                listOfMedia.add(dataUri.toString())
                                mediaSupport.musicReader(dataUri!!)
                            }
                            "pdf" -> {
                                listOfMedia.add(dataUri.toString())
                                mediaSupport.pdfReader(dataUri!!)
                            }
                            "jpg" -> {
                                listOfMedia.add(dataUri.toString())
                                mediaSupport.imageReader(dataUri!!)
                            }
                            "png" -> {
                                listOfMedia.add(dataUri.toString())
                                mediaSupport.imageReader(dataUri!!)
                            }
                            "jpeg" -> {
                                listOfMedia.add(dataUri.toString())
                                mediaSupport.imageReader(dataUri!!)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("GetExternal Uri Exception ******************* ${e.printStackTrace()}")
                }
            }
        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUploadDocsBinding.inflate(inflater, container, false)
        dialog = Dialog(requireContext())
        progressBarController = this
        binding.uploadQuestionCancel.setOnClickListener(this)
        binding.uploadQuestionUpload.setOnClickListener(this)
        binding.uploadAttachment.setOnClickListener(this)
        binding.uploadQuestionBackArrow.setOnClickListener(this)
        mediaSupport = this

        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        classInfo = Json.decodeFromString(json!!)

        multiChoiceRef = multiChoiceRef
            .child("doc_question")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(classInfo.classCode)
            .push()
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
        }
    }

    private fun uploadQueDoc() {
        val question = binding.uploadQuestionQuestion.text.toString().trim()
        val extraNote = binding.uploadQuestionExtraNote.text.toString().trim()
        if (question == "") return
        if (fileName == "") {
            val txt = "Select a file\nOr use Direct question option"
            Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
            return
        }
        progressBarController.showProgressBar()

        val dateTime = Calendar.getInstance().time.time.toString()
        val arrayDownloadUris = arrayListOf<String>()

        for (file in listOfMedia) {
            val fileUri = Uri.parse(file)
            val contentResolver = requireContext().contentResolver
            val mime = MimeTypeMap.getSingleton()
            val extension =
                mime.getExtensionFromMimeType(contentResolver?.getType(fileUri))!!
            val event = (dateTime).replace("//", ".").replace("/", ".")
            val finalStorageRef = storageRef.child("$event.$extension")
            val uploadTask = finalStorageRef.putFile(fileUri)

            uploadTask.continueWith { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                finalStorageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    finalStorageRef.downloadUrl.addOnSuccessListener {
                        val downloadUri = it.toString()
                        arrayDownloadUris.add("$downloadUri}")
                        if (arrayDownloadUris.size == listOfMedia.size) {

                            val docQuestion = MultiChoiceQuestion()
                            docQuestion.className= classInfo.className
                            docQuestion.classCode= classInfo.classCode
                            docQuestion.teacherInChargeName= classInfo.teacherInChargeName
                            docQuestion.teacherInChargeUID= classInfo.teacherInChargeUID
                            docQuestion.datetime = dateTime
                            docQuestion.question = question
                            docQuestion.extraNote = extraNote
                            docQuestion.mediaUris = arrayDownloadUris

                            multiChoiceRef.setValue(docQuestion).addOnCompleteListener {
                                val txt = "Uploaded successfully"
                                Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                                requireActivity().onBackPressed()
                                progressBarController.hideProgressBar()
                            }.addOnFailureListener {
                                val txt = "Error occurred!!!"
                                Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                            }
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
}