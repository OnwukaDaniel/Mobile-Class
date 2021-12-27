package com.iodaniel.mobileclass

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iodaniel.mobileclass.accessing_mobile_app.SignInOrSignUp
import com.iodaniel.mobileclass.student_package.StudentInitPage
import com.iodaniel.mobileclass.teacher_package.classes.ActivityMyClasses

class SplashScreen : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private val auth = FirebaseAuth.getInstance().currentUser
    private val teacher = "teacher"
    private val student = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = getSharedPreferences("userType", Context.MODE_PRIVATE)
        val userType = pref.getString("studentTeacher", "")
        println("*************************************** $userType")

        try {
            Firebase.database.setPersistenceEnabled(true)
        } catch (e: Exception) {

        }
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val newUserIntent = Intent(this, SignInOrSignUp::class.java)
        newUserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val teacherUserIntent = Intent(this, ActivityMyClasses::class.java)
        teacherUserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val studentUserIntent = Intent(this, StudentInitPage::class.java)
        studentUserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        when (firebaseUser) {
            null -> {
                startActivity(newUserIntent)
                overridePendingTransition(0, 0)
            }
            else -> when (userType) {
                "" -> {
                    startActivity(newUserIntent)
                    overridePendingTransition(0, 0)
                }
                teacher -> {
                    startActivity(teacherUserIntent)
                    overridePendingTransition(0, 0)
                }
                student -> {
                    startActivity(studentUserIntent)
                    overridePendingTransition(0, 0)
                }
            }
        }
    }
}