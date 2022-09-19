package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iodaniel.mobileclass.data_class.OwnedCourse

class OwnedCourseViewModel : ViewModel() {
    var ownedCourseList = MutableLiveData<ArrayList<OwnedCourse>>()
    fun setOwnedCourseList(input: ArrayList<OwnedCourse>) {
        ownedCourseList.value = input
    }
}