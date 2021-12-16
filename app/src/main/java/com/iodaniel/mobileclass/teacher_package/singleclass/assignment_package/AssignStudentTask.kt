package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.databinding.FragmentAssignStudentTaskBinding

class AssignStudentTask : Fragment() {

    private lateinit var binding: FragmentAssignStudentTaskBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssignStudentTaskBinding.inflate(inflater, container, false)

        return binding.root
    }
}