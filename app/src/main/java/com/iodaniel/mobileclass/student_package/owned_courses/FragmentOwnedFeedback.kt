package com.iodaniel.mobileclass.student_package.owned_courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.databinding.FragmentOwnedFeedbackBinding

class FragmentOwnedFeedback : Fragment() {
    private lateinit var binding: FragmentOwnedFeedbackBinding
    private lateinit var courseCardData: CourseCardData

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnedFeedbackBinding.inflate(inflater, container, false)

        return binding.root
    }
}