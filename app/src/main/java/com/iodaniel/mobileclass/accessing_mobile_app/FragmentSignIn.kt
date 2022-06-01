package com.iodaniel.mobileclass.accessing_mobile_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.SignInBinding
import com.iodaniel.mobileclass.repository.RegistrationRepo

class FragmentSignIn : Fragment(), View.OnClickListener, HelperListener.LoadingListener {
    private lateinit var binding: SignInBinding
    private lateinit var registrationRepo: RegistrationRepo
    private var accountType = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SignInBinding.inflate(inflater, container, false)
        binding.signIn.setOnClickListener(this)
        registrationRepo = RegistrationRepo(requireContext(), requireActivity(), binding.root, accountType, loadingListener = this, viewLifecycleOwner)
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_in -> registrationRepo.signIn(
                email = binding.signupEmail.text.trim().toString(),
                password = binding.signupPassword.text.trim().toString()
            )
        }
    }

    override fun loadingProgressBar() {
        binding.signInText.visibility = View.INVISIBLE
        binding.signInProgressbar.visibility = View.VISIBLE
    }

    override fun notLoadingProgressBar() {
        binding.signInText.visibility = View.VISIBLE
        binding.signInProgressbar.visibility = View.INVISIBLE
    }
}