package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentCreateMultiChoiceBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.*
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion
import java.util.ArrayList

class CreateMultiChoice(private val classInfo: ClassInfo) : Fragment(), View.OnClickListener,
    ProgressBarController,
    MediaSupport {

    private lateinit var binding: FragmentCreateMultiChoiceBinding
    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
        .child("multi_choice_assignments")
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child(classInfo.classCode)
        .push()
    private var listOfMedia: ArrayList<String> = arrayListOf()
    private lateinit var progressBarController: ProgressBarController
    private lateinit var mediaSupport: MediaSupport
    private var fileName = ""
    private var alpha = 0

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
                        fileName =
                            dataUri.toString().substring(dataUri.toString().lastIndexOf("/") + 1)
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
                                binding.multichoiceMedia.text = fileName
                                listOfMedia.add(dataUri.toString())
                            }
                            "3gp" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.videoPlayer(dataUri!!)
                                binding.multichoiceMedia.text = fileName
                                listOfMedia.add(dataUri.toString())
                            }
                            "mp3" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                binding.multichoiceMedia.text = fileName
                                listOfMedia.add(dataUri.toString())
                            }
                            "aac" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                binding.multichoiceMedia.text = fileName
                                listOfMedia.add(dataUri.toString())
                            }
                            "wav" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                binding.multichoiceMedia.text = fileName
                                listOfMedia.add(dataUri.toString())
                            }
                            "pdf" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.pdfReader(dataUri!!)
                                binding.multichoiceMedia.text = fileName
                                listOfMedia.add(dataUri.toString())
                            }
                            "jpg" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.imageReader(dataUri!!)
                                binding.multichoiceMedia.text = fileName
                                listOfMedia.add(dataUri.toString())
                            }
                            "png" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.imageReader(dataUri!!)
                                binding.multichoiceMedia.text = fileName
                                listOfMedia.add(dataUri.toString())
                            }
                            "jpeg" -> {
                                println("ACTIVITY RESULT ******************** $extensionType")
                                mediaSupport.imageReader(dataUri!!)
                                binding.multichoiceMedia.text = fileName
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
        binding = FragmentCreateMultiChoiceBinding.inflate(inflater, container, false)
        initialiseAllClassInterface()
        return binding.root
    }

    private fun initialiseAllClassInterface() {
        binding.multiChoiceSubmit.setOnClickListener(this)
        binding.multiChoiceQuestionFileChooser.setOnClickListener(this)
        binding.addNewOption.setOnClickListener(this)
        progressBarController = this
        mediaSupport = this
    }

    override fun videoPlayer(uri: Uri) = try {
        print("URI videoPlayer ******************* $uri")
        binding.multichoiceMedia.text = uri.toString()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun pdfReader(uri: Uri) = try {
        print("URI pdfReader ******************* $uri")
        binding.multichoiceMedia.text = uri.toString()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun musicReader(uri: Uri) {
        print("URI musicReader ******************* $uri")
        binding.multichoiceMedia.text = uri.toString()
    }

    override fun imageReader(uri: Uri) = try {
        binding.multichoiceMedia.text = uri.toString()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun makeMediaPlayersInvisible() {

    }

    private fun submit() {
        val instructions = binding.multiChoiceQuestionAssignmentInstruction.text.toString().trim()
        val question = binding.multiChoiceQuestion.text.toString().trim()
        val option1 = binding.option1.text.toString().trim()
        val solution = binding.multiChoiceQuestionSolution.text.toString().trim()
        val link = binding.multiChoiceExtraLink.text.toString().trim()
        if (question == "") return
        if (option1 == "") return
        if (solution == "") return

        //val multiChoice = MultiChoiceQuestion( className = classInfo.className, classCode = classInfo.classCode, teacherInChargeName = classInfo.teacherInChargeName, teacherInChargeUID = classInfo.teacherInChargeUID, )

    }

    //multiChoiceRef.setValue()
    private fun selectFileFromStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        pickFileLauncher.launch(intent)
    }

    private fun createOption() {
        try {
            val alphabets = "BCDEFGHIJKLMNOPQRSTUVWXYZ"
            val op = "(${alphabets[alpha]})"

            val options = EditText(activity)
            options.layoutParams = ViewGroup.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
            options.hint = "Option ${alphabets[alpha]}"
            options.setPadding(60, 0, 80, 20)
            options.gravity = Gravity.CENTER

            val optionsText = TextView(activity)
            optionsText.layoutParams =
                ViewGroup.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT)
            optionsText.textSize = 22F
            optionsText.text = op

            val linearLayout = LinearLayout(activity)
            linearLayout.layoutParams =
                ViewGroup.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT)
            linearLayout.addView(optionsText, 0)
            linearLayout.addView(options, 1)

            val viewIndex = binding.options.childCount
            binding.options.addView(linearLayout, viewIndex)
        } catch (e: Exception) {
            println("************************** ${e.printStackTrace()}")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.multi_choice_submit -> {
                submit()
            }
            R.id.multi_choice_question_file_chooser -> {
                selectFileFromStorage()
            }
            R.id.add_new_option -> {
                createOption()
                alpha += 1
            }
        }
    }

    override fun showProgressBar() {

    }

    override fun hideProgressBar() {

    }
}