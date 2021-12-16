package com.iodaniel.mobileclass.teacher_package.singleclass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.CreateNewAssignmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.AssignStudentTask
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.CreateDirectQuestion
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.CreateMultiChoice
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.UploadDocs

class CreateNewAssignment(private val classInfo: ClassInfo) : Fragment(), View.OnClickListener {

    private lateinit var binding: CreateNewAssignmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = CreateNewAssignmentBinding.inflate(inflater, container, false)
        binding.newAssignmentUpload.setOnClickListener(this)
        binding.newAssignmentAssignStudentTask.setOnClickListener(this)
        binding.newAssignmentDirectQ.setOnClickListener(this)
        binding.newAssignmentMultiChoice.setOnClickListener(this)
        return binding.root
    }

    fun newFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("")
            .replace(R.id.a_class_frame, fragment)
            .commit()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.new_assignment_assign_student_task -> {
                newFragment(AssignStudentTask())
            }
            R.id.new_assignment_direct_q -> {
                newFragment(CreateDirectQuestion())
            }
            R.id.new_assignment_multi_choice -> {
                newFragment(CreateMultiChoice(classInfo))
            }
            R.id.new_assignment_upload -> {
                newFragment(UploadDocs())
            }
        }
    }
}