package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.Question

class QuestionTransferViewModel: ViewModel() {
    var question = MutableLiveData<Question>()
    var courseCardData = MutableLiveData<CourseCardData>()
    fun setQuestion(input: Question){
        question.value=  input
    }
    fun setCourseCardData(input: CourseCardData){
        courseCardData.value=  input
    }
}