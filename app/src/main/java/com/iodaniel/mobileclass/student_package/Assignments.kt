package com.iodaniel.mobileclass.student_package

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentAssignmentsBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.MultiChoiceQuestion
import com.iodaniel.mobileclass.teacher_package.singleclass.HelperListener


class Assignments(val classInfo: ClassInfo) : Fragment(), HelperListener {
    private lateinit var binding: FragmentAssignmentsBinding
    private lateinit var assignmentAdapter: AssignmentsAdapter
    private var dataSet: ArrayList<MultiChoiceQuestion> = arrayListOf()
    private var dataSetMultipleChoice: ArrayList<ArrayList<HashMap<*, *>>> = arrayListOf()

    private var dataSetKeyList: ArrayList<String> = arrayListOf()
    private var dataSetMultipleChoiceKeyList: ArrayList<String> = arrayListOf()

    private var multiChoiceRef = FirebaseDatabase.getInstance().reference
        .child("multi_choice_question")
        .child(classInfo.teacherInChargeUID)
        .child(classInfo.classCode)
    private var uploadDocsRef = FirebaseDatabase.getInstance().reference
        .child("doc_question")
        .child(classInfo.teacherInChargeUID)
        .child(classInfo.classCode)
    private var directQueRef = FirebaseDatabase.getInstance().reference
        .child("direct_question")
        .child(classInfo.teacherInChargeUID)
        .child(classInfo.classCode)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssignmentsBinding.inflate(inflater, container, false)
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

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
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
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = dataSetMultipleChoiceKeyList.indexOf(snapshot.key)
                dataSetMultipleChoiceKeyList.removeAt(index)
                dataSetMultipleChoice.removeAt(index)
                assignmentAdapter.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


        uploadDocsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(MultiChoiceQuestion::class.java)
                dataSet.add(snap!!)
                dataSetKeyList.add(snapshot.key!!)
                rvInit()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(MultiChoiceQuestion::class.java)
                dataSet.add(snap!!)
                dataSetKeyList.add(snapshot.key!!)
                rvInit()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = dataSetKeyList.indexOf(snapshot.key)
                dataSetKeyList.removeAt(index)
                dataSet.removeAt(index)
                assignmentAdapter.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })



        directQueRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(MultiChoiceQuestion::class.java)
                dataSet.add(snap!!)
                dataSetKeyList.add(snapshot.key!!)
                rvInit()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(MultiChoiceQuestion::class.java)
                dataSet.add(snap!!)
                dataSetKeyList.add(snapshot.key!!)
                rvInit()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = dataSetKeyList.indexOf(snapshot.key)
                dataSetKeyList.removeAt(index)
                dataSet.removeAt(index)
                assignmentAdapter.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun rvInit() {
        assignmentAdapter = AssignmentsAdapter()
        binding.rvAssignmentsStudent.adapter = assignmentAdapter
        binding.rvAssignmentsStudent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        assignmentAdapter.dataset = dataSet
        assignmentAdapter.dataSetMultipleChoice = dataSetMultipleChoice
        assignmentAdapter.activity = requireActivity()
        assignmentAdapter.helperListener = this
    }

    override fun helperClickListener(datum: MultiChoiceQuestion) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("dataS")
            .replace(R.id.a_class_frame_student, ViewAssignmentStudent(datum)).commit()
    }

    override fun helperClickListenerMultipleChoice(datum: ArrayList<HashMap<*, *>>) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("data_multiple_choice_questionS")
            .replace(R.id.a_class_frame_student, ViewAssignmentStudent(multipleChoiceQuestions = datum)).commit()
    }
}

class AssignmentsAdapter : RecyclerView.Adapter<AssignmentsAdapter.ViewHolder>() {

    lateinit var dataset: ArrayList<MultiChoiceQuestion>
    lateinit var dataSetMultipleChoice: ArrayList<ArrayList<HashMap<*, *>>>
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var helperListener: HelperListener

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt: TextView = itemView.findViewById(R.id.assignment_row_chip_student)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.assignments_row_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (position < dataset.size) {
            val datum = dataset[position]
            val name = "Assignment ${position + 1}"
            holder.txt.text = name

            holder.txt.setOnClickListener {
                helperListener.helperClickListener(datum)
            }
        } else if (position >= dataset.size) {
            val datum = dataSetMultipleChoice[position - dataset.size]
            val name = "Assignment ${position + 1}"
            holder.txt.text = name

            try {
                holder.txt.setOnClickListener {
                    helperListener.helperClickListenerMultipleChoice(datum)
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun getItemCount(): Int = dataset.size + dataSetMultipleChoice.size
}
