package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel: ViewModel(), LifecycleObserver {
    var age = MutableLiveData<String>()
    var gender = MutableLiveData<String>()
    init {
        age.value = ""
        gender.value = ""
    }
    fun setAge(inputAge:String){
        age.value = inputAge
    }
    fun setGender(inputGender:String){
        gender.value = inputGender
    }
}