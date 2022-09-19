package com.iodaniel.mobileclass.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.OwnedCourse
import com.iodaniel.mobileclass.databinding.FragmentPay2Binding
import com.iodaniel.mobileclass.util.Dialogs
import com.iodaniel.mobileclass.util.uistate_manager.UIStateDialogs
import com.iodaniel.mobileclass.viewModel.CourseCardViewModel
import com.iodaniel.mobileclass.viewModel.PaymentViewModel
import com.iodaniel.mobileclass.viewModel.UIStateViewModel
import java.util.*

class FragmentPay2 : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentPay2Binding
    private val paymentViewModel by activityViewModels<PaymentViewModel>()
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private var studentCoursesRef = FirebaseDatabase.getInstance().reference
    private lateinit var uIStateDialogs: UIStateDialogs
    private val courseCardViewModel: CourseCardViewModel by activityViewModels()
    private var uIStateViewModel = UIStateViewModel()
    private var courseCardData = CourseCardData()
    private var dialogs = Dialogs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPay2Binding.inflate(inflater, container, false)
        uIStateDialogs = UIStateDialogs(uIStateViewModel, requireContext(), viewLifecycleOwner, requireActivity())
        studentCoursesRef = studentCoursesRef.child("studentData").child(auth).child("ownedCourses")
        binding.pay1Payment.setOnClickListener(this)
        courseCardViewModel.courseCard.observe(viewLifecycleOwner) {
            courseCardData = it
            binding.pay2Price.text = it.price
        }
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.pay1_payment -> {
                val time = Calendar.getInstance().timeInMillis.toString()
                val ownedCourse = OwnedCourse(time, courseCardData.courseCode, courseCardData.instructorInChargeUID)
                uIStateDialogs.uiState(false, dialogs)
                studentCoursesRef.child(time).setValue(ownedCourse).addOnSuccessListener {
                    uIStateDialogs.uiState(true, dialogs)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .addToBackStack("pay3")
                        .replace(R.id.pay2_root, FragmentPay3())
                        .commit()
                    paymentViewModel.setIndicatorLevel(3)
                }.addOnFailureListener {
                    Snackbar.make(binding.root, "Check network connection and try again", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}