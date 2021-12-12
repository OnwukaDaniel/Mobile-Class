package com.iodaniel.mobileclass.accessing_mobile_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.SignInOrSignUpBinding

class SignInOrSignUp : AppCompatActivity() {

    private val binding by lazy{
        SignInOrSignUpBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.welcomeSignUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            overridePendingTransition(0,0)
        }
        binding.welcomeSignIn.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
            overridePendingTransition(0,0)
        }
    }
}