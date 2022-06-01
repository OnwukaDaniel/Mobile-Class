package com.iodaniel.mobileclass.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.databinding.FragmentMyLearningBinding

class FragmentMyLearning : Fragment() {
    private lateinit var binding: FragmentMyLearningBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyLearningBinding.inflate(inflater, container, false)
        return binding.root
    }
}