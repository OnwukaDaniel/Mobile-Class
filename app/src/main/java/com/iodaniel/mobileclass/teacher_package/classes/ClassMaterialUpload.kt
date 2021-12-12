package com.iodaniel.mobileclass.teacher_package.classes

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.MediaController
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.MediaSupport
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.databinding.ActivityClassMaterialUploadBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import java.text.DateFormat
import java.time.Instant
import java.util.*
import kotlin.random.Random

class ClassMaterialUpload : AppCompatActivity(), View.OnClickListener, ProgressBarController,
    MediaSupport {

    private val binding by lazy { ActivityClassMaterialUploadBinding.inflate(layoutInflater) }
    private var reference = FirebaseDatabase.getInstance().reference
    private lateinit var progressBarController: ProgressBarController
    private lateinit var mediaSupport: MediaSupport
    private val dialog by lazy { Dialog(this) }
    private lateinit var snackbar: Snackbar
    private var className: String = ""
    private var classImage: String = ""
    private lateinit var controller: MediaController
    private var listOfMedia: ArrayList<Uri> = arrayListOf()
    private val storageRef = FirebaseStorage.getInstance().reference
    //private val videoView: VideoView by lazy { binding.uploadVideoView }

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                if (it.data!!.data == null) {
                    val error = "Error loading file!!!"
                    var snackBar = Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                    return@ActivityResultCallback
                }
                if (it.resultCode == RESULT_OK) {
                    val dataUri = it.data!!.data
                    val contentResolver = applicationContext.contentResolver
                    val mime = MimeTypeMap.getSingleton()
                    val extensionType =
                        mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
                    println("ACTIVITY RESULT ******************** $extensionType")
                    when (extensionType) {
                        /*"mp4" -> {
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
                        }*/
                        "jpg" -> {
                            classImage = dataUri!!.toString()
                            binding.uploadFile.setImageURI(Uri.parse(classImage))
                        }
                        "png" -> {
                            classImage = dataUri!!.toString()
                            binding.uploadFile.setImageURI(Uri.parse(classImage))
                        }
                        "jpeg" -> {
                            classImage = dataUri!!.toString()
                            binding.uploadFile.setImageURI(Uri.parse(classImage))
                        }
                    }
                }
            })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Upload Class Material"
        initialiseDatabase()
        initialiseAllClassInterface()
        initialiseUtils()
    }

    private fun initialiseDatabase() {
        reference = reference
            .child("admins")
            .child("abc@gmailcom")
            .child("classes")
            .push()
    }

    /*private fun initVideo() {
        controller = MediaController(this)
        controller.setAnchorView(videoView)
        videoView.setMediaController(controller)
    }*/

    private fun cloudStorageUpload(uris: ArrayList<Uri>, date: String, time: String) {
        val newDownloadUris: ArrayList<String> = arrayListOf<String>()
        progressBarController.showProgressBar()
        for (uri in uris) {
            val contentResolver = applicationContext.contentResolver
            val mime = MimeTypeMap.getSingleton()
            val extension = mime.getExtensionFromMimeType(contentResolver?.getType(uri))!!
            val event = (date + time + uri.toString()).replace("//", ".").replace("/", ".")
            val finalStorageRef = storageRef.child("$event+$uri.$extension")
            val uploadTask = finalStorageRef.putFile(uri)
            uploadTask.continueWith { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                finalStorageRef.downloadUrl
            }.addOnCompleteListener { task ->
                val downloadUri = task.result
                newDownloadUris.add(downloadUri.toString())
                if (newDownloadUris.size == uris.size) {
                    println("NewDownloadUri *********************** $newDownloadUris")
                    //uploadToFirebaseDatabase(newDownloadUris)
                }
            }.addOnFailureListener {
                progressBarController.hideProgressBar()
                println("OnFailureListener *********************** ${it.printStackTrace()}")
            }
            println("EXTENSION *********************** $extension")
        }
    }

    private fun initialiseAllClassInterface() {
        progressBarController = this
        mediaSupport = this
    }

    private fun initialiseUtils() {
        binding.uploadButton.setOnClickListener(this)
        binding.uploadImage.setOnClickListener(this)
    }

    private fun upload() {
        progressBarController.showProgressBar()

        var snackBar: Snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)

        val datetime = Date.from(Instant.now()).time
        val dateString = DateFormat.getInstance().format(datetime)
        val split = dateString.split(' ')
        val date = split[0].trim()
        val time = split[1].trim() + split[2].trim()
        val className = binding.uploadClassName.text.toString().trim()
        val classCode = binding.uploadCode.text.toString().trim()

        if (className == "") {
            snackBar.setText("Empty Class Name!!!"); snackBar.show()
            progressBarController.hideProgressBar()
            return
        }
        if (classCode == "") {
            snackBar.setText("Empty Class Code!!!"); snackBar.show()
            progressBarController.hideProgressBar()
            return
        }

        val pushId = reference.push().key
        /*.child(pushId.toString())*/

        if (classImage == "") {
            val classInfo = ClassInfo(className = className,
                time = time, classCode = classCode, dateCreated = "$time/$date")
            reference.setValue(classInfo).addOnCompleteListener {
                progressBarController.hideProgressBar()
                startActivity(Intent(this, TeacherPage::class.java))
                overridePendingTransition(0, 0)
            }.addOnFailureListener {
                progressBarController.hideProgressBar()
                snackBar = Snackbar.make(binding.root,
                    "Upload Failed. Please Try again", Snackbar.LENGTH_LONG)
                snackBar.show()
                return@addOnFailureListener
            }
            return
        }
        val contentResolver = applicationContext.contentResolver
        val mime = MimeTypeMap.getSingleton()
        val extension =
            mime.getExtensionFromMimeType(contentResolver?.getType(Uri.parse(classImage)))!!
        val event = (date + time + classImage.toString()).replace("//", ".").replace("/", ".")
        val finalStorageRef = storageRef.child("$event.$extension")
        val uploadTask = finalStorageRef.putFile(Uri.parse(classImage))

        uploadTask.continueWith { task ->
            if (!task.isSuccessful) task.exception?.let { throw it }
            finalStorageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finalStorageRef.downloadUrl.addOnSuccessListener {
                    val downloadUri = it.toString()
                    val rand = Random(42).nextInt(10, 255)
                    val classInfo = ClassInfo(className = className,
                        time = time,
                        red =rand,
                        green = rand,
                        blue = rand,
                        classCode = classCode,
                        classImage = downloadUri,
                        dateCreated = "$time/$date")
                    reference.setValue(classInfo).addOnCompleteListener {
                        progressBarController.hideProgressBar()
                        startActivity(Intent(this, TeacherPage::class.java))
                        overridePendingTransition(0, 0)
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

    private fun scaleBitmap(path: String): Bitmap {
        val bm = BitmapFactory.decodeFile(path)
        val inh = (bm.height * (512.0 / bm.width)).toInt()
        return Bitmap.createScaledBitmap(bm, 512, inh, true)
    }

    private fun clearInputs() {
        binding.uploadClassName.setText("")
        binding.uploadClassName.setText("")
    }

    /*private fun selectFileFromStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = ''*
        pickFileLauncher.launch(intent)
    }*/

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

    /*override fun videoPlayer(uri: Uri) = try {
        videoView.visibility = View.VISIBLE
        listOfMedia.add(uri)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }*/

    /*override fun pdfReader(uri: Uri) = try {
        binding.pdfView.visibility = View.VISIBLE
        listOfMedia.add(uri)
        binding.pdfView.fromUri(uri).load()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }*/

    /*override fun musicReader(uri: Uri) {
        binding.uploadAudio.visibility = View.VISIBLE
        val mp = MediaPlayer()
        mp.setDataSource(this, uri)
        mp.isLooping = true
        mp.setVolume(0.5F, 0.5F)
    }*/

    override fun imageReader(uri: Uri) = try {
        binding.uploadFile.visibility = View.VISIBLE
        listOfMedia.add(uri)
        binding.uploadFile.setImageURI(uri)
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun makeMediaPlayersInvisible() {
        binding.uploadFile.visibility = View.INVISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.upload_image -> {
                selectImageFromStorage()
            }
            R.id.upload_button -> {
                upload()
            }
        }
    }

}