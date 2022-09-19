package com.iodaniel.mobileclass.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentPay1Binding
import com.iodaniel.mobileclass.viewModel.CourseCardViewModel
import com.iodaniel.mobileclass.viewModel.PaymentViewModel

class FragmentPay1 : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentPay1Binding
    private val paymentViewModel by activityViewModels<PaymentViewModel>()
    private val courseCardViewModel: CourseCardViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPay1Binding.inflate(inflater, container, false)
        binding.pay1Payment.setOnClickListener(this)
        courseCardViewModel.courseCard.observe(viewLifecycleOwner){
            binding.pay1Price.text = it.price
            binding.pay1RatingBar.rating = if (it.rating == "") 0F else it.rating.toFloat()
            binding.pay1InstructorName.text = it.instructorName
            binding.pay1CourseName.text = it.courseName
        }
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.pay1_payment -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .addToBackStack("pay2")
                    .replace(R.id.pay1_root, FragmentPay2())
                    .commit()
                paymentViewModel.setIndicatorLevel(2)
            }
        }
    }
}