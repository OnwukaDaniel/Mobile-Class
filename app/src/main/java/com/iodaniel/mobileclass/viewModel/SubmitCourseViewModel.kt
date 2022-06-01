package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SubmitCourseViewModel: ViewModel() {
    var noPlans = MutableLiveData<String>()
    var noModules = MutableLiveData<String>()
    var noMedias = MutableLiveData<String>()
    var noExercises = MutableLiveData<String>()

    fun setPlans(inputPlans: String){
        noPlans.value = inputPlans
    }

    fun setModules(inputModules: String){
        noModules.value = inputModules
    }

    fun setMedias(inputMedias: String){
        noMedias.value = inputMedias
    }

    fun setExercises(inputExercises: String){
        noExercises.value = inputExercises
    }
}