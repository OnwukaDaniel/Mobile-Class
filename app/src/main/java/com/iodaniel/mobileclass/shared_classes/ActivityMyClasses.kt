package com.iodaniel.mobileclass.shared_classes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.SignInOrSignUp
import com.iodaniel.mobileclass.databinding.ActivityMyClassBinding
import com.iodaniel.mobileclass.student_package.AClass
import com.iodaniel.mobileclass.student_package.FragmentJoinClass
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassUpload
import com.iodaniel.mobileclass.teacher_package.styling_package.FragmentChangeTheme
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ActivityMyClasses : AppCompatActivity(), OnClickListener,
    LoadingListener, TeacherStudentListener, OnNavigationItemSelectedListener, DrawerStateListener {

    private lateinit var pref: SharedPreferences
    private val binding by lazy { ActivityMyClassBinding.inflate(layoutInflater) }
    private lateinit var loadingListener: LoadingListener
    private var reference = FirebaseDatabase.getInstance().reference

    private lateinit var teacherStudentListener: TeacherStudentListener
    private lateinit var drawerStateListener: DrawerStateListener
    private lateinit var internetConnection: InternetConnection
    private var myCoursesList: ArrayList<ClassInfo> = arrayListOf()
    private var myCoursesKeyList: ArrayList<String> = arrayListOf()
    private val adapter = MyClassesAdapter()
    private var userType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMyClassses)
        init()
    }

    private fun init() {
        val viewH = binding.navViewTeacherPage.getHeaderView(0)
        val textH: TextView = viewH.findViewById(R.id.header_teacher_page_email)
        textH.text = FirebaseAuth.getInstance().currentUser?.email

        binding.drawerIconMyClass.setOnClickListener(this)
        binding.activityClassLogOut.setOnClickListener(this)
        binding.fabMyClassAddClasses.setOnClickListener(this)
        binding.drawerMyClassesRoot.setBackgroundColor(Color.parseColor("#EEE0FF"))
        loadingListener = this
        teacherStudentListener = this
        drawerStateListener = this
        binding.navViewTeacherPage.setNavigationItemSelectedListener(this)
        internetConnection = InternetConnection(applicationContext)
        pref = getSharedPreferences("userType", Context.MODE_PRIVATE)
        userType = pref.getString("studentTeacher", "")!!
        when (userType) {
            "teacher" -> teacherStudentListener.teacherAccount()
            "student" -> teacherStudentListener.studentAccount()
        }
    }

    private fun readClassData() {
        myCoursesList = arrayListOf()
        reference.orderByChild("/datetime").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                loadingListener.notLoadingProgressBar()
                val classInfo = snapshot.getValue(ClassInfo::class.java)
                myCoursesList.add(classInfo!!)
                myCoursesKeyList.add(snapshot.key!!)
                if(myCoursesList.isNotEmpty()) loadingListener.occupiedData()
                rvInit()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = myCoursesKeyList.indexOf(snapshot.key!!)
                myCoursesKeyList.removeAt(index)
                myCoursesList.removeAt(index)
                adapter.notifyItemRemoved(index)
                if(myCoursesList.isEmpty()) loadingListener.emptyData()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                println("onChildMoved ************************* ${snapshot.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "Please retry!!!", Snackbar.LENGTH_LONG).show()
            }
        })
        if(myCoursesList.isEmpty()) loadingListener.emptyData()
    }

    private fun rvInit() {
        adapter.dataSet = myCoursesList
        adapter.myCoursesKeyList = myCoursesKeyList
        binding.rvListOfCourses.adapter = adapter
        binding.rvListOfCourses.layoutManager =
            LinearLayoutManager(applicationContext, VERTICAL, false)
        adapter.activity = this
        adapter.userType = userType
    }

    override fun loadingProgressBar() {
        binding.myClassProgressbar.visibility = View.VISIBLE
    }

    override fun notLoadingProgressBar() {
        binding.emptyContainer.visibility = View.GONE
        binding.myClassProgressbar.visibility = View.INVISIBLE
    }

    override fun emptyData() {
        binding.textView2Empty.visibility = View.VISIBLE
    }

    override fun occupiedData() {
        binding.textView2Empty.visibility = View.INVISIBLE
    }

    override fun teacherAccount() {
        reference = reference
            .child("teacher")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("classes")
        runBlocking {
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                readClassData()
                //THis is to emulate checking internet connectivity. After initial submission it will be replaced.
                delay(2000)
                runOnUiThread {
                    loadingListener.notLoadingProgressBar()
                }
            }
        }
    }

    override fun studentAccount() {
        reference = reference
            .child("student")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("classes")
        runBlocking {
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                readClassData()
                //THis is to emulate checking internet connectivity. After initial submission it will be replaced.
                delay(2000)
                runOnUiThread {
                    loadingListener.notLoadingProgressBar()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.change_theme -> {
                supportFragmentManager.beginTransaction().addToBackStack("styles")
                    .replace(R.id.drawer_my_classes_root, FragmentChangeTheme())
                    .commit()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_my_classes, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount < 1) {
            super.onBackPressed()
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_my_class_add_classes -> {
                when (userType) {
                    "teacher" -> {
                        val intent = Intent(applicationContext, ClassUpload::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                    "student" -> {
                        supportFragmentManager.beginTransaction().addToBackStack("new_course")
                            .replace(R.id.drawer_my_classes_root, FragmentJoinClass()).commit()
                    }
                }
            }
            R.id.drawer_icon_my_class -> {
                binding.drawerMyClassesRoot.openDrawer(GravityCompat.START)
                drawerStateListener.drawerOpened()
            }
            R.id.activity_class_log_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(applicationContext, SignInOrSignUp::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_my_classes -> {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    binding.drawerMyClassesRoot.closeDrawer(GravityCompat.START)
                } else {
                    supportFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    supportFragmentManager.popBackStack()
                    binding.drawerMyClassesRoot.closeDrawer(GravityCompat.START)
                }
                Snackbar.make(binding.root, "My Classes", Snackbar.LENGTH_LONG).show()
                return true
            }
            R.id.menu_account_settings -> {
                runBlocking {
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        binding.drawerMyClassesRoot.closeDrawer(GravityCompat.START)
                        drawerStateListener.drawerClosed()
                        delay(1_200)
                        runOnUiThread {
                            supportFragmentManager.beginTransaction()
                                .addToBackStack("accountSettings")
                                .replace(R.id.drawer_my_classes_root, FragmentAccountSettings())
                                .commit()
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    override fun drawerOpened() {
        binding.myClassScrim.visibility = View.VISIBLE
    }

    override fun drawerClosed() {
        binding.myClassScrim.visibility = View.GONE
    }
}

interface DrawerStateListener {
    fun drawerOpened()
    fun drawerClosed()
}

interface LoadingListener {
    fun loadingProgressBar()
    fun notLoadingProgressBar()
    fun emptyData()
    fun occupiedData()
}

class MyClassesAdapter : RecyclerView.Adapter<MyClassesAdapter.ViewHolder>() {

    var dataSet: ArrayList<ClassInfo> = arrayListOf()
    var myCoursesKeyList: ArrayList<String> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity
    var userType = ""

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.student_class_image)
        val className: TextView = itemView.findViewById(R.id.student_class_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.my_classes_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dataSet.reverse()
        myCoursesKeyList.reverse()
        val datum = dataSet[position]
        val datumKey = myCoursesKeyList[position]
        val imageUri = Uri.parse(datum.classImage)
        val red = datum.red
        val green = datum.green
        val blue = datum.blue

        Glide.with(context).load(imageUri).centerCrop().into(holder.image)
        holder.image.setColorFilter(Color.argb(90, red, green, blue))
        holder.className.text = datum.className
        holder.itemView.setOnClickListener {
            val json = Json.encodeToString(datum)
            var intent = Intent()
            when (userType) {
                "teacher" -> intent = Intent(
                    context,
                    com.iodaniel.mobileclass.teacher_package.singleclass.AClass::class.java
                )
                "student" -> intent = Intent(context, AClass::class.java)
            }
            intent.putExtra("class_data", json)
            intent.putExtra("class_data_key", datumKey)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount(): Int = dataSet.size
}