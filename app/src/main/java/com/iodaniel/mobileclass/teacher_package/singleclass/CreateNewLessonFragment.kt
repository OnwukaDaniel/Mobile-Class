package com.iodaniel.mobileclass.teacher_package.singleclass

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.CreateNewLessonFragmentBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.*
import com.iodaniel.mobileclass.teacher_package.classes.Material
import java.text.DateFormat
import java.time.Instant
import java.util.*

class CreateNewLessonFragment(private val classInfo: ClassInfo) : Fragment(),
    View.OnClickListener,
    ProgressBarController,
    MediaSupport {

    private var stTypeRef = FirebaseDatabase.getInstance().reference.child("users")
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var progressBarController: ProgressBarController
    private lateinit var mediaSupport: MediaSupport
    private val dialog by lazy { Dialog(requireContext()) }
    private var classImage: String = ""
    private var className: String = classInfo.className
    private lateinit var controller: MediaController
    private var listOfMedia: ArrayList<Uri> = arrayListOf()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val videoView: VideoView by lazy { binding.newLessonUploadVideoView }
    private lateinit var binding: CreateNewLessonFragmentBinding

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                try {
                    if (it.data!!.data == null) {
                        val error = "Error loading file!!!"
                        var snackBar =
                            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                        return@ActivityResultCallback
                    }
                    if (it.resultCode == AppCompatActivity.RESULT_OK) {
                        val dataUri = it.data!!.data
                        val contentResolver = requireActivity().contentResolver
                        val mime = MimeTypeMap.getSingleton()
                        val extensionType =
                            mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
                        when (extensionType) {
                            "mp4" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.videoPlayer(dataUri!!)
                                listOfMedia.add(dataUri)
                            }
                            "3gp" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.videoPlayer(dataUri!!)
                                listOfMedia.add(dataUri)
                            }
                            "mp3" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                listOfMedia.add(dataUri)
                            }
                            "aac" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                listOfMedia.add(dataUri)
                            }
                            "wav" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                listOfMedia.add(dataUri)
                            }
                            "pdf" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.pdfReader(dataUri!!)
                                listOfMedia.add(dataUri)
                            }
                            "jpg" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                classImage = dataUri!!.toString()
                                mediaSupport.imageReader(dataUri)
                                listOfMedia.add(dataUri)
                                binding.newLessonImageview.setImageURI(dataUri)
                            }
                            "png" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                classImage = dataUri!!.toString()
                                mediaSupport.imageReader(dataUri)
                                listOfMedia.add(dataUri)
                                binding.newLessonImageview.setImageURI(dataUri)
                            }
                            "jpeg" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                //mediaSupport.makeMediaPlayersInvisible()
                                classImage = dataUri!!.toString()
                                mediaSupport.imageReader(dataUri)
                                listOfMedia.add(dataUri)
                                binding.newLessonImageview.setImageURI(dataUri)
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
        binding = CreateNewLessonFragmentBinding.inflate(inflater, container, false)
        binding.newLessonUploadImage.setOnCloseIconClickListener(this)
        initialiseAllClassInterface()
        initialiseUtils()
        return binding.root
    }

    private fun initialiseAllClassInterface() {
        progressBarController = this
        mediaSupport = this
    }

    private fun initialiseUtils() {
        binding.newLessonUploadButton.setOnClickListener(this)
        binding.newLessonUploadImage.setOnClickListener(this)
    }

    private fun upload() {
        val arrayDownloadUris = arrayListOf<String>()
        progressBarController.showProgressBar()
        var snackBar: Snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)

        val datetime = Date.from(Instant.now()).time
        val dateString = DateFormat.getInstance().format(datetime)
        val split = dateString.split(' ')
        val date = split[0].trim()
        val time = split[1].trim() + split[2].trim()
        stTypeRef = stTypeRef
            .child("teacher")
            .child("materials")
            .child(classInfo.classCode)
            .push()

        if (classInfo.classCode == "") {
            snackBar.setText("Empty Class Code!!!"); snackBar.show()
            progressBarController.hideProgressBar()
            return
        }

        for (file in listOfMedia) { //fileUris
            val contentResolver = requireContext().contentResolver
            val mime = MimeTypeMap.getSingleton()
            val extension =
                mime.getExtensionFromMimeType(contentResolver?.getType(file))!!
            val event = (date + time + file).replace("//", ".").replace("/", ".")
            val finalStorageRef = storageRef.child("$event.$extension")
            val uploadTask = finalStorageRef.putFile(file)

            uploadTask.continueWith { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                finalStorageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    finalStorageRef.downloadUrl.addOnSuccessListener {
                        val downloadUri = it.toString()
                        arrayDownloadUris.add(downloadUri)
                        println("DOWNLOAD URI ******************** $downloadUri")
                        println("****************** New * ${arrayDownloadUris.size} ------------ Old --${listOfMedia.size}")
                        if (arrayDownloadUris.size == listOfMedia.size) {
                            val material = Material(
                                courseName = className,
                                note = binding.createClassNote.text.toString(),
                                extraNote = binding.createClassExtraNote.text.toString(),
                                mediaUris = arrayDownloadUris,
                                heading = binding.createClassHeading.text.toString(),
                                time = time,
                                dateCreated = classInfo.datetime
                            )

                            stTypeRef.setValue(material).addOnCompleteListener {
                                requireActivity().onBackPressed()
                                progressBarController.hideProgressBar()
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                progressBarController.hideProgressBar()
                val txt = "Upload Failed. Please Try again"
                snackBar = Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG)
                snackBar.show()
                return@addOnFailureListener
            }
        }
    }

    private fun scaleBitmap(path: String): Bitmap {
        val bm = BitmapFactory.decodeFile(path)
        val inh = (bm.height * (512.0 / bm.width)).toInt()
        return Bitmap.createScaledBitmap(bm, 512, inh, true)
    }

    private fun clearInputs() {
        binding.createClassHeading.setText("")
        binding.createClassNote.setText("")
        binding.createClassExtraNote.setText("")
    }

    private fun selectFileFromStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        pickFileLauncher.launch(intent)
    }

    private fun selectImageFromStorage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickFileLauncher.launch(intent)
    }

    override fun showProgressBar() {
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun videoPlayer(uri: Uri) = try {
        print("URI videoPlayer ******************* $uri")
        videoView.visibility = View.VISIBLE
        controller = MediaController(requireActivity())
        controller.setAnchorView(videoView)
        videoView.setMediaController(controller)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun pdfReader(uri: Uri) = try {
        print("URI pdfReader ******************* $uri")
        binding.newLessonPdfView.visibility = View.VISIBLE
        binding.newLessonPdfView.fromUri(uri).load()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun musicReader(uri: Uri) {
        print("URI musicReader ******************* $uri")
        binding.newLessonUploadAudio.visibility = View.VISIBLE
        val mp = MediaPlayer()
        mp.setDataSource(requireContext(), uri)
        mp.isLooping = true
        mp.setVolume(0.9F, 0.9F)
    }

    override fun imageReader(uri: Uri) = try {
        print("URI imageReader ******************* $uri")
        binding.newLessonImageview.visibility = View.VISIBLE
        binding.newLessonImageview.setImageURI(uri)
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun makeMediaPlayersInvisible() {
        binding.newLessonImageview.visibility = View.INVISIBLE
        binding.newLessonUploadAudio.visibility = View.INVISIBLE
        binding.newLessonPdfView.visibility = View.INVISIBLE
        binding.newLessonUploadVideoView.visibility = View.INVISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.new_lesson_upload_image -> {
                selectFileFromStorage()
            }
            R.id.new_lesson_upload_button -> {
                upload()
            }
        }
    }
}