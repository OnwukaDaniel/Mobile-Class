package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PaymentViewModel : ViewModel() {
    var indicatorLevel = MutableLiveData(0)
    fun setIndicatorLevel(input: Int) {
        indicatorLevel.value = input
    }
}