package com.iodaniel.mobileclass.course

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.iodaniel.mobileclass.databinding.FragmentPay3Binding
import com.iodaniel.mobileclass.viewModel.CourseCardViewModel

class FragmentPay3 : Fragment() {
    private lateinit var binding: FragmentPay3Binding
    private val courseCardViewModel: CourseCardViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPay3Binding.inflate(inflater, container, false)
        courseCardViewModel.courseCard.observe(viewLifecycleOwner) { courseCardData->
            binding.pay3Ok.setOnClickListener {
                val intent = Intent(requireContext(), ActivitySelectedCourse::class.java)
                intent.putExtra("courseCard", Gson().toJson(courseCardData))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
        return binding.root
    }
}