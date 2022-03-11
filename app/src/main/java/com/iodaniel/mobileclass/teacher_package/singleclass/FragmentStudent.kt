package com.iodaniel.mobileclass.teacher_package.singleclass

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.StudentFragmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.StudentRegistrationClass
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class FragmentStudent : Fragment(), TotalStudentListener {
    private lateinit var binding: StudentFragmentBinding
    private var adapter = StudentsAdapter()
    private var dataset = arrayListOf<StudentRegistrationClass>()
    private var keyList = arrayListOf<String>()
    private lateinit var totalStudentListener: TotalStudentListener
    private lateinit var classInfo: ClassInfo
    private var registrationRef = FirebaseDatabase.getInstance().reference

    override fun onStart() {
        super.onStart()
        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        classInfo = Json.decodeFromString(json!!)
        registrationRef = registrationRef
            .child("teacher")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("registered_students")
            .child(classInfo.classCode)

        totalStudentListener = this
        readDatabase()
        rvInit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = StudentFragmentBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    private fun readDatabase() {
        registrationRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(StudentRegistrationClass::class.java)!!
                dataset.add(snap)
                keyList.add(snapshot.key!!)
                binding.studentCount.text = dataset.size.toString()
                binding.rvStudents.adapter!!.notifyItemInserted(dataset.size)
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

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
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
        val tv: TextView = itemView.findViewById(R.id.student_name_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.students_row, parent, false)
        return ViewHolder((view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.tv.text = datum.email
    }

    override fun getItemCount(): Int = dataset.size
}