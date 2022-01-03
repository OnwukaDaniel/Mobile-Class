package com.iodaniel.mobileclass.student_package

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.iodaniel.mobileclass.shared_classes.FragmentAccountSettings
import com.iodaniel.mobileclass.shared_classes.InternetConnection
import com.iodaniel.mobileclass.shared_classes.TeacherStudentListener
import com.iodaniel.mobileclass.student_package.HelperListener.LoadingListener
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.styling_package.FragmentChangeTheme
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ActivityMyClasses : AppCompatActivity(), OnClickListener, LoadingListener,
    TeacherStudentListener, OnNavigationItemSelectedListener, DrawerStateListener {

    private val binding by lazy {
        ActivityMyClassBinding.inflate(layoutInflater)
    }
    private lateinit var loadingListener: LoadingListener
    private var myClassesRef = FirebaseDatabase.getInstance().reference
        .child("student")
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child("classes")
        //.orderByChild("/datetime")
    private lateinit var teacherStudentListener: TeacherStudentListener
    private lateinit var drawerStateListener: DrawerStateListener
    private lateinit var internetConnection: InternetConnection
    private var myCoursesList: ArrayList<ClassInfo> = arrayListOf()
    private var myCoursesKeyList: ArrayList<String> = arrayListOf()
    private val adapter = MyClassesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMyClassses)
        init()
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

    private fun init() {
        binding.drawerIconMyClass.setOnClickListener(this)
        binding.fabMyClassAddClasses.setOnClickListener(this)
        binding.drawerMyClassesRoot.setBackgroundColor(Color.parseColor("#EEE0FF"))
        loadingListener = this
        teacherStudentListener = this
        drawerStateListener = this
        binding.navViewTeacherPage.setNavigationItemSelectedListener(this)
        internetConnection = InternetConnection(applicationContext)

        /*internetConnection.checkInternetConnection(object : InternetConnection.ConnectivityClass {
            override fun connected() {
                println("************************************************* Connected")
            }

            override fun disConnected() {
                loadingListener.notLoadingProgressBar()
                //Snackbar.make(binding.root, "Poor network connection", Snackbar.LENGTH_LONG).show()
                println("*************************************************")
            }
        })*/
    }

    private fun readClassData() {
        myCoursesList = arrayListOf()
        myClassesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                loadingListener.notLoadingProgressBar()
                val classInfo = snapshot.getValue(ClassInfo::class.java)
                myCoursesList.add(classInfo!!)
                myCoursesKeyList.add(snapshot.key!!)
                rvInit()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                loadingListener.notLoadingProgressBar()
                val classInfo = snapshot.getValue(ClassInfo::class.java)
                myCoursesList.add(classInfo!!)
                myCoursesKeyList.add(snapshot.key!!)
                rvInit()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = myCoursesKeyList.indexOf(snapshot.key!!)
                myCoursesKeyList.removeAt(index)
                myCoursesList.removeAt(index)
                adapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                println("onChildMoved ************************* ${snapshot.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                println("error **************************** ${error.message}")
            }
        })
    }

    private fun rvInit() {
        adapter.dataSet = myCoursesList
        adapter.myCoursesKeyList = myCoursesKeyList
        binding.rvListOfCourses.adapter = adapter
        binding.rvListOfCourses.layoutManager =
            LinearLayoutManager(applicationContext, VERTICAL, false)
        adapter.activity = this
    }

    override fun loadingProgressBar() {
        binding.myClassProgressbar.visibility = View.VISIBLE
    }

    override fun notLoadingProgressBar() {
        binding.emptyContainer.visibility = View.GONE
        binding.myClassProgressbar.visibility = View.INVISIBLE
    }

    override fun teacherAccount() {

    }

    override fun studentAccount() {

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
                supportFragmentManager.beginTransaction().addToBackStack("new_course")
                    .replace(R.id.drawer_my_classes_root, FragmentJoinClass()).commit()
            }
            R.id.drawer_icon_my_class -> {
                binding.drawerMyClassesRoot.openDrawer(GravityCompat.START)
                drawerStateListener.drawerOpened()
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
            R.id.menu_log_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(applicationContext, SignInOrSignUp::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                overridePendingTransition(0, 0)
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


class MyClassesAdapter : RecyclerView.Adapter<MyClassesAdapter.ViewHolder>() {

    var dataSet: ArrayList<ClassInfo> = arrayListOf()
    var myCoursesKeyList: ArrayList<String> = arrayListOf()
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
        val datumKey = myCoursesKeyList[position]
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
            intent.putExtra("class_data_key", datumKey)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount(): Int = dataSet.size
}