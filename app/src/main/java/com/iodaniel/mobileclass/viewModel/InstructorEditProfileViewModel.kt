package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.ByteArrayInputStream

class InstructorEditProfileViewModel : ViewModel() {
    var image = MutableLiveData<Pair<ByteArrayInputStream, ByteArray>>()
    var fullName = MutableLiveData<String>()
    var username = MutableLiveData<String>()
    var about = MutableLiveData<String>()
    var certificateImage = MutableLiveData<Pair<ByteArrayInputStream, ByteArray>>()
    var identificationImage = MutableLiveData<Pair<ByteArrayInputStream, ByteArray>>()

    /*init {
        imageUri.value =
        fullName.value = ""
        username.value = ""
        about.value = ""
        certificateImage.value = ""
        identificationImage.value = ""
    }*/

    fun setImageUri(inputImage: Pair<ByteArrayInputStream, ByteArray>) {
        image.value = inputImage
    }

    fun setFullName(inputFullName: String) {
        fullName.value = inputFullName
    }

    fun setUsername(inputUsername: String) {
        username.value = inputUsername
    }

    fun setAbout(inputAbout: String) {
        about.value = inputAbout
    }

    fun setCertificateImage(inputCertificateImage: Pair<ByteArrayInputStream, ByteArray>) {
        certificateImage.value = inputCertificateImage
    }

    fun setIdentificationImage(inputIdentificationImage: Pair<ByteArrayInputStream, ByteArray>) {
        identificationImage.value = inputIdentificationImage
    }
}