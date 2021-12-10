package com.iodaniel.mobileclass.class_assignment_upload

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.class_assignment_upload.ClassMaterialUploadInterface.MediaSupport
import com.iodaniel.mobileclass.class_assignment_upload.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.class_assignment_upload.`class`.Classes
import com.iodaniel.mobileclass.databinding.ActivityClassMaterialUploadBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import java.text.DateFormat
import java.time.Instant
import java.util.*

class ClassMaterialUpload : AppCompatActivity(), View.OnClickListener, ProgressBarController,
    MediaSupport {

    private val binding by lazy { ActivityClassMaterialUploadBinding.inflate(layoutInflater) }
    private var reference = FirebaseDatabase.getInstance().reference
    private lateinit var progressBarController: ProgressBarController
    private lateinit var mediaSupport: MediaSupport
    private val dialog by lazy { Dialog(this) }
    private lateinit var snackbar: Snackbar
    private var courseName: String = ""
    private lateinit var controller: MediaController
    private var listOfMedia: ArrayList<Uri> = arrayListOf()
    private val videoView: VideoView by lazy { binding.uploadVideoView }

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                if (it.resultCode == RESULT_OK) {
                    val dataUri = it.data!!.data
                    val extensionType = dataUri.toString().split(".").last()
                    println("ACTIVITY RESULT ******************** $extensionType")
                    when (extensionType) {
                        "mp4" -> {
                            mediaSupport.makeMediaPlayersInvisible()
                            mediaSupport.videoPlayer(dataUri!!)
                        }
                        "3gp" -> {
                            mediaSupport.makeMediaPlayersInvisible()
                            mediaSupport.videoPlayer(dataUri!!)
                        }
                        "mp3" -> {
                            mediaSupport.makeMediaPlayersInvisible()
                            mediaSupport.musicReader(dataUri!!)
                        }
                        "aac" -> {
                            mediaSupport.makeMediaPlayersInvisible()
                            mediaSupport.musicReader(dataUri!!)
                        }
                        "wav" -> {
                            mediaSupport.makeMediaPlayersInvisible()
                            mediaSupport.musicReader(dataUri!!)
                        }
                        "pdf" -> {
                            mediaSupport.makeMediaPlayersInvisible()
                            mediaSupport.pdfReader(dataUri!!)
                        }
                        "jpg" -> {
                            mediaSupport.makeMediaPlayersInvisible()
                            mediaSupport.imageReader(dataUri!!)
                        }
                        "png" -> {
                            mediaSupport.makeMediaPlayersInvisible()
                            mediaSupport.imageReader(dataUri!!)
                        }
                        "jpeg" -> {
                            mediaSupport.makeMediaPlayersInvisible()
                            mediaSupport.imageReader(dataUri!!)
                        }
                    }
                    mediaSupport.listOfMediaListener(listOfMedia.size)
                }
            })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Upload Class Material"
        initialiseDatabase()
        initialiseAllClassInterface()
        initialiseUtils()
        initVideo()
        if (intent.hasExtra("class_name"))
            courseName = intent.getStringExtra("class_name")!!
    }

    private fun initVideo() {
        controller = MediaController(this)
        controller.setAnchorView(videoView)
        videoView.setMediaController(controller)
    }

    private fun initialiseDatabase() {
        reference = reference
            .child("admins")
            .child("abc@gmailcom")
            .child("classes")
            .push()
            .child("course")
    }

    private fun initialiseAllClassInterface() {
        progressBarController = this
        mediaSupport = this
    }

    private fun initialiseUtils() {
        binding.uploadButton.setOnClickListener(this)
        binding.uploadLink.setOnClickListener(this)
        binding.pdfView.setOnClickListener(this)
        binding.uploadImage.setOnClickListener(this)
    }

    private fun checkInput() {
        progressBarController.showProgressBar()
        var snackBar: Snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        val title = binding.uploadTitle.text.trim().toString()
        val note = binding.uploadNote.text.trim().toString()
        val extraNote = binding.uploadExtraNote.text.trim().toString()
        if (title == "") {
            snackBar.setText("Empty Title!!!"); snackBar.show()
            progressBarController.hideProgressBar()
            return
        }
        if (note == "") {
            snackBar.setText("Empty Note!!!"); snackBar.show()
            progressBarController.hideProgressBar()
            return
        }

        val datetime = Date.from(Instant.now()).time
        val dateString = DateFormat.getInstance().format(datetime)
        val split = dateString.split(' ')
        val date = split[0].trim()
        val time = split[1].trim() + split[2].trim()

        val myCourse = MyCourse(courseName = courseName, year = date, time = time)
        val material =
            Material(courseName = courseName,
                note = note,
                extraNote = extraNote,
                title = title,
                year = date,
                time = time,
                dateCreated = dateString)

        val pushId = reference.push().key
        reference.child(pushId.toString()).setValue(myCourse).addOnFailureListener {
            progressBarController.hideProgressBar()
            snackBar =
                Snackbar.make(binding.root, "Upload Failed. Try again!!!", Snackbar.LENGTH_LONG)
            snackBar.show()
            return@addOnFailureListener
        }
        reference.child(pushId.toString()).child("materials").push().setValue(material)
            .addOnSuccessListener {
                progressBarController.hideProgressBar()
                clearInputs()
                val intent = Intent(this, Classes::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }.addOnFailureListener {
                progressBarController.hideProgressBar()
                snackBar =
                    Snackbar.make(binding.root, "Upload Failed. Try again!!!", Snackbar.LENGTH_LONG)
                snackBar.show()
            }
    }

    private fun clearInputs() {
        binding.uploadTitle.setText("")
        binding.uploadNote.setText("")
        binding.uploadExtraNote.setText("")
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
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT)
        dialog.show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun videoPlayer(uri: Uri) = try {
        videoView.visibility = View.VISIBLE
        listOfMedia.add(uri)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }


    override fun pdfReader(uri: Uri) = try {
        binding.pdfView.visibility = View.VISIBLE
        listOfMedia.add(uri)
        binding.pdfView.fromUri(uri).load()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun musicReader(uri: Uri) {
        val mp = MediaPlayer()
        mp.setDataSource(this, uri)
        mp.isLooping = true
        mp.setVolume(0.5F, 0.5F)
    }

    override fun imageReader(uri: Uri) = try {
        binding.uploadFile.visibility = View.VISIBLE
        listOfMedia.add(uri)
        binding.uploadFile.setImageURI(uri)
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun youTubePlayer() {

    }

    override fun makeMediaPlayersInvisible() {
        binding.uploadFile.visibility = View.INVISIBLE
        binding.pdfView.visibility = View.INVISIBLE
        binding.uploadAudio.visibility = View.INVISIBLE
        videoView.visibility = View.INVISIBLE
    }

    override fun listOfMediaListener(listLength: Int) {
        when {
            listLength == 1 -> {
                binding.uploadMoreLayout.visibility = View.INVISIBLE
                binding.uploadStack1.visibility = View.INVISIBLE
                binding.uploadStack2.visibility = View.INVISIBLE
            }
            listLength > 1 -> {
                binding.uploadNumberOfFiles.text = (listLength - 1).toString()
                binding.uploadStack1.visibility = View.VISIBLE
                binding.uploadMoreLayout.visibility = View.VISIBLE
            }
            listLength > 2 -> {
                binding.uploadNumberOfFiles.text = (listLength - 1).toString()
                binding.uploadStack2.visibility = View.VISIBLE
                binding.uploadMoreLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.upload_link -> {
                selectFileFromStorage()
            }
            R.id.upload_image -> {
                selectImageFromStorage()
            }
            R.id.upload_button -> {
                checkInput()
            }
            R.id.pdfView -> {

            }
        }
    }

}