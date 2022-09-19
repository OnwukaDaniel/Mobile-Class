package com.iodaniel.mobileclass.shared_classes

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentViewMaterialsBinding
import com.iodaniel.mobileclass.teacher_package.classes.AssignmentQuestion
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.Material
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FragmentMaterials : Fragment(), ClickHelper {
    private lateinit var binding: FragmentViewMaterialsBinding
    private lateinit var datum: Material
    private lateinit var classInfo: ClassInfo
    private var exerciseAdapter = ExerciseAdapter()

    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
    private var uploadDocsRef = FirebaseDatabase.getInstance().reference
    private var directQueRef = FirebaseDatabase.getInstance().reference

    private var questionTypeDataset = arrayListOf<String>()
    private var questionDataset = arrayListOf<AssignmentQuestion>()
    private var keyList = arrayListOf<String>()

    private val writeExternalPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val permissionGranted = PackageManager.PERMISSION_GRANTED
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                readData(datum)
            } else if (!isGranted) {
                val txt = "Permission needed to show class materials"
                Snackbar.make(binding.root, txt, Snackbar.LENGTH_INDEFINITE).show()
            }
        }

    override fun onStart() {
        super.onStart()
        val json = requireArguments().getString("lesson")
        val gson = Gson()
        datum = Json.decodeFromString(json!!)
        val classInfoJson = requireArguments().getString("classInfo")

        classInfo = gson.fromJson(classInfoJson, ClassInfo::class.java)
        when (permissionGranted) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                writeExternalPermission
            ) -> readData(datum)
            else -> permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewMaterialsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun readData(material: Material) = CoroutineScope(Dispatchers.IO).launch {

        exerciseAdapter.questionDataset = questionDataset
        exerciseAdapter.listOfMediaNames = datum.listOfMediaNames
        exerciseAdapter.activity = requireActivity()
        exerciseAdapter.clickHelper = this@FragmentMaterials
        exerciseAdapter.classCode = classInfo.classCode

        requireActivity().runOnUiThread {
            if (questionDataset.size > 0) binding.viewMaterialNumExercise.text =
                questionDataset.size.toString()

            binding.rvViewMaterialAssignment.adapter = exerciseAdapter
            binding.rvViewMaterialAssignment.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        multiChoiceRef = multiChoiceRef
            .child("multi_choice_question")
            .child(classInfo.teacherInChargeUID)
            .child(classInfo.classCode)
            .child(material.dateCreated)

        uploadDocsRef = uploadDocsRef
            .child("doc_question")
            .child(classInfo.teacherInChargeUID)
            .child(classInfo.classCode)
            .child(material.dateCreated)

        directQueRef = directQueRef
            .child("direct_question")
            .child(classInfo.teacherInChargeUID)
            .child(classInfo.classCode)
            .child(material.dateCreated)
        readExercise()
    }

    private fun readExercise() {
        multiChoiceRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(AssignmentQuestion::class.java)!!
                questionDataset.add(snap)
                questionTypeDataset.add("multiChoice")
                keyList.add(snapshot.key!!)
                exerciseAdapter.notifyItemInserted(keyList.size)
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = keyList.indexOf(snapshot.key)
                keyList.removeAt(index)
                questionDataset.removeAt(index)
                questionTypeDataset.removeAt(index)
                exerciseAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        uploadDocsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(AssignmentQuestion::class.java)!!
                println("SNAP *************************** $snap")
                questionDataset.add(snap)
                questionTypeDataset.add("directQue")
                keyList.add(snapshot.key!!)
                exerciseAdapter.notifyItemInserted(keyList.size)
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = keyList.indexOf(snapshot.key)
                keyList.removeAt(index)
                questionDataset.removeAt(index)
                questionTypeDataset.removeAt(index)
                exerciseAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        directQueRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(AssignmentQuestion::class.java)!!
                questionDataset.add(snap)
                questionTypeDataset.add("directQue")
                keyList.add(snapshot.key!!)
                exerciseAdapter.notifyItemInserted(keyList.size)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = keyList.indexOf(snapshot.key)
                keyList.removeAt(index)
                questionDataset.removeAt(index)
                questionTypeDataset.removeAt(index)
                exerciseAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onClickItem(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack("view assignment")
            .replace(R.id.view_material_nested_root, fragment)
            .commit()
    }
}

class ExerciseAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var questionDataset: ArrayList<AssignmentQuestion> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var classCode: String
    var listOfMediaNames: ArrayList<String> = arrayListOf()
    private val singleQuestionView = 0
    private val multipleChoiceView = 1
    lateinit var clickHelper: ClickHelper

    class SingleQuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.view_material_file_exercise_text)
        val assignment: LinearLayout =
            itemView.findViewById(R.id.view_material_attachment_file_view)
        val deadline: TextView = itemView.findViewById(R.id.view_material_deadline)
    }

    class MultipleChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.view_material_file_exercise_text)
        val assignment: LinearLayout =
            itemView.findViewById(R.id.view_material_attachment_file_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context

        return when (viewType) {
            singleQuestionView -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_exercise_row, parent, false)
                SingleQuestionViewHolder(view)
            }
            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_exercise_row, parent, false)
                MultipleChoiceViewHolder(view)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val datum = questionDataset[position]
        when (getViewType(datum)) {
            singleQuestionView -> {
                holder as SingleQuestionViewHolder
                holder.textView.text = (position + 1).toString()
                datum.dueDate
                val date = if (datum.dueDate.isEmpty()) {
                    "No due date"
                } else {
                    val day = datum.dueDate.split(".")[0]
                    val month = Util.digitToMonth(datum.dueDate.split(".")[1].toInt())
                    val year = datum.dueDate.split(".")[2]
                    val date = "$day-$month-$year"
                    date
                }
                holder.deadline.text = date
                holder.assignment.setOnClickListener {
                    val bundle = Bundle()
                    val json = Json.encodeToString(datum)
                    bundle.putString("questionData", json)
                    val fragmentViewAssignmentStudent = FragmentStudentViewAssignment()
                    fragmentViewAssignmentStudent.arguments = bundle
                    clickHelper.onClickItem(fragmentViewAssignmentStudent)
                }
            }
            multipleChoiceView -> {
                holder as SingleQuestionViewHolder
                holder.textView.text = ("Exercise Question ${position + 1}").toString()
                val date = Util.convertLongToTime(datum.datetime.toLong())
                val year = date.split(".")[0]
                val month = Util.digitToMonth(date.split(".")[1].toInt())
                val day = date.split(".")[2].split(" ")[0]
                val dateInText = "$day-$month-$year"
                holder.deadline.text = dateInText
                holder.assignment.setOnClickListener {
                    val bundle = Bundle()
                    val json = Gson().toJson(datum)
                    bundle.putString("questionData", json)
                    val fragmentViewAssignmentStudent = FragmentStudentViewAssignment()
                    fragmentViewAssignmentStudent.arguments = bundle
                    clickHelper.onClickItem(fragmentViewAssignmentStudent)
                }
            }
        }
    }

    private fun getViewType(question: AssignmentQuestion): Int {
        return when (question.questionType) {
            context.getString(R.string.MULTIPLECHOICEQUESTION) -> multipleChoiceView
            context.getString(R.string.DOCUMENTQUESTION) -> singleQuestionView
            else -> 0
        }
    }

    override fun getItemCount(): Int = questionDataset.size
}

interface ClickHelper {
    fun onClickItem(fragment: Fragment)
}