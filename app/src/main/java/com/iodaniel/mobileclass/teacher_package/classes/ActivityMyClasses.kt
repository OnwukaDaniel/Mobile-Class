package com.iodaniel.mobileclass.teacher_package.classes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.SignInOrSignUp
import com.iodaniel.mobileclass.databinding.ActivityMyClassBinding
import com.iodaniel.mobileclass.shared_classes.FragmentAccountSettings
import com.iodaniel.mobileclass.teacher_package.HelperListener.ClassListener
import com.iodaniel.mobileclass.teacher_package.singleclass.AClass
import com.iodaniel.mobileclass.teacher_package.styling_package.FragmentChangeTheme
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ActivityMyClasses : AppCompatActivity(), View.OnClickListener, ClassListener, OnNavigationItemSelectedListener {

    private val binding by lazy {
        ActivityMyClassBinding.inflate(layoutInflater)
    }
    private lateinit var rvAdapter: MyClassesAdapter
    private lateinit var classListener: ClassListener
    private var listOfCourses: ArrayList<ClassInfo> = arrayListOf()
    private var myCoursesKeyList: ArrayList<String> = arrayListOf()
    private var reference = FirebaseDatabase.getInstance().reference
        .child("teacher")
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child("classes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMyClassses)
        title = "Classes"
        initialise()
        runBlocking {
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                readDatabase()
                //THis is to emulate checking internet connectivity. After initial submission it will be replaced.
                delay(2000)
                runOnUiThread {
                    classListener.nonEmptyClass()
                }
            }
        }
    }

    private fun initialise() {
        binding.fabMyClassAddClasses.setOnClickListener(this)
        binding.drawerIconMyClass.setOnClickListener(this)
        classListener = this
        binding.navViewTeacherPage.setNavigationItemSelectedListener(this)
    }

    private fun readDatabase() {
        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val courses = (snapshot.value as HashMap<*, *>)
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
                    myCoursesKeyList.add(snapshot.key!!)
                    initRv()
                    classListener.nonEmptyClass()
                } catch (e: Exception) {
                    println("ERROR  *************************** ${e.printStackTrace()}")
                }
            }

            @SuppressLint("NotifyDataSetChanged")
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
                    myCoursesKeyList.add(snapshot.key!!)
                    rvAdapter.notifyDataSetChanged()
                    classListener.nonEmptyClass()
                } catch (e: Exception) {
                    println("ERROR  *************************** ${e.printStackTrace()}")
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = myCoursesKeyList.indexOf(snapshot.key!!)
                myCoursesKeyList.removeAt(index)
                listOfCourses.removeAt(index)
                rvAdapter.notifyItemRemoved(index)
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
        rvAdapter.myCoursesKeyList = myCoursesKeyList
        rvAdapter.context = applicationContext
        rvAdapter.activity = this
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_my_classes -> {
                Snackbar.make(binding.root, "My Classes", Snackbar.LENGTH_LONG).show()
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
            R.id.menu_account_settings -> {
                runBlocking {
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        binding.drawerMyClassesRoot.closeDrawer(GravityCompat.START)
                        delay(700)
                        runOnUiThread {
                            supportFragmentManager.beginTransaction()
                                .addToBackStack("accountSettings")
                                .replace(R.id.drawer_my_classes_root, FragmentAccountSettings(true)).commit()
                        }
                    }
                }
                return true
            }
            else -> {
                return false
            }
        }
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount<1) {
            super.onBackPressed()
            finish()
        } else{
            supportFragmentManager.popBackStack()
        }
    }

    override fun emptyClass() {
        binding.myClassProgressbar.visibility = View.GONE
        binding.emptyContainer.visibility = View.VISIBLE
        binding.rvListOfCourses.visibility = View.INVISIBLE
    }

    override fun nonEmptyClass() {
        binding.myClassProgressbar.visibility = View.GONE
        binding.emptyContainer.visibility = View.INVISIBLE
        binding.rvListOfCourses.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.drawer_icon_my_class -> {
                binding.drawerMyClassesRoot.openDrawer(GravityCompat.START)
            }
            R.id.fab_my_class_add_classes -> {
                val intent = Intent(applicationContext, ClassUpload::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
    }
}

class MyClassesAdapter : RecyclerView.Adapter<MyClassesAdapter.MyCoursesAdapterViewHolder>() {

    var dataset: ArrayList<ClassInfo> = arrayListOf()
    var myCoursesKeyList: ArrayList<String> = arrayListOf()
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
        val classKey = myCoursesKeyList[position]
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
            val json = Json.encodeToString(datum)
            intent.putExtra("class_data", json)
            intent.putExtra("class_key", classKey)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount(): Int = dataset.size
}