package com.iodaniel.mobileclass.teacher_package.course

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityManageUploadBinding
import com.iodaniel.mobileclass.teacher_package.profile.FragmentManageUploadAudited
import com.iodaniel.mobileclass.teacher_package.profile.FragmentManageUploadCompleted
import com.iodaniel.mobileclass.teacher_package.profile.FragmentManageUploadSaved
import com.iodaniel.mobileclass.teacher_package.profile.ManageProfileCourseType

class ActivityManageUpload : AppCompatActivity() {
    private val binding by lazy { ActivityManageUploadBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (intent.hasExtra(getString(R.string.manage_course_data_intent))) {
            val sfm = supportFragmentManager.beginTransaction()
            when (intent.getIntExtra(getString(R.string.manage_course_data_intent), ManageProfileCourseType.SAVED)) {
                ManageProfileCourseType.COMPLETE -> sfm.replace(R.id.manage_upload_root, FragmentManageUploadCompleted()).commit()
                ManageProfileCourseType.SAVED -> sfm.replace(R.id.manage_upload_root, FragmentManageUploadSaved()).commit()
                ManageProfileCourseType.AUDITED -> sfm.replace(R.id.manage_upload_root, FragmentManageUploadAudited()).commit()
            }
        }
    }
}