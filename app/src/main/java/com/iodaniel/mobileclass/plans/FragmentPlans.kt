package com.iodaniel.mobileclass.plans

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentPlansBinding

class FragmentPlans : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentPlansBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansBinding.inflate(inflater, container, false)
        binding.plansCancle.setOnClickListener(this)
        binding.plansBasic.setOnClickListener(this)
        binding.plansFree.setOnClickListener(this)
        binding.plansPremium.setOnClickListener(this)
        binding.plansGoToPlans.setOnClickListener(this)
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.plans_cancle -> requireActivity().onBackPressed()
            R.id.plans_free -> {}
            R.id.plans_basic -> {}
            R.id.plans_premium -> {}
            R.id.plans_go_to_plans -> {
                startActivity(Intent(requireContext(), ActivityPlans::class.java))
                requireActivity().overridePendingTransition(0, 0)
            }
        }
    }
}