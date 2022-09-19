package com.iodaniel.mobileclass.shared_classes

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.*
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.*
import com.iodaniel.mobileclass.databinding.FragmentViewAssignmentStudentBinding
import com.iodaniel.mobileclass.shared_classes.MultiChoiceQuestionAdapter.ResultDisplayHelpers
import com.iodaniel.mobileclass.shared_classes.MultiChoiceQuestionAdapter.ScrollClickHelpers
import com.iodaniel.mobileclass.student_package.AssignmentViewTypeListener
import com.iodaniel.mobileclass.viewModel.QuestionTransferViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class FragmentStudentViewAssignment : Fragment(), OnClickListener, ScrollClickHelpers, AssignmentViewTypeListener, RemoveHelper, ResultDisplayHelpers {
    private lateinit var binding: FragmentViewAssignmentStudentBinding
    private var multiChoiceQuestionAdapter: MultiChoiceQuestionAdapter = MultiChoiceQuestionAdapter()
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private var dataset: ArrayList<Question> = arrayListOf()
    private val questionTransferViewModel by activityViewModels<QuestionTransferViewModel>()
    private lateinit var assignmentViewTypeListener: AssignmentViewTypeListener
    private lateinit var scrollClickHelpers: ScrollClickHelpers
    private var courseCardData: CourseCardData? = null
    private val fileRecyclerViewAdapter = FileRecyclerViewAdapter()
    private var attachmentAdapter = AttachmentAdapter()
    private var attachmentDataset: ArrayList<String> = arrayListOf()
    private var questionMedia: ArrayList<Map<String, String>> = arrayListOf()
    private val acceptedTypes: ArrayList<String> = arrayListOf("mp4", "3gp", "mp3", "aac", "wav", "pdf", "jpg", "png", "jpeg")
    private val writeExternalPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val permissionGranted = PackageManager.PERMISSION_GRANTED
    private lateinit var question: Question

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        val txt = "Afri-Learn requires permission to show media"
        if (!isGranted) {
            Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).setAction("Grant Permission") {
                requestPermission()
            }
        }
    }

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        try {
            if (it.data!!.data == null) return@ActivityResultCallback
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val dataUri = it.data!!.data
                val contentResolver = requireActivity().contentResolver
                val mime = MimeTypeMap.getSingleton()
                if (mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!! in acceptedTypes) {
                    attachmentDataset.add(dataUri.toString())
                    attachmentAdapter.notifyItemInserted(attachmentDataset.size)
                }
            }
        } catch (e: Exception) {
            println("GetExternal Uri Exception ******************* ${e.printStackTrace()}")
        }
    })

    override fun onStart() {
        super.onStart()
        scrollClickHelpers = this
        assignmentViewTypeListener = this
        //if (ContextCompat.checkSelfPermission(requireContext(), writeExternalPermission) != permissionGranted) requestPermission()
    }

    private fun requestPermission() {
        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun initAttachment() {
        attachmentAdapter.dataset = attachmentDataset
        attachmentAdapter.removeHelper = this
        binding.rvAttachment.adapter = attachmentAdapter
        binding.rvAttachment.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL)
    }

    private fun initDocuments() {
        fileRecyclerViewAdapter.dataset = questionMedia
        fileRecyclerViewAdapter.classCode = courseCardData!!.courseCode
        fileRecyclerViewAdapter.activity = requireActivity()
        binding.rvDocument.adapter = fileRecyclerViewAdapter
        binding.rvDocument.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentViewAssignmentStudentBinding.inflate(inflater, container, false)
        binding.viewAssignmentBackArrow.setOnClickListener(this)
        binding.fileUpload.setOnClickListener(this)
        binding.submitBtn.setOnClickListener(this)
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = Unit
        })
        questionTransferViewModel.question.observe(viewLifecycleOwner) {
            question = it
            questionMedia = question.docStorageLinks
            fileRecyclerViewAdapter.notifyDataSetChanged()
            binding.rvDocument.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
            val failedTxt = "Failed to load Questions"
            when (question.exerciseType) {
                ExerciseType.NORMAL_QUESTION -> assignmentViewTypeListener.questionsOnlyView(question.singleQuestion)
                ExerciseType.DOC_QUESTION -> assignmentViewTypeListener.questionsOnlyView(question.docQuestion)
                ExerciseType.MULTI_QUESTION -> multiChoiceViewFun()
                else -> Snackbar.make(binding.root, failedTxt, Snackbar.LENGTH_INDEFINITE).show()
            }
        }
        questionTransferViewModel.courseCardData.observe(viewLifecycleOwner) {
            courseCardData = it
            initAttachment()
            initDocuments()
        }
        return binding.root
    }

    private fun fetchLastPost() {
        val myAuth = FirebaseAuth.getInstance().currentUser!!.uid
        val courseCode = courseCardData!!.courseCode

        val solutionRef = FirebaseDatabase.getInstance().reference
            .child(SOLUTION_TO_COURSE_EXERCISE)
            .child(courseCardData!!.instructorInChargeUID)
            .child(courseCode)
            .child(myAuth)
        solutionRef.get().addOnSuccessListener {
            if (it.exists()) {
                val d = Gson().fromJson(Gson().toJson(it.value), ArrayList::class.java)
                val solutionList: ArrayList<SolutionData> = arrayListOf()
                for (i in d) {
                    val que = Gson().fromJson(Gson().toJson(i), SolutionData::class.java)
                    solutionList.add(que)
                }
            }
        }

    }

    @SuppressLint("ShowToast")
    private fun multiChoiceViewFun() {
        try {
            assignmentViewTypeListener.multiChoiceView()
            multiChoiceQuestionAdapter.resultDisplayHelpers = this
            multiChoiceQuestionAdapter.dataset = question.multipleChoiceQuestion
            multiChoiceQuestionAdapter.root = binding.root
            multiChoiceQuestionAdapter.activity = requireActivity()

            binding.rvMultipleChoiceStudent.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvMultipleChoiceStudent.adapter = multiChoiceQuestionAdapter

            smoothScroller = LinearSmoothScroller(requireContext())
            PagerSnapHelper().attachToRecyclerView(binding.rvMultipleChoiceStudent)
            multiChoiceQuestionAdapter.scrollClickHelpers = scrollClickHelpers

            binding.rvMultipleChoiceStudent.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    return rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
                }
            })
        } catch (e: Exception) {
            println("Exception ******************************* %${e.printStackTrace()}")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.submit_btn -> {
                val answer = binding.answerInput.text.toString().trim()
                val auth = FirebaseAuth.getInstance().currentUser!!.uid
                if (answer.isEmpty()) {
                    Snackbar.make(binding.root, "Answer cannot be empty", Snackbar.LENGTH_LONG).show()
                    return
                }
                loadingScreen()
                val uploaded = Util.functionTimeout(120_000, uploadSolutionDoc())
                //if (uploaded) snackBar.setText("Uploaded!").show() else snackBar.setText("Error Timeout!!! Retry").show()
            }
            R.id.file_upload -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                pickFileLauncher.launch(intent)
            }
            R.id.view_assignment_back_arrow -> requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun uploadSolutionDoc() {
        val answer = binding.answerInput.text.toString().trim()
        val eventTime = Calendar.getInstance().timeInMillis.toString()
        val myAuth = FirebaseAuth.getInstance().currentUser!!.uid
        val courseCode = courseCardData!!.courseCode
        val storageRef = FirebaseStorage.getInstance().reference.child(SOLUTION_TO_COURSE_EXERCISE)
            .child(courseCardData!!.instructorInChargeUID)
            .child(courseCode)
            .child(myAuth)
            .child(eventTime)

        val arrayDownloadUris = arrayListOf<String>()
        val solutionData = SolutionData(timeSent = eventTime, singleAnswer = answer, uid = myAuth, docLinks = arrayDownloadUris)

        if (attachmentDataset.isNotEmpty()) for (file in attachmentDataset) {
            val fileUri = Uri.parse(file)
            val uploadTask = storageRef.putFile(fileUri)

            uploadTask.continueWith { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener {
                        val downloadUri = it.toString()
                        arrayDownloadUris.add(downloadUri)
                        if (arrayDownloadUris.size == attachmentDataset.size) {
                            solutionData.docLinks = arrayDownloadUris
                            upload(solutionData)
                        }
                    }
                }
            }.addOnFailureListener {
                val txt = "Upload Failed. Please Try again"
                Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                return@addOnFailureListener
            }
        }
        if (attachmentDataset.isEmpty()) upload(solutionData)
    }

    private fun upload(solutionData: SolutionData) {
        val myAuth = FirebaseAuth.getInstance().currentUser!!.uid
        val courseCode = courseCardData!!.courseCode

        val solutionRef = FirebaseDatabase.getInstance().reference
            .child(SOLUTION_TO_COURSE_EXERCISE)
            .child(courseCardData!!.instructorInChargeUID)
            .child(courseCode)
            .child(myAuth)

        solutionRef.get().addOnSuccessListener {
            if (it.exists()) {
                val d = Gson().fromJson(Gson().toJson(it.value), ArrayList::class.java)
                val solutionList: ArrayList<SolutionData> = arrayListOf()
                for (i in d) {
                    val que = Gson().fromJson(Gson().toJson(i), SolutionData::class.java)
                    solutionList.add(que)
                }
                solutionList.add(solutionData)
                solutionRef.setValue(solutionList).addOnSuccessListener {
                    if (activity != null && isAdded) {
                        removeLoadingScreen()
                        Snackbar.make(binding.root, "Upload successful", Snackbar.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    if (activity != null && isAdded) removeLoadingScreen()
                }
            } else {
                solutionRef.setValue(arrayListOf(solutionData)).addOnSuccessListener {
                    if (activity != null && isAdded) {
                        removeLoadingScreen()
                        Snackbar.make(binding.root, "Upload successful", Snackbar.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    if (activity != null && isAdded) removeLoadingScreen()
                }
            }
        }
    }

    override fun smoothScrollHelper(position: Int) {
        smoothScroller.targetPosition = position
        binding.rvMultipleChoiceStudent.layoutManager?.startSmoothScroll(smoothScroller)
    }

    override fun multiChoiceView() {
        binding.viewAssignmentToolbarText.text = "Multiple Choice Questions"
        binding.viewAssignmentResultQuestionRoot.visibility = View.GONE
        binding.viewAssignmentSingleQuestionSection.visibility = View.GONE
        binding.viewAssignmentMultipleQuestionRoot.visibility = View.VISIBLE
    }

    private fun loadingScreen() {
        binding.assignmentLoading.visibility = View.VISIBLE
    }

    private fun removeLoadingScreen() {
        binding.assignmentLoading.visibility = View.GONE
    }

    override fun questionsOnlyView(question: String) {
        binding.viewQuestionQuestion.text = question
        if (this.question.extraNote.isNotEmpty()) {
            binding.viewQuestionExtraNoteHeader.visibility = View.VISIBLE
            binding.viewQuestionExtraNote.text = this.question.extraNote
        }

        val toolBarText = "Questions"
        binding.viewAssignmentToolbarText.text = toolBarText
        binding.viewAssignmentSingleQuestionSection.visibility = View.VISIBLE
        binding.assignmentLoading.visibility = View.GONE
        binding.viewAssignmentResultQuestionRoot.visibility = View.GONE
        binding.viewAssignmentMultipleQuestionRoot.visibility = View.GONE
    }

    override fun fileQuestionsView() {
    }

    override fun resultMultiChoiceView() {
        val toolBarText = "Result"
        binding.viewAssignmentToolbarText.text = toolBarText
        binding.viewAssignmentSingleQuestionSection.visibility = View.GONE
        binding.viewAssignmentMultipleQuestionRoot.visibility = View.GONE
        binding.viewAssignmentResultQuestionRoot.visibility = View.VISIBLE
    }

    override fun onRemove(datum: String) {
        attachmentDataset.remove(datum)
    }

    companion object {
        const val SOLUTION_TO_COURSE_EXERCISE = "SOLUTION_TO_COURSE_EXERCISE"
    }

    override fun displayResult(result: Int) {
        val toolBarText = "Result"
        binding.viewAssignmentToolbarText.text = toolBarText
        binding.viewAssignmentSingleQuestionSection.visibility = View.GONE
        binding.viewAssignmentMultipleQuestionRoot.visibility = View.GONE
        binding.viewAssignmentResultQuestionRoot.visibility = View.VISIBLE

        val overall = dataset.size
        binding.viewAssignmentResultQuestionRoot.visibility = View.VISIBLE
        binding.viewAssignmentBackArrow.setOnClickListener {
            requireActivity().onBackPressed()
        }
        val score = ((result.toFloat() / overall.toFloat()) * 100).toInt().toString() + "%"
        binding.viewAssignmentScore.text = score
        binding.viewAssignmentProgressBar.progress = (score.split("%")[0]).toInt()
    }
}

class AttachmentAdapter : RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {
    var dataset: ArrayList<String> = arrayListOf()
    var listOfDownload: ArrayList<String> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var removeHelper: RemoveHelper
    lateinit var classCode: String
    var listOfMediaNames: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val attachmentText: TextView = itemView.findViewById(R.id.upload_attachment_file_attachment_text)
        val cancel: ImageView = itemView.findViewById(R.id.upload_question_cancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_attachment1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        val extension = dataset[position].split(".").last().split("?")[0]
        val fileName = "${datum}.$extension"
        holder.attachmentText.text = fileName

        /*val helperClass = HelperClass(datum, classCode, extension, context)
        val fullyQualifiedName = "/$classCode/${helperClass.uniqueName}.$extension"
        val proposedDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + fullyQualifiedName
        )*/
        holder.cancel.setOnClickListener {
            removeHelper.onRemove(datum)
            notifyItemRemoved(holder.absoluteAdapterPosition)
        }
    }

    private fun downloadAndOpenFile(datum: String, extension: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val helperClass = HelperClass(datum, classCode, extension, context)
            helperClass.download()
            val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val dir = File(helperClass.requestDownloadPath())
                    listOfDownload.add(dir.toString())

                    //AFTER DOWNLOAD IS DONE
                    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(dir.toString())
                    val dirUri = FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".provider",
                        dir
                    )
                    val viewIntent = Intent(Intent.ACTION_VIEW)
                    viewIntent.setDataAndType(dirUri, mime)
                    viewIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    viewIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.startActivity(viewIntent)
                }
            }
            activity.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    override fun getItemCount(): Int = dataset.size

}

interface RemoveHelper {
    fun onRemove(datum: String)
}

class FileRecyclerViewAdapter : RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder>() {

    var dataset: ArrayList<Map<String, String>> = arrayListOf()
    var listOfDownload: ArrayList<String> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var classCode: String
    var listOfMediaNames: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.upload_attachment_file_attachment_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_attachment2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        val fileName = datum["name"]
        val extension = datum["name"]!!.split(".").last()
        holder.textView.text = fileName

        val helperClass = HelperClass(datum["url"]!!, classCode, extension, context)
        val fullyQualifiedName = "/$classCode/${helperClass.uniqueName}.$extension"
        val proposedDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + fullyQualifiedName
        )
        listOfDownload.add(proposedDir.toString())
        holder.itemView.setOnClickListener {
            try {
                val dir = File(listOfDownload[position])
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(dir.toString())
                val dirUri = FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
                    dir
                )
                if (dir.exists()) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType((dirUri), mime)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.startActivity(intent)
                } else {
                    downloadAndOpenFile(datum["url"]!!, extension)
                }
            } catch (e: Exception) {
                println("Exception ------------------------------------- ${e.printStackTrace()}")
            }
        }
    }

    private fun downloadAndOpenFile(datum: String, extension: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val helperClass = HelperClass(datum, classCode, extension, context)
            helperClass.download()
            val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val dir = File(helperClass.requestDownloadPath())
                    listOfDownload.add(dir.toString())

                    //AFTER DOWNLOAD IS DONE
                    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(dir.toString())
                    val dirUri = FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".provider",
                        dir
                    )
                    val viewIntent = Intent(Intent.ACTION_VIEW)
                    viewIntent.setDataAndType(dirUri, mime)
                    viewIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    viewIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.startActivity(viewIntent)
                }
            }
            activity.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    override fun getItemCount(): Int = dataset.size

}

class MultiChoiceQuestionAdapter : RecyclerView.Adapter<MultiChoiceQuestionAdapter.ViewHolder>() {

    lateinit var resultDisplayHelpers: ResultDisplayHelpers
    lateinit var dataset: ArrayList<MultipleChoice>
    private var solutionSubmitted: ArrayList<String> = arrayListOf()
    private var solutions: ArrayList<String> = arrayListOf()
    lateinit var activity: Activity
    lateinit var context: Context
    lateinit var root: View
    lateinit var scrollClickHelpers: ScrollClickHelpers
    var viewType: String = "student"
    lateinit var snackbar: Snackbar

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioGroup: RadioGroup = itemView.findViewById(R.id.multi_choice_radio_group)
        val question: TextView = itemView.findViewById(R.id.multi_choice_question)
        val instruction: TextView = itemView.findViewById(R.id.multi_choice_instruction)
        val fileCard: CardView = itemView.findViewById(R.id.multi_choice_file_card)
        val fileName: TextView = itemView.findViewById(R.id.multi_choice_file_name)

        val radioA: RadioButton = itemView.findViewById(R.id.multi_choice_a)
        val radioB: RadioButton = itemView.findViewById(R.id.multi_choice_b)
        val radioC: RadioButton = itemView.findViewById(R.id.multi_choice_c)
        val radioD: RadioButton = itemView.findViewById(R.id.multi_choice_d)
        val radioE: RadioButton = itemView.findViewById(R.id.multi_choice_e)

        val next: Button = itemView.findViewById(R.id.multi_choice_row_next_button)
        val previous: Button = itemView.findViewById(R.id.multi_choice_row_prev_button)
        val submit: Chip = itemView.findViewById(R.id.multi_choice_row_submit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        solutions = arrayListOf()
        for (i in 0 until dataset.size) solutions.add(dataset[i].solution)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.multiple_choice_questions_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        when (position) {
            0 -> holder.previous.visibility = View.INVISIBLE
            else -> holder.previous.visibility = View.VISIBLE
        }
        when (position) {
            (dataset.size - 1) -> holder.next.visibility = View.INVISIBLE
            else -> holder.next.visibility = View.VISIBLE
        }
        when (viewType) {
            "student" -> {
                holder.submit.visibility =
                    if (position == dataset.size - 1) View.VISIBLE else View.GONE
            }
            "teacher" -> {
                holder.submit.visibility = View.GONE
                Toast.makeText(context, "View Only", Toast.LENGTH_LONG).show()
            }
        }
        try {
            holder.question.text = datum.question
            holder.instruction.text = datum.instructions
            val options = datum.options
            if (datum.media.isNotEmpty()){
                holder.fileCard.visibility = View.VISIBLE
                holder.fileName.text = datum.media["name"]
            }

            holder.radioA.text = options["A"]
            holder.radioB.text = options["B"]
            holder.radioC.text = options["C"]
            holder.radioD.text = options["D"]
            if (options["E"]!!.isNotEmpty()) {
                holder.radioE.visibility = View.VISIBLE
                holder.radioE.text = options["E"]
            } else holder.radioE.visibility = View.GONE

            holder.radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (position < dataset.size) {
                    scrollClickHelpers.smoothScrollHelper(position + 1)

                    when (checkedId) {
                        R.id.multi_choice_a -> {
                            solutionSubmitted.add("A")
                        }
                        R.id.multi_choice_b -> {
                            solutionSubmitted.add("B")
                        }
                        R.id.multi_choice_c -> {
                            solutionSubmitted.add("C")
                        }
                        R.id.multi_choice_d -> {
                            solutionSubmitted.add("D")
                        }
                        R.id.multi_choice_e -> {
                            solutionSubmitted.add("E")
                        }
                    }
                }
            }

            holder.next.setOnClickListener {
                if (position < dataset.size - 1) scrollClickHelpers.smoothScrollHelper(position + 1)
            }

            holder.previous.setOnClickListener {
                if (position > 0) {
                    solutionSubmitted.removeLast()
                    scrollClickHelpers.smoothScrollHelper(position - 1)
                }
            }

            holder.submit.setOnClickListener {
                val txt = "Pick an Option"
                if (solutionSubmitted.size < solutions.size)
                    Snackbar.make(holder.itemView, txt, Snackbar.LENGTH_LONG).show() else submit()
            }
        } catch (e: Exception) {
            println("EXCEPTION ******************************** $e")
        }
    }

    private fun submit() {
        try {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val result = evaluateAssessment()
                val activityX = (activity as FragmentActivity)
                if (activity != null)activity.runOnUiThread { resultDisplayHelpers.displayResult(result) }
            }
        } catch (e: Exception) {
            println("EXCEPTION ******************************** ${e.printStackTrace()}")
        }
    }

    private fun evaluateAssessment(): Int {
        var correctQuestions = 0
        for (i in 0 until dataset.size) if (solutions[i] == solutionSubmitted[i]) correctQuestions++
        return correctQuestions
    }

    override fun getItemCount(): Int = dataset.size

    interface ScrollClickHelpers {
        fun smoothScrollHelper(position: Int)
    }

    interface ResultDisplayHelpers {
        fun displayResult(result: Int)
    }
}

class SingleQuestionMediaAdapter : RecyclerView.Adapter<SingleQuestionMediaAdapter.ViewHolder>() {
    var courseCardDataJson: String = ""
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var plansAndModulesList: ArrayList<PlanModulesExercise>
    var modulePosition = 0
    var data: ModuleData = ModuleData()
    private val imagesExtList: ArrayList<String> = arrayListOf("jpg", "png", "jpeg")
    var dataset: ArrayList<MutableMap<String, String>> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: Chip = itemView.findViewById(R.id.row_module_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        dataset = data.uris
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_module, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.chip.text = datum["filename"]
    }

    override fun getItemCount() = dataset.size
}