package com.iodaniel.mobileclass.teacher_package.classes

import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.databinding.ClassUploadBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

class ClassUpload : AppCompatActivity(), View.OnClickListener, ProgressBarController {

    private val binding by lazy { ClassUploadBinding.inflate(layoutInflater) }
    private var reference = FirebaseDatabase.getInstance().reference
    private var allClassesRef = FirebaseDatabase.getInstance()
        .reference
        .child("class_codes")
        .push()
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var progressBarController: ProgressBarController
    private val dialog by lazy { Dialog(this) }
    private var classImage: String = ""
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var errorSnackBar: Snackbar
    private val customImages = arrayListOf(R.drawable.study1, R.drawable.study2, R.drawable.study4)
    private lateinit var cn: InternetConnection

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                try {
                    if (it.data!!.data == null) {
                        return@ActivityResultCallback
                    }
                    if (it.resultCode == RESULT_OK) {
                        val dataUri = it.data!!.data
                        val contentResolver = applicationContext.contentResolver
                        val mime = MimeTypeMap.getSingleton()
                        val extensionType =
                            mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
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
        val txt = "Upload Failed. Please Try again"
        cn = InternetConnection(applicationContext)
        errorSnackBar = Snackbar.make(
            binding.root, txt, Snackbar.LENGTH_LONG
        )
        initialiseAllClassInterface()
        initialiseUtils()
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

        val datetime = Calendar.getInstance().time.time
        val dateString = DateFormat.getInstance().format(datetime)
        val split = dateString.split(' ')
        val date = split[0].trim()
        val time = split[1].trim() + split[2].trim()
        val className = binding.uploadClassName.text.toString().trim()

        val splita = UUID.randomUUID().toString().split("-")
        val classCode = splita[1] + splita[2].substring(0, 1) + splita[4].substring(6, 8)

        if (className == "") {
            snackBar.setText("Empty Class Name!!!"); snackBar.show()
            progressBarController.hideProgressBar()
            return
        }
        if (classImage == "") {
            val rand = (Math.random() * 2).roundToInt()
            classImage = (Uri.Builder())
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(customImages[rand]))
                .appendPath(resources.getResourceTypeName(customImages[rand]))
                .appendPath(resources.getResourceEntryName(customImages[rand]))
                .build().toString()
        }

        val contentResolver = applicationContext.contentResolver
        val mime = MimeTypeMap.getSingleton()

        val extension = when (classImage.split(".")[0]) {
            "android" -> {
                "jpg"
            }
            else -> {
                mime.getExtensionFromMimeType(contentResolver?.getType(Uri.parse(classImage)))!!
            }
        }
        val event = (date + time + classImage).replace("//", ".").replace("/", ".")
        val finalStorageRef = storageRef.child("$event.$extension")
        val uploadTask = finalStorageRef.putFile(Uri.parse(classImage))

        uploadTask.continueWith { task ->
            if (!task.isSuccessful) task.exception?.let { throw it }
            finalStorageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finalStorageRef.downloadUrl.addOnSuccessListener {
                    val splitc = UUID.randomUUID().toString().split("-")
                    val codee = splitc[1] + splitc[2].substring(0, 1) + splitc[4].substring(6, 8)
                    val classCodeX = codee
                    val downloadUri = it.toString()
                    val rand = Random(42).nextInt(10, 255)
                    reference = reference
                        .child("teacher")
                        .child(auth)
                        .child("classes")
                        .child(classCodeX)

                    val detailsX = hashMapOf(
                        "classCode" to classCodeX,
                        "auth" to auth
                    )

                    val classInfoX = ClassInfo()
                    classInfoX.className = className
                    classInfoX.time = time
                    classInfoX.red = rand
                    classInfoX.green = rand
                    classInfoX.blue = rand
                    classInfoX.classCodePushId = allClassesRef.key!!
                    classInfoX.classCode = classCodeX
                    classInfoX.classImage = downloadUri
                    classInfoX.teacherInChargeUID = auth
                    classInfoX.datetime = datetime.toString()

                    allClassesRef.setValue(detailsX).addOnCompleteListener {
                        reference.setValue(classInfoX).addOnCompleteListener {
                            progressBarController.hideProgressBar()
                            onBackPressed()
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
        val paramWrap = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.setLayout(paramWrap, paramWrap)
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
                if (cn != null) {
                    cn.setCustomInternetListener(object :
                        InternetConnection.CheckInternetConnection {
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
    }
}