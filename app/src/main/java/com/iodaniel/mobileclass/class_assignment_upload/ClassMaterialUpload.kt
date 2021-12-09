package com.iodaniel.mobileclass.class_assignment_upload

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.class_assignment_upload.ClassMaterialUploadInterface.progressBarController
import com.iodaniel.mobileclass.class_assignment_upload.`class`.Classes
import com.iodaniel.mobileclass.databinding.ActivityClassMaterialUploadBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import java.text.DateFormat
import java.time.Instant
import java.util.*

class ClassMaterialUpload : AppCompatActivity(), View.OnClickListener, progressBarController {

    private val binding by lazy { ActivityClassMaterialUploadBinding.inflate(layoutInflater) }
    private var reference = FirebaseDatabase.getInstance().reference
    private lateinit var progressBarController: progressBarController
    private val dialog by lazy { Dialog(this) }
    private lateinit var snackbar: Snackbar
    private var courseName: String = ""

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                println("ACTIVITY RESULT ******************** ${it.data}")
                try {
                    binding.uploadFile.setImageURI(it.data!!.data)
                } catch (e: Exception) {
                    print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
                }
            })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Upload Class Material"
        initialiseDatabase()
        initialiseAllClassInterface()
        initialiseUtils()
        if (intent.hasExtra("class_name"))
            courseName = intent.getStringExtra("class_name")!!
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
    }

    private fun initialiseUtils() {
        binding.uploadButton.setOnClickListener(this)
        binding.uploadLink.setOnClickListener(this)
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


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.upload_link -> {
                selectFileFromStorage()
            }
            R.id.upload_button -> {
                checkInput()
            }
        }
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
}