package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SingleToggleViewModel:ViewModel() {
    var toggle = MutableLiveData(false)
    fun setToggle(input: Boolean){
        toggle.value = input
    }
}