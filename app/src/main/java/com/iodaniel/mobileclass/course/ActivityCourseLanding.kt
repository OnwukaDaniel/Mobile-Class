package com.iodaniel.mobileclass.course

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.databinding.ActivityCourseLandingBinding
import com.iodaniel.mobileclass.student_package.owned_courses.FragmentContent
import com.iodaniel.mobileclass.student_package.owned_courses.FragmentOwnedExercise
import com.iodaniel.mobileclass.student_package.owned_courses.FragmentOwnedFeedback
import com.iodaniel.mobileclass.viewModel.CourseCardViewModel
import com.iodaniel.mobileclass.viewModel.QuestionTransferViewModel

class ActivityCourseLanding : AppCompatActivity() {
    private val binding by lazy { ActivityCourseLandingBinding.inflate(layoutInflater) }
    private lateinit var courseCardData: CourseCardData
    private val courseCardViewModel by viewModels<CourseCardViewModel>()
    private val questionTransferViewModel by viewModels<QuestionTransferViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val json = intent.getStringExtra("courseJson")
        courseCardData = Gson().fromJson(json, CourseCardData::class.java)
        Glide.with(this).load(courseCardData.courseImage).centerCrop().into(binding.courseImage)

        courseCardViewModel.setCC(courseCardData)
        questionTransferViewModel.setCourseCardData(courseCardData)
        val tabsText = arrayListOf("Content", "Exercise", "Feedback")
        val courseTabAdapter = CourseTabAdapter(this)
        courseTabAdapter.dataset = arrayListOf(FragmentContent(), FragmentOwnedExercise(), FragmentOwnedFeedback())
        binding.courseTablayout.setBackgroundColor(Color.TRANSPARENT)
        binding.courseViewpager.offscreenPageLimit = 3
        binding.courseViewpager.adapter = courseTabAdapter
        TabLayoutMediator(binding.courseTablayout, binding.courseViewpager) { tabs, position -> tabs.text = tabsText[position] }.attach()
    }

    inner class CourseTabAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        lateinit var dataset: ArrayList<Fragment>
        override fun getItemCount(): Int = dataset.size
        override fun createFragment(position: Int): Fragment {
            return dataset[position]
        }
    }
}

