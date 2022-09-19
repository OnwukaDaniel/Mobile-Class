package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.ByteArrayInputStream

class ByteOutputStreamViewModel : ViewModel() {
    val stream = MutableLiveData<ByteArrayInputStream>()
    fun setStream(inputStream: ByteArrayInputStream) {
        stream.value = inputStream
    }
}