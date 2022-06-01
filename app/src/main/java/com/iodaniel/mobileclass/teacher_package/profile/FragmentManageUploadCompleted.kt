package com.iodaniel.mobileclass.teacher_package.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.databinding.FragmentManageUploadCompletedBinding

class FragmentManageUploadCompleted : Fragment() {
    private lateinit var binding: FragmentManageUploadCompletedBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentManageUploadCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }
}