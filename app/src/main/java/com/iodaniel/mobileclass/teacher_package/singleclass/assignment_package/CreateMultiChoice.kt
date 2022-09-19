package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.ExerciseType
import com.iodaniel.mobileclass.data_class.MultipleChoice
import com.iodaniel.mobileclass.data_class.Question
import com.iodaniel.mobileclass.databinding.FragmentCreateMultiChoiceBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.home.ActivityLandingPage
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.teacher_package.singleclass.DataAndPositionViewModel
import kotlinx.serialization.json.*
import java.util.*

class CreateMultiChoice : Fragment(), View.OnClickListener, ProgressBarController {

    private lateinit var binding: FragmentCreateMultiChoiceBinding
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var dialog: Dialog
    private lateinit var progressBarController: ProgressBarController
    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
    private var listOfMedia: ArrayList<Map<String, String>> = arrayListOf()
    private var media: MutableMap<String, String> = mutableMapOf()
    private var fileName = ""
    private var alpha = 0
    private var listOfQuestion: ArrayList<MultipleChoice> = arrayListOf()
    private lateinit var cn: InternetConnection
    private var pmeRef = FirebaseDatabase.getInstance().reference
    private val dataAndPositionViewModel by activityViewModels<DataAndPositionViewModel>()
    private var exercisePosition = 0
    private var courseCardData: CourseCardData? = null

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        try {
            if (it.data!!.data == null) return@ActivityResultCallback

            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val dataUri = it.data!!.data
                fileName = com.iodaniel.mobileclass.util.Util.getFileName(dataUri!!)
                binding.multichoiceMedia.text = fileName
                media = mutableMapOf("name" to fileName, "url" to dataUri.toString())
            }
        } catch (e: Exception) {
            println("GetExternal Uri Exception ******************* ${e.printStackTrace()}")
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateMultiChoiceBinding.inflate(inflater, container, false)
        dataAndPositionViewModel.dataAndPosition.observe(viewLifecycleOwner) {
            exercisePosition = it.second
            courseCardData = it.first
            cn = InternetConnection(requireContext())
            initialiseAllClassInterface()
            pmeRef = pmeRef.child(getString(R.string.pme_ref))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(courseCardData!!.courseCode)
                .child(exercisePosition.toString())
                .child("exercise")
                .child("questions")
        }

        try {
            val jsonArray = requireArguments().getString("json", "")
            if (jsonArray != "") {
                val gsonArray: JsonElement = Json.parseToJsonElement(jsonArray!!)
                val sonArray = Json.encodeToJsonElement(gsonArray)
                for (i in sonArray as JsonArray) {
                    val data: MultipleChoice = Json.decodeFromJsonElement(i)
                    listOfQuestion.add(data)
                }
            }
        } catch (e: Exception) {
        }

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
        val allAvailableOptions = arrayListOf("A", "B", "C", "D", "E")
        val sandbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        val question = binding.multiChoiceQuestion.text.toString().trim()
        val instruction = binding.multiChoiceQuestionAssignmentInstruction.text.toString().trim()
        val extraNote = binding.multiChoiceExtraNote.text.toString().trim()
        val optionA = binding.optionA.text.toString().trim()
        val optionB = binding.optionB.text.toString().trim()
        val optionC = binding.optionC.text.toString().trim()
        val optionD = binding.optionD.text.toString().trim()
        val optionE = binding.optionE.text.toString().trim()

        val solution = binding.multiChoiceQuestionSolution.text.toString().trim()

        when ("") {
            solution -> {
                sandbar.setText("No solution set").show()
                return
            }
            question -> {
                sandbar.setText("Empty question").show()
                return
            }
            instruction -> {
                sandbar.setText("Empty instruction").show()
                return
            }
            optionA -> {
                sandbar.setText("Empty option at A").show()
                return
            }
            optionB -> {
                sandbar.setText("Empty option at B").show()
                return
            }
            optionC -> {
                sandbar.setText("Empty option at C").show()
                return
            }
            optionD -> {
                sandbar.setText("Empty option at D").show()
                return
            }
            optionE -> {
                if (binding.eLayout.visibility == View.VISIBLE) {
                    sandbar.setText("Empty option at A").show()
                    return
                }
            }
        }

        if (solution !in allAvailableOptions) {
            val txt = "Solution not found in options!\nReview options and solution."
            Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
            return
        }

        val options = mapOf("A" to optionA, "B" to optionB, "C" to optionC, "D" to optionD, "E" to optionE)
        val multipleChoice = MultipleChoice(question = question, options = options, solution = solution, media = media, extraNote = extraNote)
        listOfQuestion.add(multipleChoice)
        val dateTime = Calendar.getInstance().timeInMillis.toString()
        val assignmentQuestion = Question(timeCreated = dateTime, multipleChoiceQuestion = listOfQuestion, exerciseType = ExerciseType.MULTI_QUESTION)

        val uploadableMedia: ArrayList<String> = arrayListOf()
        for (i in assignmentQuestion.multipleChoiceQuestion) {
            if (i.media.isNotEmpty()) uploadableMedia.add(i.media["url"]!!)
        }

        val listOfUploadedMedia: ArrayList<String> = arrayListOf()
        for (i in assignmentQuestion.multipleChoiceQuestion) {
            val storageRef = Firebase.storage.reference
                .child(getString(R.string.pme_ref))
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(courseCardData!!.courseCode)
                .child(exercisePosition.toString())
                .child("exercise")
                .child("questions")
            if (uploadableMedia.isNotEmpty() && i.media.isNotEmpty()) {
                val uploadTask = storageRef.putFile(Uri.parse(i.media["url"]))
                uploadTask.continueWith { task ->
                    if (!task.isSuccessful) task.exception?.let { throw it }
                    storageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener {
                            val downloadUri = it.toString()
                            i.media["url"] = downloadUri
                            listOfUploadedMedia.add(downloadUri)
                            if (listOfUploadedMedia.size == uploadableMedia.size) {
                                finalUpload(assignmentQuestion)
                            }
                        }
                    }
                }.addOnFailureListener {
                    progressBarController.hideProgressBar()
                    val txt = "Upload Failed. Please Try again"
                    Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                    return@addOnFailureListener
                }
            } else if (uploadableMedia.isEmpty()) finalUpload(assignmentQuestion)
        }
        progressBarController.showProgressBar()
    }

    private fun finalUpload(assignmentQuestion: Question) {
        pmeRef.get().addOnSuccessListener {
            if (it.exists()) {
                val d = Gson().fromJson(Gson().toJson(it.value), ArrayList::class.java)
                val questionList: ArrayList<Question> = arrayListOf()
                for (i in d) {
                    val que = Gson().fromJson(Gson().toJson(i), Question::class.java)
                    questionList.add(que)
                }
                questionList.add(assignmentQuestion)
                pmeRef.setValue(questionList).addOnCompleteListener {
                    Snackbar.make(binding.root, "Uploaded successfully", Snackbar.LENGTH_LONG).show()
                    val intent = Intent(requireContext(), ActivityLandingPage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    requireActivity().onBackPressed()
                    progressBarController.hideProgressBar()
                }.addOnFailureListener {
                    progressBarController.hideProgressBar()
                    Snackbar.make(binding.root, "Error occurred!!!", Snackbar.LENGTH_LONG).show()
                }
            } else {
                pmeRef.setValue(arrayListOf(assignmentQuestion)).addOnCompleteListener {
                    Snackbar.make(binding.root, "Uploaded successfully", Snackbar.LENGTH_LONG).show()
                    val intent = Intent(requireContext(), ActivityLandingPage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    requireActivity().onBackPressed()
                    progressBarController.hideProgressBar()
                }.addOnFailureListener {
                    progressBarController.hideProgressBar()
                    Snackbar.make(binding.root, "Error occurred!!!", Snackbar.LENGTH_LONG).show()
                }
            }
        }
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
            if (binding.eLayout.visibility == View.VISIBLE) {
                binding.eLayout.visibility = View.GONE
                binding.createExtraOptionText.text = "Create Extra Option"
            } else if (binding.eLayout.visibility == View.GONE) {
                binding.eLayout.visibility = View.VISIBLE
                binding.createExtraOptionText.text = "Remove Extra Option"
            }
        } catch (e: Exception) {
        }
    }

    private fun saveDataAndNext(listOfQuestion: ArrayList<MultipleChoice>) {
        val allAvailableOptions = arrayListOf("A", "B", "C", "D", "E")
        val sandbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        val question = binding.multiChoiceQuestion.text.toString().trim()
        val instruction = binding.multiChoiceQuestionAssignmentInstruction.text.toString().trim()
        val extraNote = binding.multiChoiceExtraNote.text.toString().trim()
        val optionA = binding.optionA.text.toString().trim()
        val optionB = binding.optionB.text.toString().trim()
        val optionC = binding.optionC.text.toString().trim()
        val optionD = binding.optionD.text.toString().trim()
        val optionE = binding.optionE.text.toString().trim()

        val solution = binding.multiChoiceQuestionSolution.text.toString().trim()

        when ("") {
            solution -> {
                sandbar.setText("No solution set").show()
                return
            }
            question -> {
                sandbar.setText("Empty question").show()
                return
            }
            instruction -> {
                sandbar.setText("Empty instruction").show()
                return
            }
            optionA -> {
                sandbar.setText("Empty option at A").show()
                return
            }
            optionB -> {
                sandbar.setText("Empty option at B").show()
                return
            }
            optionC -> {
                sandbar.setText("Empty option at C").show()
                return
            }
            optionD -> {
                sandbar.setText("Empty option at D").show()
                return
            }
            optionE -> {
                if (binding.eLayout.visibility == View.VISIBLE) {
                    sandbar.setText("Empty option at A").show()
                    return
                }
            }
        }

        if (solution !in allAvailableOptions) {
            val txt = "Solution not found in options!\nReview options and solution."
            Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
            return
        }

        val options = mapOf("A" to optionA, "B" to optionB, "C" to optionC, "D" to optionD, "E" to optionE)
        val multipleChoice = MultipleChoice(question = question, options = options, solution = solution, media = media, extraNote = extraNote)
        listOfQuestion.add(multipleChoice)

        this.listOfQuestion = listOfQuestion
        val fragment = CreateMultiChoice()
        val jsonArray = Gson().toJsonTree(listOfQuestion)
        val bundle = Bundle()
        bundle.putString("json", jsonArray.toString())
        fragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(listOfQuestion.size.toString())
            .replace(R.id.multi_choice_root, fragment)
            .commit()
    }

    private fun previousData() {
        if (listOfQuestion.isNotEmpty()) requireActivity().supportFragmentManager.popBackStack()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.multi_choice_submit -> {
                if (cn != null) {
                    cn.setCustomInternetListener(object :
                        InternetConnection.CheckInternetConnection {
                        override fun isConnected() = submit()

                        override fun notConnected() {
                            val txt = "No active internet!!! Retry"
                            Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                        }
                    })
                } else {
                    Snackbar.make(binding.root, "Retry", Snackbar.LENGTH_LONG).show()
                }
            }
            R.id.multi_choice_previous -> {
                previousData()
            }
            R.id.multi_choice_next -> {
                saveDataAndNext(listOfQuestion)
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