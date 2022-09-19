package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iodaniel.mobileclass.data_class.PersonRatingData

class RatingDisplayViewModel : ViewModel() {
    var ratingDisplayList = MutableLiveData<ArrayList<PersonRatingData>>()
    fun setRatingDisplayList(input: ArrayList<PersonRatingData>){
        ratingDisplayList.value = input
    }
}