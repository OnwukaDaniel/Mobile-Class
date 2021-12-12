package com.iodaniel.mobileclass.teacher_package.singleclass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.iodaniel.mobileclass.databinding.AClassBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AClass : AppCompatActivity() {

    private val binding by lazy {
        AClassBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (intent.hasExtra("class_data")) {
            try{
                val json = intent.getStringExtra("class_data")!!
                val msg: ClassInfo = Json.decodeFromString(json)
                println("INTENT *************************** ${msg.className}")
            } catch (e:Exception){
                println("INTENT EXCEPTION *************************** ${e.printStackTrace()}")
            }
        }
        tabLayout()
    }

    private fun tabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                TODO("Not yet implemented")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                TODO("Not yet implemented")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                TODO("Not yet implemented")
            }
        })
    }
}
