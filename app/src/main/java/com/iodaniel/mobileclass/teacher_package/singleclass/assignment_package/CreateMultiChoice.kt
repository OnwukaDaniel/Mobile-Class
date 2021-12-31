package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentCreateMultiChoiceBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.*
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion
import java.util.*

class CreateMultiChoice(
    private val classInfo: ClassInfo,
    private val arrayOfQuestions: ArrayList<MultiChoiceQuestion> = arrayListOf(),
) : Fragment(), View.OnClickListener,
    ProgressBarController {

    private lateinit var binding: FragmentCreateMultiChoiceBinding
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var dialog: Dialog
    private lateinit var progressBarController: ProgressBarController
    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
        .child("multi_choice_question")
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child(classInfo.classCode)
        .push()
    private var listOfMedia: ArrayList<String> = arrayListOf()
    private var fileName = ""
    private var alpha = 0

    private val pickFileLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
                try {
                    if (it.data!!.data == null) {
                        return@ActivityResultCallback
                    }
                    if (it.resultCode == AppCompatActivity.RESULT_OK) {
                        val dataUri = it.data!!.data
                        fileName =
                            dataUri.toString().substring(dataUri.toString().lastIndexOf("/") + 1)
                        binding.multichoiceMedia.text = fileName
                        listOfMedia.add(dataUri.toString())

                        val mime = MimeTypeMap.getSingleton()
                        val contentResolver = requireActivity().contentResolver
                        val extensionType =
                            mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
                    }
                } catch (e: Exception) {
                    println("GetExternal Uri Exception ******************* ${e.printStackTrace()}")
                }
            }
        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateMultiChoiceBinding.inflate(inflater, container, false)
        initialiseAllClassInterface()
        return binding.root
    }

    private fun initialiseAllClassInterface() {
        binding.multiChoiceSubmit.setOnClickListener(this)
        binding.multiChoicePrevious.setOnClickListener(this)
        binding.multiChoiceNext.setOnClickListener(this)
        binding.multiChoiceQuestionFileChooser.setOnClickListener(this)
        binding.addNewOption.setOnClickListener(this)
        dialog = Dialog(requireContext())
        progressBarController = this
    }

    private fun getAllOptions(): ArrayList<String> {
        val optionsArrayList = arrayListOf<String>()
        optionsArrayList.add(binding.optionA.text.toString().trim())
        optionsArrayList.add(binding.optionB.text.toString().trim())
        optionsArrayList.add(binding.optionC.text.toString().trim())
        optionsArrayList.add(binding.optionD.text.toString().trim())
        if (binding.eLayout.visibility == View.VISIBLE)
            optionsArrayList.add(binding.optionE.text.toString().trim())
        return optionsArrayList
    }

    private fun submit() {
        val allAvailableOptions = arrayListOf("A", "B", "C", "D")
        val optionsArray = getAllOptions()
        val instructions = binding.multiChoiceQuestionAssignmentInstruction.text.toString().trim()
        val question = binding.multiChoiceQuestion.text.toString().trim()
        val optionA = binding.optionA.text.toString().trim()
        val optionB = binding.optionB.text.toString().trim()
        val optionC = binding.optionC.text.toString().trim()
        val optionD = binding.optionD.text.toString().trim()
        val optionE = if (binding.optionE.visibility == View.VISIBLE) {
            allAvailableOptions.add("E")
            binding.optionE.text.toString().trim()
        } else ""
        if (binding.optionE.visibility == View.VISIBLE && optionE == "") return

        val solution = binding.multiChoiceQuestionSolution.text.toString().trim()
        val extraNote = binding.multiChoiceExtraNote.text.toString().trim()
        if (question == "") return
        if (optionA == "" || optionB == "" || optionC == "" || optionD == "") return
        if (solution == "") {
            Snackbar.make(binding.root, "No solution set!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (solution !in allAvailableOptions) {
            val txt = "Solution not found in option!\nReview options and solution."
            Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
            return
        }

        if (optionsArray.isEmpty()) return
        progressBarController.showProgressBar()

        val dateTime = Calendar.getInstance().time.time.toString()
        val arrayDownloadUris = arrayListOf<String>()

        if (listOfMedia.isEmpty()) {
            val multiChoice = MultiChoiceQuestion()
            multiChoice.classCode = classInfo.classCode
            multiChoice.teacherInChargeName = classInfo.teacherInChargeName
            multiChoice.teacherInChargeUID = classInfo.teacherInChargeUID
            multiChoice.datetime = dateTime
            multiChoice.instructions = instructions
            multiChoice.question = question
            multiChoice.solution = solution
            multiChoice.mediaUris = arrayListOf()
            multiChoice.extraNote = extraNote
            multiChoice.options = optionsArray
            arrayOfQuestions.add(multiChoice)

            println("arrayOfQuestions.toList() ********************* ${arrayOfQuestions.toList()}")
            multiChoiceRef.setValue(arrayOfQuestions.toList()).addOnCompleteListener {
                Snackbar.make(binding.root, "Uploaded successfully", Snackbar.LENGTH_LONG).show()
                requireActivity().supportFragmentManager.popBackStack(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                progressBarController.hideProgressBar()
            }.addOnFailureListener {
                progressBarController.hideProgressBar()
                Snackbar.make(binding.root, "Error occurred!!!", Snackbar.LENGTH_LONG).show()
            }
            return
        }

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

                            val multiChoice = MultiChoiceQuestion()
                            multiChoice.classCode = classInfo.classCode
                            multiChoice.teacherInChargeName = classInfo.teacherInChargeName
                            multiChoice.teacherInChargeUID = classInfo.teacherInChargeUID
                            multiChoice.datetime = dateTime
                            multiChoice.instructions = instructions
                            multiChoice.question = question
                            multiChoice.solution = solution
                            multiChoice.mediaUris = arrayListOf()
                            multiChoice.extraNote = extraNote
                            multiChoice.options = optionsArray
                            arrayOfQuestions.add(multiChoice)

                            multiChoiceRef.setValue(arrayOfQuestions.toList())
                                .addOnCompleteListener {
                                    requireActivity().supportFragmentManager.popBackStack(
                                        null,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                                    )
                                    val text = "Uploaded successfully"
                                    Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).show()
                                    progressBarController.hideProgressBar()
                                }.addOnFailureListener {
                                    progressBarController.hideProgressBar()
                                    val text = "Error occurred!!!"
                                    Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).show()
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

    private fun submitX() {
        val optionsArray = getAllOptions()
        val instructions = binding.multiChoiceQuestionAssignmentInstruction.text.toString().trim()
        val question = binding.multiChoiceQuestion.text.toString().trim()
        val solution = binding.multiChoiceQuestionSolution.text.toString().trim()
        val extraNote = binding.multiChoiceExtraNote.text.toString().trim()
        if (question == "") return
        if (solution == "") return
        progressBarController.showProgressBar()

        val dateTime = Calendar.getInstance().time.time.toString()
        val arrayDownloadUris = arrayListOf<String>()

        if (listOfMedia.isEmpty()) {
            val multiChoice = MultiChoiceQuestion()
            multiChoice.classCode = classInfo.classCode
            multiChoice.teacherInChargeName = classInfo.teacherInChargeName
            multiChoice.teacherInChargeUID = classInfo.teacherInChargeUID
            multiChoice.datetime = dateTime
            multiChoice.instructions = instructions
            multiChoice.question = question
            multiChoice.solution = solution
            multiChoice.mediaUris = arrayListOf()
            multiChoice.extraNote = extraNote
            multiChoice.options = optionsArray
            progressBarController.hideProgressBar()
            saveDataAndNext(multiChoice)
            return
        }

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

                            val multiChoice = MultiChoiceQuestion()
                            multiChoice.classCode = classInfo.classCode
                            multiChoice.teacherInChargeName = classInfo.teacherInChargeName
                            multiChoice.teacherInChargeUID = classInfo.teacherInChargeUID
                            multiChoice.datetime = dateTime
                            multiChoice.instructions = instructions
                            multiChoice.question = question
                            multiChoice.solution = solution
                            multiChoice.mediaUris = arrayListOf()
                            multiChoice.extraNote = extraNote
                            multiChoice.options = optionsArray
                            progressBarController.hideProgressBar()
                            saveDataAndNext(multiChoice)
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

    //multiChoiceRef.setValue()
    private fun selectFileFromStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "images/*"
        pickFileLauncher.launch(intent)
    }

    private fun createOption() {
        try {
            if (binding.eLayout.visibility == View.VISIBLE) {
                binding.eLayout.visibility = View.GONE
                binding.createExtraOptionText.text = "Create Extra Option"
            } else if (binding.eLayout.visibility == View.GONE) {
                binding.eLayout.visibility = View.VISIBLE
                binding.createExtraOptionText.text = "Remove Extra Option"
            }
        } catch (e: Exception) {
            println("************************** ${e.printStackTrace()}")
        }
    }

    private fun saveDataAndNext(list: MultiChoiceQuestion) {
        arrayOfQuestions.add(list)
        println("ARRAY OF QUESTIONS SIZE ************************ ${arrayOfQuestions}")
        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(arrayOfQuestions.size.toString() + list.datetime)
            .replace(R.id.multi_choice_root, CreateMultiChoice(classInfo, arrayOfQuestions))
            .commit()
    }

    private fun previousData() {
        arrayOfQuestions.removeLast()
        requireActivity().supportFragmentManager.popBackStack()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.multi_choice_submit -> {
                submit()
            }
            R.id.multi_choice_previous -> {
                previousData()
            }
            R.id.multi_choice_next -> {
                submitX()
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
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()

    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }
}