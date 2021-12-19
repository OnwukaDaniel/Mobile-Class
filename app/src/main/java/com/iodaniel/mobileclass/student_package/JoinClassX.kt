package com.iodaniel.mobileclass.student_package

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityJoinClassBinding
import com.iodaniel.mobileclass.databinding.StudentPageBinding

class JoinClassX : AppCompatActivity() {
    private val binding by lazy {
        ActivityJoinClassBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}