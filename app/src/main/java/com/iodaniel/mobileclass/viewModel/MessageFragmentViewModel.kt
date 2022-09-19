package com.iodaniel.mobileclass.viewModel

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessageFragmentViewModel : ViewModel() {
    var cancelFunction = MutableLiveData<Pair<Unit, Boolean>>()
    var okFunction = MutableLiveData<Pair<Pair<Fragment, Int>, Boolean>>()
    var okText = MutableLiveData<String>()
    var cancelText = MutableLiveData<String>()
    var displayText = MutableLiveData<String>()
    var showAgain = MutableLiveData<Boolean>()
    var editTextInput = MutableLiveData<String>()

    fun setShowAgain(input: Boolean) {
        showAgain.value = input
    }

    fun setEditTextInput(input: String) {
        editTextInput.value = input
    }

    fun setCancelFunction(inputFunction: Pair<Unit, Boolean>) {
        cancelFunction.value = inputFunction
    }

    fun setOkFunction(inputFunction: Pair<Pair<Fragment, Int>, Boolean>) {
        okFunction.value = inputFunction
    }

    fun setOkText(inputText: String) {
        okText.value = inputText
    }

    fun setCancelText(inputText: String) {
        cancelText.value = inputText
    }

    fun setDisplayText(inputText: String) {
        displayText.value = inputText
    }
}

object OkFunctions{
    val NAVIGATE = 0
    val CLOSE = 1
}