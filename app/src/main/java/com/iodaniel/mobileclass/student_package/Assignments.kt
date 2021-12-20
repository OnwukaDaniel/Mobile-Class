package com.iodaniel.mobileclass.student_package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.databinding.FragmentAssignmentsBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo


class Assignments(val classInfo: ClassInfo) : Fragment() {
    private lateinit var binding: FragmentAssignmentsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssignmentsBinding.inflate(inflater, container, false)

        return binding.root
    }
}