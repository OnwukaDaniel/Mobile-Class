package com.iodaniel.mobileclass.teacher_package.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.databinding.FragmentManageUploadAuditedBinding

class FragmentManageUploadAudited : Fragment() {
    private lateinit var binding: FragmentManageUploadAuditedBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentManageUploadAuditedBinding.inflate(inflater, container, false)
        return binding.root
    }
}