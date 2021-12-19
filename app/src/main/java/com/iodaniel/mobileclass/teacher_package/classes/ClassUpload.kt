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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ClassUploadBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import java.text.DateFormat
import java.time.Instant
import java.util.*
import kotlin.random.Random

class ClassUpload : AppCompatActivity(), View.OnClickListener, ProgressBarController {

    private val binding by lazy { ClassUploadBinding.inflate(layoutInflater) }
    private var reference = FirebaseDatabase.getInstance().reference
    private var allClassesRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var progressBarController: ProgressBarController
    private val dialog by lazy { Dialog(this) }
    private var className: String = ""
    private var classImage: String = ""
    private lateinit var controller: MediaController
    private var listOfMedia: ArrayList<Uri> = arrayListOf()
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var errorSnackBar: Snackbar

    val customImages: ArrayList<Int> = arrayListOf(R.drawable.classimages,
        R.drawable.classimages1,
        R.drawable.classimages2,
        R.drawable.classimages3)

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
                    if (it.resultCode == RESULT_OK) {
                        val dataUri = it.data!!.data
                        val contentResolver = applicationContext.contentResolver
                        val mime = MimeTypeMap.getSingleton()
                        val extensionType =
                            mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
                        println("ACTIVITY RESULT ******************** $extensionType")
                        when (extensionType) {
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
                } catch (e: Exception) {
                }
            })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Upload Class Material"
        errorSnackBar = Snackbar.make(binding.root,
            "Upload Failed. Please Try again", Snackbar.LENGTH_LONG)
        initialiseDatabase()
        initialiseAllClassInterface()
        initialiseUtils()
    }

    private fun initialiseDatabase() {
        allClassesRef = allClassesRef
            .child("class_codes")
            .push()
    }

    private fun initialiseAllClassInterface() {
        progressBarController = this
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
        println("datetime ****************** $datetime")
        val split = dateString.split(' ')
        val date = split[0].trim()
        val time = split[1].trim() + split[2].trim()
        val className = binding.uploadClassName.text.toString().trim()
        val classCode = UUID.randomUUID().toString()

        val classInfo = ClassInfo(className = className,
            time = time, datetime = datetime.toString(), classCode = classCode)

        val details = hashMapOf("classCode" to classInfo.classCode,
            "auth" to auth)

        if (className == "") {
            snackBar.setText("Empty Class Name!!!"); snackBar.show()
            progressBarController.hideProgressBar()
            return
        }
        if (classImage == "") {
            reference = reference
                .child("teacher")
                .child(auth)
                .child("classes")
                .child(classCode)
            allClassesRef.setValue(details).addOnCompleteListener {
                reference.setValue(classInfo).addOnCompleteListener {
                    progressBarController.hideProgressBar()
                    startActivity(Intent(this, TeacherPage::class.java))
                    overridePendingTransition(0, 0)
                }.addOnFailureListener {
                    progressBarController.hideProgressBar()
                    errorSnackBar.show()
                    return@addOnFailureListener
                }
            }.addOnFailureListener {
                progressBarController.hideProgressBar()
                errorSnackBar.show()
                return@addOnFailureListener
            }
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
                    val classCodeX = UUID.randomUUID().toString()
                    val downloadUri = it.toString()
                    val rand = Random(42).nextInt(10, 255)
                    reference = reference
                        .child("teacher")
                        .child(auth)
                        .child("classes")
                        .child(classCodeX)

                    val detailsX = hashMapOf("classCode" to classCodeX,
                        "auth" to auth)

                    val classInfoX = ClassInfo(className = className,
                        time = time,
                        red = rand,
                        green = rand,
                        blue = rand,
                        classCode = classCodeX,
                        classImage = downloadUri,
                        datetime = datetime.toString())
                    allClassesRef.setValue(detailsX).addOnCompleteListener {
                        reference.setValue(classInfoX).addOnCompleteListener {
                            progressBarController.hideProgressBar()
                            startActivity(Intent(this, TeacherPage::class.java))
                            overridePendingTransition(0, 0)
                        }.addOnFailureListener {
                            progressBarController.hideProgressBar()
                            errorSnackBar.show()
                            return@addOnFailureListener
                        }
                    }.addOnFailureListener {
                        progressBarController.hideProgressBar()
                        errorSnackBar.show()
                        return@addOnFailureListener
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

    private fun selectImageFromStorage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickFileLauncher.launch(intent)
    }

    override fun showProgressBar() {
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
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