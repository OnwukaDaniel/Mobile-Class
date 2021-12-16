package com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentUploadDocsBinding

class UploadDocs : Fragment() {

    private lateinit var binding: FragmentUploadDocsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentUploadDocsBinding.inflate(inflater)
        return inflater.inflate(R.layout.fragment_upload_docs, container, false)
    }
}