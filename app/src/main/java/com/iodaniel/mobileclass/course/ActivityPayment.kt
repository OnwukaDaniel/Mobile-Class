package com.iodaniel.mobileclass.course

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.StudentDetails
import com.iodaniel.mobileclass.databinding.ActivityPaymentBinding
import com.iodaniel.mobileclass.viewModel.CourseCardViewModel
import com.iodaniel.mobileclass.viewModel.PaymentViewModel

class ActivityPayment : AppCompatActivity(), View.OnClickListener {
    private val binding by lazy { ActivityPaymentBinding.inflate(layoutInflater) }
    private var courseCardData = CourseCardData()
    private lateinit var pref: SharedPreferences
    private val courseCardViewModel: CourseCardViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        pref = getSharedPreferences(getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        getUserDetail()
        val json = intent.getStringExtra("course card data")
        courseCardData = Gson().fromJson(json, CourseCardData::class.java)
        courseCardViewModel.setCC(courseCardData)
        binding.paymentBack.setOnClickListener(this)
        supportFragmentManager.beginTransaction().replace(R.id.payment_frame, FragmentPay1()).commit()
        binding.paymentIndLevel2Kite.adjustViewBounds = true
        binding.paymentIndLevel3Kite.adjustViewBounds = true
        paymentViewModel.indicatorLevel.observe(this) {
            when (it) {
                1 -> clearAll()
                2 -> {
                    clearAll()
                    binding.paymentIndLine1.setBackgroundColor(Color.BLACK)
                    binding.paymentIndLevel2Kite.setImageResource(R.drawable.ic_kite_filled)
                    //binding.paymentIndLevel2Kite.maxWidth = 14
                    //binding.paymentIndLevel2Kite.maxHeight = 14
                }
                3 -> {
                    clearAll()
                    binding.paymentIndLine1.setBackgroundColor(Color.BLACK)
                    binding.paymentIndLine2.setBackgroundColor(Color.BLACK)
                    binding.paymentIndLevel2Kite.setImageResource(R.drawable.ic_kite_filled)
                    binding.paymentIndLevel3Kite.setImageResource(R.drawable.ic_kite_filled)
                    //binding.paymentIndLevel2Kite.maxWidth = 14
                    //binding.paymentIndLevel2Kite.maxHeight = 14
                    //binding.paymentIndLevel3Kite.maxWidth = 14
                    //binding.paymentIndLevel3Kite.maxHeight = 14
                }
            }
        }
    }

    private fun getUserDetail() {
        val userInfoJson = pref.getString(getString(R.string.student_details), "")
        val user = Gson().fromJson(userInfoJson, StudentDetails::class.java)
        binding.paymentStudentName.text = user.fullName
    }

    private fun clearAll() {
        binding.paymentIndLine1.setBackgroundColor(Color.GRAY)
        binding.paymentIndLine2.setBackgroundColor(Color.GRAY)
        binding.paymentIndLevel2Kite.setImageResource(R.drawable.ic_kite)
        binding.paymentIndLevel3Kite.setImageResource(R.drawable.ic_kite)
        binding.paymentIndLevel2Kite.maxWidth = 24
        binding.paymentIndLevel3Kite.maxWidth = 24
        binding.paymentIndLevel2Kite.maxHeight = 24
        binding.paymentIndLevel3Kite.maxHeight = 24
    }

    override fun onBackPressed() {
        paymentViewModel.setIndicatorLevel(supportFragmentManager.backStackEntryCount)
        super.onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.payment_back -> {
                val count = supportFragmentManager.backStackEntryCount
                if (count == 0) super.onBackPressed() else supportFragmentManager.popBackStack()
                paymentViewModel.setIndicatorLevel(supportFragmentManager.backStackEntryCount)
            }
        }
    }
}