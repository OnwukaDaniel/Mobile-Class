package com.iodaniel.mobileclass

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iodaniel.mobileclass.class_assignment_upload.`class`.Classes


class SplashScreen: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.database.setPersistenceEnabled(true)
        val intent = Intent(this, Classes::class.java)
        startActivity(intent)
        overridePendingTransition(0,0)
    }
}