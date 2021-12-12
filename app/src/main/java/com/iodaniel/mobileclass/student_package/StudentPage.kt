package com.iodaniel.mobileclass.student_package

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iodaniel.mobileclass.databinding.StudentPageBinding

class StudentPage : AppCompatActivity() {

    private val binding by lazy {
        StudentPageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}