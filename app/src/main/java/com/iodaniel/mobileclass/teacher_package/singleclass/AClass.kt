package com.iodaniel.mobileclass.teacher_package.singleclass

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.AClassBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.student_package.HelperListener
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AClass : FragmentActivity(), OnClickListener, HelperListener.LoadingListener {

    private val binding by lazy {
        AClassBinding.inflate(layoutInflater)
    }
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var classInfo: ClassInfo
    private lateinit var classKey: String
    private lateinit var loadingListener: HelperListener.LoadingListener
    private var courseRef = FirebaseDatabase.getInstance().reference
    private var allRegStudentRef = FirebaseDatabase.getInstance().reference
    private var allClassesRef = FirebaseDatabase.getInstance().reference
    var dataset: ArrayList<Fragment> = arrayListOf()
    private val dialog by lazy { Dialog(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setActionBar(binding.aClassToolbar)
        loadingListener = this
        binding.aClassBack.setOnClickListener(this)
        binding.copyClassCode.setOnClickListener(this)

        if (intent.hasExtra("class_data")) {
            try {
                loadingListener.loadingProgressBar()
                val json = intent.getStringExtra("class_data")!!
                classKey = intent.getStringExtra("class_data_key")!!
                //TODO: IN DEVELOPMENT-> REMOVE CLASS FROM STUDENT AND TEACHER
                classInfo = Json.decodeFromString(json)

                val bundle = Bundle()
                bundle.putString("classInfo", json)
                val studentFragment = StudentFragment()
                studentFragment.arguments = bundle
                val lessonsFragment = LessonsFragment()
                lessonsFragment.arguments = bundle
                val assignments = FragmentAssignments()
                assignments.arguments = bundle

                dataset = arrayListOf(studentFragment, lessonsFragment, assignments)
                binding.className.text = classInfo.className
                binding.classCode.text = classInfo.classCode
                loadingListener.notLoadingProgressBar()
                viewPager()

                courseRef = courseRef
                    .child("teacher")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("classes")
                    .child(classInfo.classCode)

                allRegStudentRef = allRegStudentRef
                    .child("teacher")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("registered_students")
                    .child(classInfo.classCode)

            } catch (e: Exception) {
                println("INTENT EXCEPTION *************************** ${e.printStackTrace()}")
            }
        }
    }

    private fun viewPager() {
        val data = arrayListOf("Student", "Lessons", "Assignments")
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPagerAdapter.dataset = dataset
        binding.aClassViewpager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.aClassTabLayout, binding.aClassViewpager) { tab, position ->
            tab.text = data[position]
        }.attach()
    }

    inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        lateinit var dataset: ArrayList<Fragment>
        override fun getItemCount(): Int = dataset.size
        override fun createFragment(position: Int): Fragment {
            return dataset[position]
        }
    }

    private fun inflateCreateNewLessonFragment() {
        val fragmentCreateNewLesson = FragmentCreateNewLesson()
        val bundle = Bundle()
        val json = Json.encodeToString(classInfo)
        bundle.putString("classInfo", json)
        fragmentCreateNewLesson.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.a_class_frame, fragmentCreateNewLesson)
            .addToBackStack("newClass").commit()
    }

    private fun inflateCreateNewAssignment() {
        val createNewAssignmentFragment = CreateNewAssignment()
        val bundle = Bundle()
        val json = Json.encodeToString(classInfo)
        bundle.putString("classInfo", json)
        createNewAssignmentFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.a_class_frame, createNewAssignmentFragment)
            .addToBackStack("newAssi").commit()
    }

    private fun copyToClipBoard() {
        val txt = binding.classCode.text.toString()
        val clip = ClipData.newPlainText("Copied Text", txt)
        val clipBoardService =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipBoardService.setPrimaryClip(clip)
    }

    private fun deleteClass() {
        val view = layoutInflater.inflate(R.layout.delete, null)
        val alertDialog = AlertDialog.Builder(this, R.style.WarningDialogs)
        alertDialog.setPositiveButton("Delete") { dialog, _ ->
            loadingListener.loadingProgressBar()
            courseRef.removeValue()
            allRegStudentRef.removeValue()

            allClassesRef = allClassesRef
                .child("class_codes")
                .child(classInfo.classCodePushId)
            allClassesRef.removeValue()
            onBackPressed()
            dialog.dismiss()
            loadingListener.notLoadingProgressBar()
        }.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }.setView(view)
        alertDialog.setMessage("Are you sure?")
        alertDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.a_class_menu_teacher, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.create_new_lesson -> {
                inflateCreateNewLessonFragment()
                return true
            }
            R.id.create_new_assignment -> {
                inflateCreateNewAssignment()
                return true
            }
            R.id.menu_delete -> {
                deleteClass()
                return true
            }
            else -> {
                return false
            }
        }
    }

    override fun loadingProgressBar() {
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun notLoadingProgressBar() {
        dialog.dismiss()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.a_class_back -> {
                onBackPressed()
            }
            R.id.copy_class_code -> {
                copyToClipBoard()
                Snackbar.make(binding.copyClassCode, "Copied!!!", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
