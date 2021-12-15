package com.iodaniel.mobileclass.teacher_package.singleclass

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
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.LessonFragmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.Material

class LessonsFragment(private val classInfo: ClassInfo) : Fragment() {

    private lateinit var binding: LessonFragmentBinding
    private lateinit var adapter: LessonRvAdapter
    private var listOfLessons: ArrayList<Material> = arrayListOf()
    private var stTypeRef = FirebaseDatabase.getInstance().reference.child("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        println("ON CREATE ********************************* LESSON FRAGMENT ***** $classInfo")
        binding = LessonFragmentBinding.inflate(layoutInflater, container, false)

        readFromDatabase()
        return binding.root
    }

    private fun rvInit() {
        println("rvInit ********************************* rvInit")
        adapter = LessonRvAdapter()
        adapter.dataSet = listOfLessons
        binding.rvLessons.adapter = adapter
        binding.rvLessons.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun readFromDatabase() {
        stTypeRef = stTypeRef
            .child("teacher")
            .child("materials")
            .child(classInfo.classCode)

        stTypeRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                readData(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                readData(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

            fun readData(snapshot: DataSnapshot) {
                try {
                    val lessonSnap = (snapshot.value as HashMap<*, *>)
                    println("LESSON SNAP ****************************** $lessonSnap")
                    val json = Gson().toJson(lessonSnap.values.toList())

                    val courseName = lessonSnap["courseName"].toString()
                    val note = lessonSnap["note"].toString()
                    val extraNote = lessonSnap["extraNote"].toString()
                    val heading = lessonSnap["heading"].toString()

                    val mediaUris =
                        if (lessonSnap["mediaUris"] != null) lessonSnap["mediaUris"] as ArrayList<String> else arrayListOf()
                    val classwork =
                        if (lessonSnap["classwork"] != null) lessonSnap["classwork"] as ArrayList<String> else arrayListOf()
                    val test =
                        if (lessonSnap["test"] != null) lessonSnap["test"] as ArrayList<String> else arrayListOf()

                    val teacherInCharge = lessonSnap["teacherInCharge"].toString()
                    val year = lessonSnap["year"].toString()
                    val time = lessonSnap["time"].toString()
                    val dateModified = lessonSnap["dateModified"].toString()
                    val dateCreated = lessonSnap["dateCreated"].toString()

                    val material = Material(courseName,
                        note,
                        extraNote,
                        heading,
                        mediaUris = mediaUris,
                        classwork = classwork,
                        test,
                        teacherInCharge,
                        year,
                        time,
                        dateModified,
                        dateCreated)
                    listOfLessons.add(material)
                    rvInit()
                } catch (e: Exception) {
                    println("Exception from database ******************** ${e.printStackTrace()}")
                }
            }
        })
    }

}

class LessonRvAdapter : RecyclerView.Adapter<LessonRvAdapter.ViewHolder>() {

    var dataSet: ArrayList<Material> = arrayListOf()
    lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lesson_serial_number: TextView = itemView.findViewById(R.id.lesson_serial_number)
        val lesson_heading: TextView = itemView.findViewById(R.id.lesson_heading)
        val lesson_number: TextView = itemView.findViewById(R.id.lesson_number)
        val lesson_date: TextView = itemView.findViewById(R.id.lesson_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lesson_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataSet[position]
        holder.lesson_date.text = datum.time
        holder.lesson_heading.text = datum.heading
        println("********************* dataset size ${datum.heading}")

    }

    override fun getItemCount(): Int = dataSet.size
}