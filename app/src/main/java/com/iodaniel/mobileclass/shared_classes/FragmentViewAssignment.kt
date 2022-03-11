package com.iodaniel.mobileclass.student_package

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentViewAssignmentStudentBinding
import com.iodaniel.mobileclass.shared_classes.HelperClass
import com.iodaniel.mobileclass.student_package.MultiChoiceQuestionAdapter.ScrollClickHelpers
import com.iodaniel.mobileclass.teacher_package.classes.AssignmentQuestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FragmentViewAssignment : Fragment(), OnClickListener, ScrollClickHelpers,
    AssignmentViewTypeListener {

    private lateinit var binding: FragmentViewAssignmentStudentBinding
    private var adapter: MultiChoiceQuestionAdapter = MultiChoiceQuestionAdapter()
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private var dataset: ArrayList<AssignmentQuestion> = arrayListOf()
    private lateinit var assignmentViewTypeListener: AssignmentViewTypeListener
    lateinit var scrollClickHelpers: ScrollClickHelpers
    private val writeExternalPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val permissionGranted = PackageManager.PERMISSION_GRANTED

    private lateinit var assignmentQuestion: AssignmentQuestion

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val txt = "Afri-Learn requires permission to show media"
            if (isGranted) {
                showViews()
            } else {
                Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG)
                    .setAction("Grant Permission") {
                        requestPermission()
                    }
            }
        }

    override fun onStart() {
        super.onStart()
        scrollClickHelpers = this
        assignmentViewTypeListener = this
        binding.viewAssignmentBackArrow.setOnClickListener(this)

        requireActivity().onBackPressedDispatcher.addCallback(object :
            OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
            }
        })
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                writeExternalPermission
            ) == permissionGranted
        ) showViews()
        else requestPermission()
    }

    private fun requestPermission() {
        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentViewAssignmentStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun showViews() {
        val failedTxt = "Failed to load Questions"
        val infoJson = requireArguments().getString("questionData")!!
        assignmentQuestion = Gson().fromJson(infoJson, AssignmentQuestion::class.java)
        when (assignmentQuestion.questionType) {
            getString(R.string.DIRECTQUESTION) -> assignmentViewTypeListener.questionsOnlyView()
            getString(R.string.DOCUMENTQUESTION) -> assignmentViewTypeListener.questionsOnlyView()
            getString(R.string.MULTIPLECHOICEQUESTION) -> multiChoiceViewFun()
            else -> Snackbar.make(binding.root, failedTxt, Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    @SuppressLint("ShowToast")
    private fun multiChoiceViewFun() {
        try {
            /*if (bundle.getString("viewType", "") == "teacher") {
                adapter.viewType = "teacher"
                Toast.makeText(context, "View Only!!!", Toast.LENGTH_LONG).show()
            }*/
            assignmentViewTypeListener.multiChoiceView()
            adapter.dataset = assignmentQuestion.arrAssignment
            adapter.root = binding.root
            adapter.activity = requireActivity()

            binding.rvMultipleChoiceStudent.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvMultipleChoiceStudent.adapter = adapter
            println("MULTIPLE CHOICE ***************************** ${assignmentQuestion.arrAssignment[0].question}")

            smoothScroller = LinearSmoothScroller(requireContext())
            PagerSnapHelper().attachToRecyclerView(binding.rvMultipleChoiceStudent)
            adapter.scrollClickHelpers = scrollClickHelpers

            binding.rvMultipleChoiceStudent.addOnItemTouchListener(object :
                RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    return rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
                }
            })
        } catch (e: Exception) {
            println("Exception ******************************* %${e.printStackTrace()}")
        }
    }

    private fun directQuestionFun() {
        val bundle = arguments
        /*assignmentViewTypeListener.questionsOnlyView()
        binding.viewQuestionQuestion.text = assignmentQuestion.question
        binding.viewQuestionExtraNote.text = assignmentQuestion.extraNote
        if (assignmentQuestion.instructions != "") {
            binding.viewQuestionAssignmentInstruction.visibility = View.VISIBLE
            binding.viewQuestionAssignmentInstruction.text = assignmentQuestion.instructions
        }
        if (assignmentQuestion.extraNote != "") {
            binding.viewQuestionExtraNoteHeader.visibility = View.VISIBLE
            binding.viewQuestionExtraNote.text = assignmentQuestion.extraNote
        }
        if (assignmentQuestion.mediaUris.isNotEmpty()) {
            println("FILE URIS *********************************** ${assignmentQuestion.mediaUris}")
            fileAdapter.dataset = assignmentQuestion.mediaUris
            fileAdapter.activity = requireActivity()
            fileAdapter.classCode = assignmentQuestion.classCode
            binding.rvAssignmentDocument.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvAssignmentDocument.adapter = fileAdapter
        }*/
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.view_assignment_back_arrow -> requireActivity().supportFragmentManager.popBackStack()
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

    override fun questionsOnlyView() {
        binding.viewQuestionQuestion.text = assignmentQuestion.question

        binding.viewAssignmentToolbarText.text = "Direct Questions"
        binding.viewAssignmentResultQuestionRoot.visibility = View.GONE
        binding.viewAssignmentMultipleQuestionRoot.visibility = View.GONE
        binding.viewAssignmentSingleQuestionSection.visibility = View.VISIBLE
    }

    override fun fileQuestionsView() {

    }

    override fun resultMultiChoiceView() {
        binding.viewAssignmentToolbarText.text = "Result"
        binding.viewAssignmentSingleQuestionSection.visibility = View.GONE
        binding.viewAssignmentMultipleQuestionRoot.visibility = View.GONE
        binding.viewAssignmentResultQuestionRoot.visibility = View.VISIBLE
    }
}

class FileRecyclerViewAdapter : RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder>() {

    var dataset: ArrayList<String> = arrayListOf()
    var listOfDownload: ArrayList<String> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var classCode: String
    var listOfMediaNames: java.util.ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.view_material_file_attachment_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.view_material_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        val fileName = "Attachment"
        val extension = dataset[position].split(".").last().split("?")[0]
        holder.textView.text = fileName

        val helperClass = HelperClass(datum, classCode, extension, context)
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
                    downloadAndOpenFile(datum, extension)
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

    lateinit var dataset: ArrayList<AssignmentQuestion>
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.multiple_choice_questions_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
            val datum = dataset[position]
            holder.question.text = datum.question
            holder.instruction.text = datum.instructions
            val options = datum.options

            holder.radioA.text = options[0]
            holder.radioB.text = options[1]
            holder.radioC.text = options[2]
            holder.radioD.text = options[3]
            if (options.size > 4) {
                holder.radioE.visibility = View.VISIBLE
                holder.radioE.text = options[4]
            } else {
                holder.radioE.visibility = View.GONE
            }

            holder.radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (position < dataset.size) {
                    scrollClickHelpers.smoothScrollHelper(position + 1)
                }

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
            println("EXCEPTION ******************************** ${e.printStackTrace()}")
        }
    }

    private fun submit() {
        try {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val result = evaluateAssessment()
                val activityX = (activity as FragmentActivity)
                activity.runOnUiThread {
                    val fragmentResult = FragmentResult()
                    val bundle = Bundle()
                    bundle.putInt("result", result)
                    bundle.putInt("overall", dataset.size)
                    fragmentResult.arguments = bundle

                    activityX.supportFragmentManager.popBackStack()
                    activityX.supportFragmentManager.beginTransaction()
                        .addToBackStack("review_of_answers")
                        .replace(R.id.view_material_nested_root, fragmentResult)
                        .commit()
                }
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
}

class FragmentResult : Fragment() {

    private lateinit var binding: FragmentViewAssignmentStudentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAssignmentStudentBinding.inflate(inflater, container, false)

        val bundle = arguments
        val result = bundle!!.getInt("result")
        val overall = bundle.getInt("overall")

        binding.viewAssignmentResultQuestionRoot.visibility = View.VISIBLE
        binding.viewAssignmentBackArrow.setOnClickListener {
            requireActivity().onBackPressed()
        }
        val score = ((result.toFloat() / overall.toFloat()) * 100).toInt().toString() + "%"
        binding.viewAssignmentScore.text = score
        binding.viewAssignmentProgressBar.progress = (score.split("%")[0]).toInt()
        return binding.root
    }
}