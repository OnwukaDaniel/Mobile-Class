package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EducationViewModel: ViewModel() {
    var education = MutableLiveData<String>()
    fun setEducation(input: String){
        education.value = input
    }
}