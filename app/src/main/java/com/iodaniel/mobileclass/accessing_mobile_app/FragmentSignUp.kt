package com.iodaniel.mobileclass.accessing_mobile_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentSignUpBinding
import com.iodaniel.mobileclass.repository.RegistrationRepo
import com.iodaniel.mobileclass.viewModel.SignUpViewModel

class FragmentSignUp : Fragment(), View.OnClickListener, HelperListener.LoadingListener {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var loadingListener: HelperListener.LoadingListener
    private var accountType = ""
    private val signUpViewModel = SignUpViewModel()
    private lateinit var registrationRepo: RegistrationRepo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        loadingListener = this
        accountType = requireArguments().getString("student_teacher")!!
        binding.signUp.setOnClickListener(this)
        binding.signUpAge.setOnClickListener(this)
        registrationRepo = RegistrationRepo(requireContext(), requireActivity(), binding.root, accountType,this, viewLifecycleOwner)
        signUpViewModel.age.observe(viewLifecycleOwner) {
            binding.signUpAge.text = it
        }
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_up -> registrationRepo.signUp(
                fullName = binding.signupFullName.text.trim().toString(),
                username = binding.signupUsername.text.trim().toString(),
                age = binding.signUpAge.text.trim().toString(),
                email = binding.signupEmail.text.trim().toString(),
                password = binding.signupPassword.text.trim().toString(),
                confirmPassword = binding.confirmPassword.text.trim().toString(),
            )
            R.id.sign_up_age -> {
                val pair = registrationRepo.agePicker()
                val view = pair.first
                val alertDialog = pair.second
                val age1: LinearLayout = view.findViewById(R.id.dialog_age_1)
                val age2: LinearLayout = view.findViewById(R.id.dialog_age_2)
                val age3: LinearLayout = view.findViewById(R.id.dialog_age_3)
                val age4: LinearLayout = view.findViewById(R.id.dialog_age_4)
                val age5: LinearLayout = view.findViewById(R.id.dialog_age_5)
                age1.setOnClickListener {
                    signUpViewModel.setAge("<17")
                    alertDialog.dismiss()
                }
                age2.setOnClickListener {
                    signUpViewModel.setAge("17-25")
                    alertDialog.dismiss()
                }
                age3.setOnClickListener {
                    signUpViewModel.setAge("26-40")
                    alertDialog.dismiss()
                }
                age4.setOnClickListener {
                    signUpViewModel.setAge("41-50")
                    alertDialog.dismiss()
                }
                age5.setOnClickListener {
                    signUpViewModel.setAge(">50")
                    alertDialog.dismiss()
                }
            }
        }
    }

    override fun loadingProgressBar() {
        binding.signUpText.visibility = View.INVISIBLE
        binding.signUpProgressbar.visibility = View.VISIBLE
    }

    override fun notLoadingProgressBar() {
        binding.signUpText.visibility = View.VISIBLE
        binding.signUpProgressbar.visibility = View.INVISIBLE
    }
}