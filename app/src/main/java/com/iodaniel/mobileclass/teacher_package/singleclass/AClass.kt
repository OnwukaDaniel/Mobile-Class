package com.iodaniel.mobileclass.teacher_package.singleclass

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
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
        binding.aClassMore.setOnClickListener(this)
        if (intent.hasExtra("class_data")) {
            try {
                val json = intent.getStringExtra("class_data")!!
                classInfo = Json.decodeFromString(json)
                binding.className.text = classInfo.className
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
        viewPagerAdapter.dataset = arrayListOf(StudentFragment(classInfo), LessonsFragment(classInfo), Assignments(classInfo))
        TabLayoutMediator(binding.aClassTabLayout, binding.aClassViewpager) { tab, position ->
            tab.text = data[position]
        }.attach()
    }

    inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        var dataset: ArrayList<Fragment> =
            arrayListOf(StudentFragment(classInfo), LessonsFragment(classInfo), Assignments(classInfo))

        override fun getItemCount(): Int = dataset.size
        override fun createFragment(position: Int): Fragment {
            return dataset[position]
        }
    }

    private fun inflateCreateNewLessonFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.a_class_frame, CreateNewLessonFragment(classInfo))
            .addToBackStack("newClass").commit()
    }

    private fun inflateCreateNewAssignment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.a_class_frame, CreateNewAssignment(classInfo))
            .addToBackStack("newAssi").commit()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.a_class_more -> {
                val popUp = PopupMenu(this, binding.aClassMore, Gravity.CENTER)
                popUp.menuInflater.inflate(R.menu.a_class_menu, popUp.menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.create_new_lesson -> {
                            inflateCreateNewLessonFragment()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.create_new_assignment -> {
                            inflateCreateNewAssignment()
                            return@setOnMenuItemClickListener true
                        }
                        else -> {
                            return@setOnMenuItemClickListener false
                        }
                    }
                }
                popUp.show()
            }
        }
    }
}
