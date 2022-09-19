package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CourseUploadProgressViewModel:ViewModel() {
    val state = MutableLiveData<Int>(CourseUploadState.STEP_ONE)

    fun setState(inputState: Int){
        state.value = inputState
    }
}
object CourseUploadState{
    var STEP_ONE = 0
    var STEP_TWO = 1
}