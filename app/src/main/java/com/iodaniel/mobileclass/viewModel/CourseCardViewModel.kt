package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iodaniel.mobileclass.data_class.CourseCardData

class CourseCardViewModel: ViewModel() {
    val courseCard = MutableLiveData<CourseCardData>()
    fun setCC(inputCC: CourseCardData){
        courseCard.value = inputCC
    }
}