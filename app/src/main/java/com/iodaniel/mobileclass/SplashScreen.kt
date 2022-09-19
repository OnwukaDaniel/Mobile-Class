package com.iodaniel.mobileclass

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.iodaniel.mobileclass.accessing_mobile_app.SignInOrSignUp
import com.iodaniel.mobileclass.home.ActivityLandingPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private val teacher = "teacher"
    private val student = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        pref = getSharedPreferences(getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        val userType = pref.getString(getString(R.string.studentTeacherPreference), "")
        println("*************************************** $userType")

        CoroutineScope(Dispatchers.IO).launch {
            delay(2500)
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val newUserIntent = Intent(this@SplashScreen, SignInOrSignUp::class.java)
            newUserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            val userIntent = Intent(this@SplashScreen, ActivityLandingPage::class.java)
            userIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            when (firebaseUser) {
                null -> startActivity(newUserIntent)
                else ->  startActivity(userIntent)
            }
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onResume() {
        super.onResume()
        window.statusBarColor = resources.getColor(R.color.app_primary_color)
    }
}