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
import com.iodaniel.mobileclass.databinding.FragmentUploadDocsBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.MediaSupport
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion
import java.util.*

class UploadDocs(val classInfo: ClassInfo) : Fragment(), ProgressBarController,
    MediaSupport {

    private lateinit var binding: FragmentUploadDocsBinding
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var dialog: Dialog
    private var fileName = ""
    private lateinit var progressBarController: ProgressBarController
    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
        .child("doc_question")
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child(classInfo.classCode)
        .push()
    private var listOfMedia: ArrayList<String> = arrayListOf()
    private lateinit var mediaSupport: MediaSupport

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                try {
                    if (it.data!!.data == null) return@ActivityResultCallback
                    if (it.resultCode == AppCompatActivity.RESULT_OK) {
                        val dataUri = it.data!!.data
                        fileName = "Attachment"
                        println("______________________ filename: $fileName")
                        val contentResolver = requireActivity().contentResolver
                        val mime = MimeTypeMap.getSingleton()
                        val extensionType =
                            mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
                        when (extensionType) {
                            "mp4" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.videoPlayer(dataUri!!)
                                listOfMedia.add(dataUri.toString())
                            }
                            "3gp" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.videoPlayer(dataUri!!)
                                listOfMedia.add(dataUri.toString())
                            }
                            "mp3" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                listOfMedia.add(dataUri.toString())
                            }
                            "aac" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                listOfMedia.add(dataUri.toString())
                            }
                            "wav" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                listOfMedia.add(dataUri.toString())
                            }
                            "pdf" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.pdfReader(dataUri!!)
                                listOfMedia.add(dataUri.toString())
                            }
                            "jpg" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.imageReader(dataUri!!)
                                listOfMedia.add(dataUri.toString())
                            }
                            "png" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.imageReader(dataUri!!)
                                listOfMedia.add(dataUri.toString())
                            }
                            "jpeg" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.imageReader(dataUri!!)
                                listOfMedia.add(dataUri.toString())
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("GetExternal Uri Exception ******************* ${e.printStackTrace()}")
                }
            })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUploadDocsBinding.inflate(inflater, container, false)
        dialog = Dialog(requireContext())
        progressBarController = this
        mediaSupport = this

        binding.uploadQuestionUpload.setOnClickListener {
            uploadQueDoc()
        }

        binding.uploadAttachment.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            pickFileLauncher.launch(intent)
        }

        return binding.root
    }

    private fun uploadQueDoc() {
        val question = binding.uploadQuestionQuestion.text.toString().trim()
        val extraNote = binding.uploadQuestionExtraNote.text.toString().trim()
        if (question == "") return
        if (fileName == "") return
        progressBarController.showProgressBar()

        val dateTime = Calendar.getInstance().time.time.toString()
        val arrayDownloadUris = arrayListOf<String>()

        for (file in listOfMedia) { //fileUris
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

                            val docQuestion = MultiChoiceQuestion(
                                className = classInfo.className,
                                classCode = classInfo.classCode,
                                teacherInChargeName = classInfo.teacherInChargeName,
                                teacherInChargeUID = classInfo.teacherInChargeUID,
                                datetime = dateTime,
                                question = question,
                                extraNote = extraNote,
                                mediaUris = arrayDownloadUris,
                            )
                            multiChoiceRef.setValue(docQuestion).addOnCompleteListener {
                                requireActivity().onBackPressed()
                                progressBarController.hideProgressBar()
                            }.addOnFailureListener {
                                Snackbar.make(binding.root,
                                    "Error occurred!!!",
                                    Snackbar.LENGTH_LONG)
                                    .show()
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
        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun videoPlayer(uri: Uri) = try {
        binding.uploadAttachmentFileView.visibility = View.VISIBLE
        binding.uploadAttachmentFileView.text = fileName
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun pdfReader(uri: Uri) = try {
        binding.uploadAttachmentFileView.visibility = View.VISIBLE
        binding.uploadAttachmentFileView.text = fileName
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun musicReader(uri: Uri) {
        binding.uploadAttachmentFileView.visibility = View.VISIBLE
        binding.uploadAttachmentFileView.text = fileName
    }

    override fun imageReader(uri: Uri) = try {
        binding.uploadAttachmentFileView.visibility = View.VISIBLE
        binding.uploadAttachmentFileView.text = fileName
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun makeMediaPlayersInvisible() {

    }

}