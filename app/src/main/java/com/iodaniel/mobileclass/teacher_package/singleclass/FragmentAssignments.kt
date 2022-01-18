package com.iodaniel.mobileclass.teacher_package.singleclass

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.AssignmentBinding
import com.iodaniel.mobileclass.student_package.FragmentViewAssignmentStudent
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.AssignmentQuestion
import com.iodaniel.mobileclass.teacher_package.singleclass.AssignmentsAdapter.ViewHolder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class FragmentAssignments : Fragment(), HelperListener {
    private lateinit var binding: AssignmentBinding
    private lateinit var assignmentAdapter: AssignmentsAdapter
    private var dataSet: ArrayList<AssignmentQuestion> = arrayListOf()
    private var dataSetMultipleChoice: ArrayList<ArrayList<HashMap<*, *>>> = arrayListOf()
    private lateinit var classInfo: ClassInfo
    private var dataSetKeyList: ArrayList<String> = arrayListOf()
    private var dataSetMultipleChoiceKeyList: ArrayList<String> = arrayListOf()

    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
    private var uploadDocsRef = FirebaseDatabase.getInstance().reference
    private var directQueRef = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = AssignmentBinding.inflate(layoutInflater, container, false)
        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        classInfo = Json.decodeFromString(json!!)

        multiChoiceRef = multiChoiceRef
            .child("multi_choice_question")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(classInfo.classCode)
        uploadDocsRef = uploadDocsRef
            .child("doc_question")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(classInfo.classCode)
        directQueRef = directQueRef
            .child("direct_question")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(classInfo.classCode)

        readData()
        return binding.root
    }

    private fun readData() {
        multiChoiceRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.value as ArrayList<HashMap<*, *>>
                try {
                    dataSetMultipleChoice.add(snap)
                    dataSetMultipleChoiceKeyList.add(snapshot.key!!)
                    rvInit()
                } catch (e: Exception) {
                    println("********************* ${e.printStackTrace()}")
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.value as ArrayList<HashMap<*, *>>
                try {
                    dataSetMultipleChoice.add(snap)
                    dataSetMultipleChoiceKeyList.add(snapshot.key!!)
                    assignmentAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    println("********************* ${e.printStackTrace()}")
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = dataSetMultipleChoiceKeyList.indexOf(snapshot.key)
                dataSetMultipleChoiceKeyList.removeAt(index)
                dataSetMultipleChoice.removeAt(index)
                assignmentAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        uploadDocsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(AssignmentQuestion::class.java)
                dataSet.add(snap!!)
                dataSetKeyList.add(snapshot.key!!)
                rvInit()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(AssignmentQuestion::class.java)
                dataSet.add(snap!!)
                dataSetKeyList.add(snapshot.key!!)
                assignmentAdapter.notifyDataSetChanged()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = dataSetKeyList.indexOf(snapshot.key)
                dataSetKeyList.removeAt(index)
                dataSet.removeAt(index)
                assignmentAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        directQueRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(AssignmentQuestion::class.java)
                dataSet.add(snap!!)
                dataSetKeyList.add(snapshot.key!!)
                println("SNAP ******************** ${snap.datetime}")
                rvInit()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(AssignmentQuestion::class.java)
                dataSet.add(snap!!)
                dataSetKeyList.add(snapshot.key!!)
                rvInit()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = dataSetKeyList.indexOf(snapshot.key)
                dataSetKeyList.removeAt(index)
                dataSet.removeAt(index)
                assignmentAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun rvInit() {
        /*dataSet = dataSet.shuffled().sortedBy {
            it.datetime
        } as ArrayList<MultiChoiceQuestion>*/
        assignmentAdapter = AssignmentsAdapter()
        binding.rvAssignments.adapter = assignmentAdapter
        binding.rvAssignments.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        assignmentAdapter.dataset = dataSet
        assignmentAdapter.dataSetMultipleChoice = dataSetMultipleChoice
        assignmentAdapter.activity = requireActivity()
        assignmentAdapter.helperListener = this
    }

    override fun singleQuestionHelper(datum: AssignmentQuestion) {
        val viewAssignmentStudentFragment = FragmentViewAssignmentStudent()
        val bundle = Bundle()
        val jsonMultiChoiceQuestion = Json.encodeToString(datum)
        bundle.putString("jsonMultiChoiceQuestion", jsonMultiChoiceQuestion)
        bundle.putString("questionType", "singleQuestion")
        viewAssignmentStudentFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("dataS")
            .replace(R.id.a_class_frame, viewAssignmentStudentFragment).commit()
    }

    override fun multipleChoiceHelper(datum: ArrayList<HashMap<*, *>>) {
        val bundle = Bundle()
        val viewAssignmentStudentFragment = FragmentViewAssignmentStudent()
        val json= Gson().toJsonTree(datum)
        bundle.putString("jsonMultiChoiceQuestion", json.toString())
        bundle.putString("questionType", "multiChoice")
        bundle.putString("viewType", "teacher")
        viewAssignmentStudentFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack("data_multiple_choice_questionS")
            .replace(R.id.a_class_frame, viewAssignmentStudentFragment).commit()
    }
}

class AssignmentsAdapter : RecyclerView.Adapter<ViewHolder>() {

    lateinit var dataset: ArrayList<AssignmentQuestion>
    lateinit var dataSetMultipleChoice: ArrayList<ArrayList<HashMap<*, *>>>
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var helperListener: HelperListener

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
        if (position < dataset.size) {
            val datum = dataset[position]
            try{
                val name = "Assignment ${position + 1}"
                holder.chip.text = name
            } catch (e: Exception){

            }

            holder.chip.setOnClickListener {
                helperListener.singleQuestionHelper(datum)
            }
        } else if (position >= dataset.size) {
            val datum = dataSetMultipleChoice[position - dataset.size]
            val name = "Assignment ${position + 1}"
            holder.chip.text = name

            try {
                holder.chip.setOnClickListener {
                    helperListener.multipleChoiceHelper(datum)
                }
            } catch (e: Exception) {
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }

    override fun getItemCount(): Int = dataset.size + dataSetMultipleChoice.size
}

interface HelperListener {
    fun singleQuestionHelper(datum: AssignmentQuestion)
    fun multipleChoiceHelper(datum: ArrayList<HashMap<*, *>>)
}