package com.iodaniel.mobileclass.class_assignment_upload.singleclass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.StudentFragmentBinding

class StudentFragment : Fragment() {
    private lateinit var binding: StudentFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        binding = StudentFragmentBinding.inflate(layoutInflater, container, false)
        return inflater.inflate(R.layout.fragment_student_framgment, container, false)
    }
}