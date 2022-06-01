package com.iodaniel.mobileclass.teacher_package.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityTeacherProfileBinding

class ActivityTeacherProfile : AppCompatActivity(), View.OnClickListener {
    private val binding by lazy { ActivityTeacherProfileBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.teacherProfileContinue.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.teacher_profile_continue -> {
                startActivity(Intent(this, ActivityEditProfile::class.java))
                overridePendingTransition(0, 0)
            }
        }
    }
}