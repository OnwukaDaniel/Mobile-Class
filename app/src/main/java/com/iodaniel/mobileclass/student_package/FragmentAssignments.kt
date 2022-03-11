package com.iodaniel.mobileclass.student_package

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.AssignmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.AssignmentQuestion
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class FragmentAssignments : Fragment(), ViewAssignmentHelper{
    private lateinit var binding: AssignmentBinding
    private lateinit var assignmentAdapter: AssignmentsAdapter
    private lateinit var classInfo: ClassInfo
    private var dataSet: ArrayList<AssignmentQuestion> = arrayListOf()
    private var questionDataset: ArrayList<String> = arrayListOf()
    private var keyList: ArrayList<String> = arrayListOf()
    private var questionTypeDataset: ArrayList<String> = arrayListOf()
    private var dataSetKeyList: ArrayList<String> = arrayListOf()
    private lateinit var viewAssignmentHelper: ViewAssignmentHelper

    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
    private var uploadDocsRef = FirebaseDatabase.getInstance().reference
    private var directQueRef = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = AssignmentBinding.inflate(layoutInflater, container, false)
        viewAssignmentHelper = this
        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        classInfo = Json.decodeFromString(json!!)

        multiChoiceRef = multiChoiceRef
            .child("multi_choice_question")
            .child(classInfo.teacherInChargeUID)
            .child(classInfo.classCode)
        uploadDocsRef = uploadDocsRef
            .child("doc_question")
            .child(classInfo.teacherInChargeUID)
            .child(classInfo.classCode)
        directQueRef = directQueRef
            .child("direct_question")
            .child(classInfo.teacherInChargeUID)
            .child(classInfo.classCode)

        readData()
        return binding.root
    }

    private fun readData() {
        multiChoiceRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                questionDataset.add(snapshot.key!!)
                questionTypeDataset.add("multiChoice")
                keyList.add(snapshot.key!!)
                rvInit()
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
                assignmentAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        uploadDocsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                questionDataset.add(snapshot.key!!)
                questionTypeDataset.add("uploadDocs")
                keyList.add(snapshot.key!!)
                rvInit()
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
                assignmentAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        directQueRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                questionDataset.add(snapshot.key!!)
                questionTypeDataset.add("directQue")
                keyList.add(snapshot.key!!)
                rvInit()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = keyList.indexOf(snapshot.key)
                keyList.removeAt(index)
                questionDataset.removeAt(index)
                questionTypeDataset.removeAt(index)
                assignmentAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun rvInit() {
        assignmentAdapter = AssignmentsAdapter()
        binding.rvAssignments.adapter = assignmentAdapter
        binding.rvAssignments.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        assignmentAdapter.questionDataset = questionDataset
        assignmentAdapter.questionTypeDataset = questionTypeDataset
        assignmentAdapter.activity = requireActivity()
        assignmentAdapter.classInfo = classInfo
        assignmentAdapter.viewAssignmentHelper = viewAssignmentHelper
    }

    override fun inflateFragment(viewAssignmentStudentFragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack("dataS")
            .replace(R.id.a_class_frame_student, viewAssignmentStudentFragment).commit()
    }
}

class AssignmentsAdapter : RecyclerView.Adapter<AssignmentsAdapter.ViewHolder>() {

    lateinit var questionDataset: ArrayList<String>
    lateinit var questionTypeDataset: ArrayList<String>
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var classInfo: ClassInfo
    lateinit var viewAssignmentHelper: ViewAssignmentHelper

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: Chip = itemView.findViewById(R.id.assignment_row_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.assignments_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = questionDataset[position]
        val questionType = questionTypeDataset[position]
        val name = "Assignment ${position + 1}"
        holder.chip.text = name

        holder.chip.setOnClickListener {
            val bundle = Bundle()
            val classInfoJson = Gson().toJson(classInfo)

            bundle.putString("datum", datum)
            bundle.putString("questionType", questionType)
            bundle.putString("classCode", classInfo.classCode)
            bundle.putString("classInfo", classInfoJson)

            val viewAssignmentStudentFragment = FragmentViewAssignment()
            viewAssignmentStudentFragment.arguments = bundle
            if (viewAssignmentHelper!=null) viewAssignmentHelper.inflateFragment(viewAssignmentStudentFragment)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }

    override fun getItemCount(): Int = questionDataset.size
}
interface ViewAssignmentHelper{
    fun inflateFragment(viewAssignmentStudentFragment: Fragment)
}