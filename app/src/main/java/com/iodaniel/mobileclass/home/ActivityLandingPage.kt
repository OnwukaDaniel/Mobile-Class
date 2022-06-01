package com.iodaniel.mobileclass.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityLandingPageBinding
import com.iodaniel.mobileclass.liveDataClasses.LandingPageFragmentLiveData

class ActivityLandingPage : AppCompatActivity() {
    private val binding by lazy { ActivityLandingPageBinding.inflate(layoutInflater) }
    private val market = FragmentMarket()
    private val learning = FragmentMyLearning()
    private val user = FragmentUser()
    private var currentMenuItem = 0
    private var currentFragment = Fragment()
    private val landingPageFragmentLiveData = LandingPageFragmentLiveData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sfm = supportFragmentManager
        sfm.beginTransaction().replace(R.id.landing_frame, market).commit()
        sfm.beginTransaction().show(market).commit()
        landingPageFragmentLiveData.setCurrentFragment(market)

        landingPageFragmentLiveData.currentFragment.observe(this){
            currentFragment = it
        }

        landingPageFragmentLiveData.currentMenuItem.observe(this){
            currentMenuItem = it
        }

        binding.landingBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_my_classes -> {
                    sfm.beginTransaction().replace(R.id.landing_frame, market).commit()
                    landingPageFragmentLiveData.setCurrentFragment(market)
                    landingPageFragmentLiveData.setCurrentMenuItem(R.id.menu_my_classes)
                    true
                }
                R.id.menu_my_learning -> {
                    sfm.beginTransaction().replace(R.id.landing_frame, learning).commit()
                    landingPageFragmentLiveData.setCurrentFragment(learning)
                    landingPageFragmentLiveData.setCurrentMenuItem(R.id.menu_my_learning)
                    true
                }
                R.id.menu_my_profile -> {
                    sfm.beginTransaction().replace(R.id.landing_frame, user).commit()
                    landingPageFragmentLiveData.setCurrentFragment(user)
                    landingPageFragmentLiveData.setCurrentMenuItem(R.id.menu_my_profile)
                    true
                }
                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }
        binding.landingBottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.menu_my_classes -> {
                }
                R.id.menu_my_learning -> {
                }
                R.id.menu_my_profile -> {
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.landingBottomNav.selectedItemId = currentMenuItem
    }
}