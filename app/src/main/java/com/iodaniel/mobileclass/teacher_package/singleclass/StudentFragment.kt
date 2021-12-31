package com.iodaniel.mobileclass.teacher_package.singleclass

import android.annotation.SuppressLint
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
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.StudentFragmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.StudentRegistrationClass

class StudentFragment(val classInfo: ClassInfo) : Fragment(), TotalStudentListener {
    private lateinit var binding: StudentFragmentBinding
    private var adapter = StudentsAdapter()
    private var dataset = arrayListOf<StudentRegistrationClass>()
    private var keyList = arrayListOf<String>()
    private lateinit var totalStudentListener: TotalStudentListener
    private var registeredRef = FirebaseDatabase.getInstance()
        .reference
        .child("teacher")
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child("registered_students")
        .child(classInfo.classCode)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        binding = StudentFragmentBinding.inflate(layoutInflater, container, false)
        totalStudentListener = this
        readDatabase()
        return binding.root
    }

    private fun readDatabase() {
        registeredRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                println("STUDENTS onChildAdded ******************************** ${snapshot.value}")
                val snap = snapshot.getValue(StudentRegistrationClass::class.java)!!
                dataset.add(snap)
                keyList.add(snapshot.key!!)
                binding.studentCount.text = dataset.size.toString()
                rvInit()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(StudentRegistrationClass::class.java)!!
                dataset.add(snap)
                keyList.add(snapshot.key!!)
                binding.studentCount.text = dataset.size.toString()
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = keyList.indexOf(snapshot.key)
                keyList.removeAt(index)
                dataset.removeAt(index)
                binding.rvStudents.adapter!!.notifyItemRemoved(index)
                binding.studentCount.text = dataset.size.toString()
                adapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                println("Moved STUDENTS ************* $snapshot")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun rvInit() {
        totalStudentListener.updateTotalStudents(dataset)
        adapter.dataset = dataset
        binding.rvStudents.adapter = adapter
        binding.rvStudents.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    override fun updateTotalStudents(dataset: ArrayList<StudentRegistrationClass>) {
        binding.studentCount.text = dataset.size.toString()
    }
}

interface TotalStudentListener {
    fun updateTotalStudents(dataset: ArrayList<StudentRegistrationClass>)
}

class StudentsAdapter : RecyclerView.Adapter<StudentsAdapter.ViewHolder>() {

    var dataset = arrayListOf<StudentRegistrationClass>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: Chip = itemView.findViewById(R.id.student_name_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.students_row, parent, false)
        return ViewHolder((view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.chip.text = datum.email
    }

    override fun getItemCount(): Int = dataset.size
}