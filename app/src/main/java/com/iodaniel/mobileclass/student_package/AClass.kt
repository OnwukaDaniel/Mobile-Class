package com.iodaniel.mobileclass.student_package

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityAclassBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AClass : AppCompatActivity(), OnClickListener, HelperListener.LoadingListener {

    private val binding by lazy {
        ActivityAclassBinding.inflate(layoutInflater)
    }
    private lateinit var classInfo: ClassInfo
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private var myClassesRef = FirebaseDatabase.getInstance().reference
    private lateinit var loadingListener: HelperListener.LoadingListener
    private val dialog by lazy { Dialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.aClassToolbar)
        binding.aClassToolbar.setOnClickListener(this)
        loadingListener = this
        getIntentData()
    }

    private fun getIntentData() {
        if (intent.hasExtra("class_data")) {
            val json = intent.getStringExtra("class_data")
            val classKey = intent.getStringExtra("class_data_key")
            classInfo = Json.decodeFromString(json!!)
            binding.studentAClassName.text = classInfo.className

            myClassesRef = myClassesRef
                .child("student")
                .child(auth)
                .child("classes")
                .child(classKey!!)
        }
        viewPagerInit()
    }

    private fun viewPagerInit() {
        val adapter = ViewPagerAdapter(this)
        binding.studentAClassViewPager.adapter = adapter
        val dataNames = arrayListOf("Course Work", "Assignments")
        TabLayoutMediator(
            binding.studentAClassTabLayout,
            binding.studentAClassViewPager
        ) { tab, position ->
            tab.text = dataNames[position]
        }.attach()
    }

    inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        var dataset = arrayListOf(CourseWork(classInfo), Assignments(classInfo))
        override fun getItemCount(): Int = dataset.size

        override fun createFragment(position: Int): Fragment {
            return dataset[position]
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.a_class_menu_student, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.leave_class -> {
                val view = layoutInflater.inflate(R.layout.delete, null, false)
                val alertDialog =
                    AlertDialog.Builder(this, R.style.WarningDialogs)
                alertDialog.setView(view)
                    .setTitle("Are you sure?")
                alertDialog.setPositiveButton("Delete") { dialog, which ->
                    loadingListener.loadingProgressBar()
                    myClassesRef.removeValue()
                    loadingListener.notLoadingProgressBar()
                    onBackPressed()
                    dialog.dismiss()
                }.setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                alertDialog.show()
                return true
            }
            else -> return false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.a_class_toolbar -> {
                onBackPressed()
                overridePendingTransition(0, 0)
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
}
interface AssignmentViewTypeListener{
    fun multiChoiceView()
    fun questionsOnlyView()
    fun fileQuestionsView()
    fun resultMultiChoiceView()
}