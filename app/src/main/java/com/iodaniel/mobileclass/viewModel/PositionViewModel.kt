package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PositionViewModel: ViewModel() {
    var position = MutableLiveData<Int>(0)
    fun setPosition(input: Int){
        position.value= input
    }
}