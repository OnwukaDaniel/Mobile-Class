package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iodaniel.mobileclass.data_class.PlanModulesExercise

class PMEViewModel: ViewModel() {
    var planModulesExercise = MutableLiveData<ArrayList<PlanModulesExercise>>()
    var singlePlanModulesExercise = MutableLiveData<PlanModulesExercise>()
    fun setPME(inputPME: ArrayList<PlanModulesExercise>){
        planModulesExercise.value = inputPME
    }
    fun setSinglePME(input: PlanModulesExercise){
        singlePlanModulesExercise.value = input
    }
}