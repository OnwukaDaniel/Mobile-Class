package com.iodaniel.mobileclass.student_package

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
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityMyClassesBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MyClasses : AppCompatActivity(), View.OnClickListener, HelperListener.LoadingListener {

    private val binding by lazy {
        ActivityMyClassesBinding.inflate(layoutInflater)
    }
    private lateinit var loadingListener: HelperListener.LoadingListener
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private var registerRef = FirebaseDatabase.getInstance().reference
    private var classRef = FirebaseDatabase.getInstance().reference
    private var myCoursesRef = FirebaseDatabase.getInstance().reference
    private var myCoursesList: ArrayList<ClassInfo> = arrayListOf()
    private val adapter = MyClassesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
        checkHasIntent()
    }

    private fun init() {
        loadingListener = this
        binding.studentFab.setOnClickListener(this)
        myCoursesRef = myCoursesRef
            .child("student")
            .child(auth)
            .child("classes")
    }

    private fun checkHasIntent() {
        if (intent.hasExtra("class_detail")) {
            try {
                val classDetail = intent.getStringExtra("class_detail")
                val json: HashMap<String, String> = Json.decodeFromString(classDetail!!)
                readClass(json)
            } catch (e: Exception) {
                println("Exception ****************************** ${e.printStackTrace()}")
            }
        } else readClassData()
    }

    private fun readClassData() {
        myCoursesList = arrayListOf()
        myCoursesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                loadingListener.notLoadingProgressBar()
                val classInfo = snapshot.getValue(ClassInfo::class.java)
                myCoursesList.add(classInfo!!)
                rvInit()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val courseList = snapshot.getValue(ClassInfo::class.java)
                myCoursesList.add(courseList!!)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                println("onChildRemoved *********************** ${snapshot.value}")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                println("onChildMoved ************************* ${snapshot.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                println("error **************************** ${error.message}")
            }
        })
    }

    private fun readClass(any: HashMap<String, String>) {
        loadingListener.loadingProgressBar()
        myCoursesList = arrayListOf()
        try {
            classRef = classRef
                .child("teacher")
                .child(any["auth"] as String)
                .child("classes")
                .child(any["classCode"]!!)

            registerRef = registerRef
                .child("teacher")
                .child(any["auth"] as String)
                .child("registered_students")
                .child(any["classCode"]!!)
                .child(auth)


            myCoursesRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val courseList = snapshot.getValue(ClassInfo::class.java)
                    myCoursesList.add(courseList!!)

                    //Register User for Teacher to see
                    registerRef.setValue(FirebaseAuth.getInstance().currentUser?.email).addOnCompleteListener {

                    }
                    rvInit()
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val courseList = snapshot.getValue(ClassInfo::class.java)
                    myCoursesList.add(courseList!!)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    println("onChildRemoved *********************** ${snapshot.value}")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    println("onChildMoved ************************* ${snapshot.value}")
                }

                override fun onCancelled(error: DatabaseError) {
                    println("error **************************** ${error.message}")
                }
            })

            classRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val course = (snapshot.value)
                    myCoursesRef.push().setValue(course).addOnCompleteListener {
                        myCoursesList = arrayListOf()
                        myCoursesRef.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(
                                snapshot: DataSnapshot, previousChildName: String?,
                            ) {
                                loadingListener.notLoadingProgressBar()
                                val classInfo = snapshot.getValue(ClassInfo::class.java)
                                myCoursesList.add(classInfo!!)
                                rvInit()
                            }

                            override fun onChildChanged(
                                snapshot: DataSnapshot,
                                previousChildName: String?,
                            ) {
                                val courseList = snapshot.getValue(ClassInfo::class.java)
                                myCoursesList.add(courseList!!)
                                rvInit()
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                println("onChildRemoved *********************** ${snapshot.value}")
                            }

                            override fun onChildMoved(
                                snapshot: DataSnapshot,
                                previousChildName: String?,
                            ) {
                                println("onChildMoved ************************* ${snapshot.value}")
                            }

                            override fun onCancelled(error: DatabaseError) {
                                println("error **************************** ${error.message}")
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        } catch (e: Exception) {
            println(" **************************** ${e.printStackTrace()}")
        }
    }

    private fun rvInit() {
        adapter.dataSet = myCoursesList
        println("rvInit ************************** $myCoursesList")
        binding.rvMyClasses.adapter = adapter
        binding.rvMyClasses.layoutManager = LinearLayoutManager(applicationContext, VERTICAL, false)
        adapter.activity = this
    }

    override fun loadingProgressBar() {
        binding.myClassProgressbar.visibility = View.VISIBLE
    }

    override fun notLoadingProgressBar() {
        binding.myClassProgressbar.visibility = View.INVISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.student_fab -> {
                supportFragmentManager.beginTransaction().addToBackStack("new_course")
                    .replace(R.id.student_my_courses_root_layout, JoinClass()).commit()
            }
        }
    }
}

class MyClassesAdapter : RecyclerView.Adapter<MyClassesAdapter.ViewHolder>() {

    var dataSet: ArrayList<ClassInfo> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.student_class_image)
        val className: TextView = itemView.findViewById(R.id.student_class_name)
        val teacherInChargeName: TextView = itemView.findViewById(R.id.student_year)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.my_classes_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataSet[position]
        println("AClass ************************** ${datum.className}")
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
            val intent =
                Intent(context, AClass::class.java)
            val json = Json.encodeToString(datum)
            intent.putExtra("class_data", json)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount(): Int = dataSet.size
}