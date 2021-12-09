package com.iodaniel.mobileclass.class_assignment_upload.`class`

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.class_assignment_upload.Material
import com.iodaniel.mobileclass.class_assignment_upload.MyCourse
import com.iodaniel.mobileclass.class_assignment_upload.MyCoursesAdapter
import com.iodaniel.mobileclass.databinding.ActivityClassBinding
import com.iodaniel.mobileclass.class_assignment_upload.ClassMaterialUpload

class Classes : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityClassBinding.inflate(layoutInflater)
    }
    private lateinit var rvAdapter: MyCoursesAdapter
    private var listOfCourses: ArrayList<MyCourse> = arrayListOf()
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
                    val courses = (snapshot.value as HashMap<*, *>)["course"] as HashMap<*, *>
                    val gson = Gson().toJson((courses.values.toList()))
                    for (course in courses.values.toList()) {
                        val courseName = (course as HashMap<*, *>)["courseName"].toString()
                        val dateCreated = course["dateCreated"].toString()
                        val courseImageUri = course["courseImageUri"].toString()
                        val materials =
                            if (course["materials"] == null) (course["materials"] as HashMap<*, *>).values.toList() else arrayListOf()
                        val year = course["year"].toString()
                        val teacherInCharge = course["teacherInCharge"].toString()
                        val time = course["time"].toString()

                        val myCourse = MyCourse(
                            courseName = courseName,
                            courseImageUri = courseImageUri,
                            year = year,
                            time = time,
                            dateCreated = dateCreated,
                            materials = materials.toMutableList() as ArrayList<Material>,
                            teacherInCharge = teacherInCharge,
                        )
                        listOfCourses.add(myCourse)
                        initRv()
                    }
                } catch (e: Exception) {
                    println("ERROR  *************************** ${e.printStackTrace()}")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val courses = (snapshot.value as HashMap<*, *>)["course"] as HashMap<*, *>
                    val gson = Gson().toJson((courses.values.toList()))
                    for (course in courses.values.toList()) {
                        val courseName = (course as HashMap<*, *>)["courseName"].toString()
                        val dateCreated = course["dateCreated"].toString()
                        val courseImageUri = course["courseImageUri"].toString()
                        val materials =
                            (course["materials"] as HashMap<*, *>).values.toList()
                        val year = course["year"].toString()
                        val teacherInCharge = course["teacherInCharge"].toString()
                        val time = course["time"].toString()

                        val myCourse = MyCourse(
                            courseName = courseName,
                            courseImageUri = courseImageUri,
                            year = year,
                            time = time,
                            dateCreated = dateCreated,
                            materials = materials.toMutableList() as ArrayList<Material>,
                            teacherInCharge = teacherInCharge,
                        )
                        listOfCourses.add(myCourse)
                        initRv()
                    }
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
        rvAdapter = MyCoursesAdapter()
        binding.rvListOfCourses.adapter = rvAdapter
        binding.rvListOfCourses.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        rvAdapter.dataset = listOfCourses
        rvAdapter.context = applicationContext
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_class_note -> {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.class_name_dalog)
                dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT)
                val proceed: TextView = dialog.findViewById(R.id.proceed)
                val className: EditText = dialog.findViewById(R.id.class_name)
                proceed.setOnClickListener {
                    if (className.text.trim().toString() != "") {
                        val intent = Intent(applicationContext, ClassMaterialUpload::class.java)
                        intent.putExtra("class_name", className.text.trim().toString())
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
        }
    }
}