package com.iodaniel.mobileclass

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.iodaniel.mobileclass.accessing_mobile_app.SignInOrSignUp
import com.iodaniel.mobileclass.home.ActivityLandingPage
import com.iodaniel.mobileclass.student_package.StudentInitPage
import java.util.*

class SplashScreen : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private val teacher = "teacher"
    private val student = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = getSharedPreferences(getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        val userType = pref.getString(getString(R.string.studentTeacherPreference), "")
        println("*************************************** $userType")

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val newUserIntent = Intent(this, SignInOrSignUp::class.java)
        newUserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val teacherUserIntent = Intent(this, ActivityLandingPage::class.java)
        teacherUserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val studentUserIntent = Intent(this, StudentInitPage::class.java)
        studentUserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        when (firebaseUser) {
            null -> startActivity(newUserIntent)
            else -> when (userType.toString().lowercase(Locale.getDefault())) {
                "" -> startActivity(newUserIntent)
                teacher -> startActivity(teacherUserIntent)
                student -> startActivity(studentUserIntent)
            }
        }
        overridePendingTransition(0, 0)
    }
}