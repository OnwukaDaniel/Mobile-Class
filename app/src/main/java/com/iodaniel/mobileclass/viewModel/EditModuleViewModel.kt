package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditModuleViewModel : ViewModel() {
    var mediaPresenceListener = MutableLiveData<Boolean>(false)

    fun setMediaPresence(input: Boolean) {
        mediaPresenceListener.value = input
    }
}