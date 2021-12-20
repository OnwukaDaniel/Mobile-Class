package com.iodaniel.mobileclass.teacher_package.classes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.TeacherPageBinding
import com.iodaniel.mobileclass.teacher_package.HelperListener.ClassListener
import com.iodaniel.mobileclass.teacher_package.singleclass.AClass
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val materialRemoved = snapshot.getValue(ClassInfo::class.java)
                listOfCourses.remove(materialRemoved)
                initRv()
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
        if (listOfCourses.size == 0) {
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

class MyClassesAdapter : RecyclerView.Adapter<MyClassesAdapter.MyCoursesAdapterViewHolder>() {

    var dataset: ArrayList<ClassInfo> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity

    init {
        setHasStableIds(true)
    }

    class MyCoursesAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.teacher_class_image)
        val className: TextView = view.findViewById(R.id.teacher_class_name)
        val teacherInChargeName: TextView = view.findViewById(R.id.teacher_year)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCoursesAdapterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.my_courses_row, parent, false)
        return MyCoursesAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCoursesAdapterViewHolder, position: Int) {
        val datum = dataset[position]
        val imageUri = Uri.parse(datum.classImage)
        val red = datum.red
        val green = datum.green
        val blue = datum.blue

        Glide.with(context).load(imageUri).centerCrop().into(holder.image)
        holder.image.setColorFilter(Color.argb(80, red, green, blue))
        holder.className.text = datum.className
        if (holder.teacherInChargeName.text != "") {
            holder.teacherInChargeName.visibility = View.VISIBLE
            holder.teacherInChargeName.text = datum.teacherInChargeName
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, AClass::class.java)
            println("AClass ************************** ${datum.className}")
            println("AClass dataset ************************** ${dataset.size}")
            val json = Json.encodeToString(datum)
            intent.putExtra("class_data", json)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount(): Int = dataset.size
}