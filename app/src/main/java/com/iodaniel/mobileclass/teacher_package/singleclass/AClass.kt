package com.iodaniel.mobileclass.teacher_package.singleclass

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.iodaniel.mobileclass.databinding.AClassBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AClass : FragmentActivity() {

    private val binding by lazy {
        AClassBinding.inflate(layoutInflater)
    }
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (intent.hasExtra("class_data")) {
            try {
                val json = intent.getStringExtra("class_data")!!
                val msg: ClassInfo = Json.decodeFromString(json)
                binding.className.text = msg.className
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
        viewPagerAdapter.dataset = arrayListOf(StudentFragment(), LessonsFragment(), Assignments())
        TabLayoutMediator(binding.aClassTabLayout, binding.aClassViewpager) { tab, position ->
            tab.text = data[position]
        }.attach()
    }

    inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        var dataset: ArrayList<Fragment> =
            arrayListOf(StudentFragment(), LessonsFragment(), Assignments())

        override fun getItemCount(): Int = dataset.size
        override fun createFragment(position: Int): Fragment {
            return dataset[position]
        }
    }
}
