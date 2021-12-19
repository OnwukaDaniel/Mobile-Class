package com.iodaniel.mobileclass.teacher_package.singleclass

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
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.StudentFragmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo

class StudentFragment(val classInfo: ClassInfo) : Fragment() {
    private lateinit var binding: StudentFragmentBinding
    private var adapter = StudentsAdapter()
    private var stRef = FirebaseDatabase.getInstance().reference
        .child("students")
        .child(classInfo.classCode)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = StudentFragmentBinding.inflate(layoutInflater, container, false)

        readDatabase()
        return binding.root
    }

    private fun readDatabase() {
        stRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.value
                println("Added STUDENTS ************* $snap")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                println("Changed STUDENTS ************* $snapshot")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                println("Removed STUDENTS ************* $snapshot")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                println("Moved STUDENTS ************* $snapshot")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun rvInit() {
        adapter.dataset
        binding.rvStudents.adapter = adapter
        binding.rvStudents.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }
}

class StudentsAdapter : RecyclerView.Adapter<StudentsAdapter.ViewHolder>() {

    var dataset = arrayListOf<String>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: Chip = itemView.findViewById(R.id.student_name_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.students_row, parent, false)
        return ViewHolder((view))
    }

    override fun onBindViewHolder(holder: StudentsAdapter.ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.chip.text = datum
    }

    override fun getItemCount(): Int = dataset.size

}