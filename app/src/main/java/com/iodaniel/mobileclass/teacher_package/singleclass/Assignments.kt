package com.iodaniel.mobileclass.teacher_package.singleclass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.databinding.AssignmentBinding

class Assignments : Fragment() {
    private lateinit var binding: AssignmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = AssignmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}