package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentCreateDirectQuestionBinding

class CreateDirectQuestion : Fragment() {

    private lateinit var binding: FragmentCreateDirectQuestionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateDirectQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }
}