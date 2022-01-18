package com.iodaniel.mobileclass.student_package

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.StudentPageBinding
import com.iodaniel.mobileclass.shared_classes.ActivityMyClasses

class StudentInitPage : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy {
        StudentPageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.studentJoinClass.setOnClickListener(this)
        binding.studentMyCourses.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.student_join_class -> {
                supportFragmentManager.beginTransaction().addToBackStack("join_class")
                    .replace(R.id.student_page_root, FragmentJoinClass()).commit()
            }
            R.id.student_my_courses -> {
                val intent = Intent(this, ActivityMyClasses::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                overridePendingTransition(0,0)
            }
        }
    }
}