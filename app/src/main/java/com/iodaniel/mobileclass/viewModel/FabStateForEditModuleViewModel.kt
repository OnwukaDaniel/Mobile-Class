package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iodaniel.mobileclass.viewModel.FabStateForEditModule.CLOSED

class FabStateForEditModuleViewModel : ViewModel() {
    var state = MutableLiveData<Int>()
    init {
        state.value = CLOSED
    }
    fun setState(inputState: Int) {
        state.value = inputState
    }
}
object FabStateForEditModule{
    const val OPEN = 0
    const val CLOSED = 1
}