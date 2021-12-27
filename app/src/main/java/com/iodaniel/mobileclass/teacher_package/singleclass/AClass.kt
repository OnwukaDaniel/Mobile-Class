package com.iodaniel.mobileclass.teacher_package.singleclass

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.AClassBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AClass : FragmentActivity(), View.OnClickListener {

    private val binding by lazy {
        AClassBinding.inflate(layoutInflater)
    }
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var classInfo: ClassInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setActionBar(binding.aClassToolbar)

        binding.aClassBack.setOnClickListener(this)
        if (intent.hasExtra("class_data")) {
            try {
                val json = intent.getStringExtra("class_data")!!
                classInfo = Json.decodeFromString(json)
                binding.className.text = classInfo.className
                binding.classCode.text = classInfo.classCode
            } catch (e: Exception) {
                println("INTENT EXCEPTION *************************** ${e.printStackTrace()}")
            }
        }
        viewPager()
    }

    private fun viewPager() {
        viewPagerAdapter = ViewPagerAdapter(this)
        binding.aClassViewpager.adapter = viewPagerAdapter
        val data = arrayListOf("Student", "Lessons", "Assignments")
        viewPagerAdapter.dataset = arrayListOf(
            StudentFragment(classInfo),
            LessonsFragment(classInfo),
            Assignments(classInfo)
        )
        TabLayoutMediator(binding.aClassTabLayout, binding.aClassViewpager) { tab, position ->
            tab.text = data[position]
        }.attach()
    }

    inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        var dataset: ArrayList<Fragment> =
            arrayListOf(
                StudentFragment(classInfo),
                LessonsFragment(classInfo),
                Assignments(classInfo)
            )

        override fun getItemCount(): Int = dataset.size
        override fun createFragment(position: Int): Fragment {
            return dataset[position]
        }
    }

    private fun inflateCreateNewLessonFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.a_class_frame, FragmentCreateNewLesson(classInfo))
            .addToBackStack("newClass").commit()
    }

    private fun inflateCreateNewAssignment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.a_class_frame, CreateNewAssignment(classInfo))
            .addToBackStack("newAssi").commit()
    }

    private fun deleteClass() {
        val view = layoutInflater.inflate(R.layout.delete, null)
        val alertDialog = AlertDialog.Builder(this, R.style.WarningDialogs)
        alertDialog.setPositiveButton("Delete") { dialog, which ->
            dialog.dismiss()
        }.setNegativeButton("Cancel") { dialog, which ->
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.a_class_back -> {
                onBackPressed()
            }
        }
    }
}
