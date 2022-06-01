package com.iodaniel.mobileclass.liveDataClasses

import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel

class LandingPageFragmentLiveData : ViewModel() {
    val currentFragment = MediatorLiveData<Fragment>()
    val currentMenuItem = MediatorLiveData<Int>()
    fun setCurrentFragment(inputFragment: Fragment) {
        currentFragment.value = inputFragment
    }
    fun setCurrentMenuItem(inputCurrentMenuItem: Int) {
        currentMenuItem.value = inputCurrentMenuItem
    }
}