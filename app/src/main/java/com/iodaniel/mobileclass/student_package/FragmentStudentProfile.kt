package com.iodaniel.mobileclass.student_package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.databinding.FragmentStudentProfileBinding

class FragmentStudentProfile : Fragment() {
  private lateinit var binding: FragmentStudentProfileBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding= FragmentStudentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
}