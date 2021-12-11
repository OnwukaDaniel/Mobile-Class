package com.iodaniel.mobileclass.class_assignment_upload.classes

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityClassBinding

class Classes : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityClassBinding.inflate(layoutInflater)
    }
    private lateinit var rvAdapter: MyClassesAdapter
    private var listOfCourses: ArrayList<ClassInfo> = arrayListOf()
    private var reference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Classes"
        binding.addClassNote.setOnClickListener(this)
        initialiseDatabase()
        readDatabase()
    }

    private fun initialiseDatabase() {
        reference = reference
            .child("admins")
            .child("abc@gmailcom")
            .child("classes")
    }

    private fun readDatabase() {
        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val courses = (snapshot.value as HashMap<*, *>)
                    val gson = Gson().toJson((courses.values.toList()))
                    val className = courses["className"].toString()
                    val classCode = courses["classCode"].toString()
                    val dateCreated = courses["dateCreated"].toString()
                    val dateModified = courses["dateModified"].toString()
                    val classImage = courses["classImage"].toString()
                    val teacherInChargeName = courses["teacherInChargeName"].toString()
                    val time = courses["time"].toString()

                    val classInfo = ClassInfo(
                        className = className,
                        classCode = classCode,
                        classImage = classImage,
                        time = time,
                        dateCreated = dateCreated,
                        teacherInChargeName = teacherInChargeName,
                    )
                    listOfCourses.add(classInfo)
                    initRv()
                } catch (e: Exception) {
                    println("ERROR  *************************** ${e.printStackTrace()}")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val courses = (snapshot.value as HashMap<*, *>)
                    val gson = Gson().toJson((courses.values.toList()))
                    val className = courses["className"].toString()
                    val classCode = courses["classCode"].toString()
                    val dateCreated = courses["dateCreated"].toString()
                    val dateModified = courses["dateModified"].toString()
                    val classImage = courses["classImage"].toString()
                    val teacherInChargeName = courses["teacherInChargeName"].toString()
                    val time = courses["time"].toString()

                    val classInfo = ClassInfo(
                        className = className,
                        classCode = classCode,
                        classImage = classImage,
                        time = time,
                        dateCreated = dateCreated,
                        teacherInChargeName = teacherInChargeName,
                    )
                    listOfCourses.add(classInfo)
                    initRv()
                } catch (e: Exception) {
                    println("ERROR  *************************** ${e.printStackTrace()}")
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                print("Error ********************************* ${error.message}")
            }
        })
    }

    private fun initRv() {
        rvAdapter = MyClassesAdapter()
        binding.rvListOfCourses.adapter = rvAdapter
        binding.rvListOfCourses.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        rvAdapter.dataset = listOfCourses
        rvAdapter.context = applicationContext
        rvAdapter.activity = this
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_class_note -> {
                val intent = Intent(applicationContext, ClassMaterialUpload::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
    }
}