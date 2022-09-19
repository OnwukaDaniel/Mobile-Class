package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UIStateViewModel : ViewModel() {
    var uIState = MutableLiveData<Int>(UiState.stateLoading)

    fun setUIState(inputDataState: Int) {
        uIState.value = inputDataState
    }
}

object UiState {
    var stateData = 0
    var stateLoading = 1
    var stateNoData = 2
    var stateNetworkError = 3
    var stateSuccess = 4
}