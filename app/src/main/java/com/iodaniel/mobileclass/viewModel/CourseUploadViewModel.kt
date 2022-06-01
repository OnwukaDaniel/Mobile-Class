package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CourseUploadViewModel: ViewModel(), LifecycleObserver {
    var level = MutableLiveData<String>()
    init {
        level.value = ""
    }
    fun setLevel(inputLevel:String){
        level.value = inputLevel
    }
}