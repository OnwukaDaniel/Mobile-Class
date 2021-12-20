package com.iodaniel.mobileclass.student_package

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.iodaniel.mobileclass.databinding.ActivityAclassBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AClass : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityAclassBinding.inflate(layoutInflater)
    }
    private lateinit var classInfo: ClassInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
        getIntentData()
    }

    private fun init() {

    }

    private fun getIntentData() {
        if (intent.hasExtra("class_data")) {
            val json = intent.getStringExtra("class_data")
            classInfo = Json.decodeFromString(json!!)
            binding.studentAClassName.text = classInfo.className
            println("************************ ${classInfo.classCode}")
        }
        viewPagerInit()
    }

    private fun viewPagerInit() {
        val adapter = ViewPagerAdapter(this)
        binding.studentAClassViewPager.adapter = adapter
        val dataNames = arrayListOf<String>("Course Work", "Assignments")
        TabLayoutMediator(binding.studentAClassTabLayout,
            binding.studentAClassViewPager) { tab, position ->
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

    override fun onClick(v: View?) {

    }
}