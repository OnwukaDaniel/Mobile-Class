package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditCourseViewModel: ViewModel() {
    var plans = MutableLiveData<ArrayList<String>>()

    fun setPlans(plansList: ArrayList<String>){
        plans.value = plansList
    }
}