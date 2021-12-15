package com.iodaniel.mobileclass.teacher_package.classes

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.TeacherPageBinding
import com.iodaniel.mobileclass.teacher_package.HelperListener
import com.iodaniel.mobileclass.teacher_package.HelperListener.ClassListener

class TeacherPage : AppCompatActivity(), View.OnClickListener, ClassListener {

    private val binding by lazy {
        TeacherPageBinding.inflate(layoutInflater)
    }
    private lateinit var rvAdapter: MyClassesAdapter
    private lateinit var classListener: ClassListener
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private var listOfCourses: ArrayList<ClassInfo> = arrayListOf()
    private var reference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Classes"
        binding.addClassNote.setOnClickListener(this)
        classListener = this
        initialiseDatabase()
        readDatabase()
    }

    private fun initialiseDatabase() {
        reference = reference
            .child("teacher")
            .child(auth)
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
                    val datetime = courses["datetime"].toString()
                    val dateModified = courses["dateModified"].toString()
                    val classImage = courses["classImage"].toString()
                    val teacherInChargeName = courses["teacherInChargeName"].toString()
                    val time = courses["time"].toString()

                    val classInfo = ClassInfo(
                        className = className,
                        classCode = classCode,
                        classImage = classImage,
                        time = time,
                        datetime = datetime,
                        teacherInChargeName = teacherInChargeName,
                    )
                    listOfCourses.add(classInfo)
                    initRv()
                    classListener.nonEmptyClass()
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
                    val datetime = courses["datetime"].toString()
                    val classImage = courses["classImage"].toString()
                    val teacherInChargeName = courses["teacherInChargeName"].toString()
                    val time = courses["time"].toString()

                    val classInfo = ClassInfo(
                        className = className,
                        classCode = classCode,
                        classImage = classImage,
                        time = time,
                        datetime = datetime,
                        teacherInChargeName = teacherInChargeName,
                    )
                    listOfCourses.add(classInfo)
                    initRv()
                    classListener.nonEmptyClass()
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
        if (listOfCourses.size==0) {
            classListener.emptyClass()
        }
        rvAdapter.context = applicationContext
        rvAdapter.activity = this
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun emptyClass() {
        binding.emptyContainer.visibility = View.VISIBLE
        binding.rvListOfCourses.visibility = View.INVISIBLE
    }

    override fun nonEmptyClass() {
        binding.emptyContainer.visibility = View.INVISIBLE
        binding.rvListOfCourses.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_class_note -> {
                val intent = Intent(applicationContext, ClassUpload::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
    }
}