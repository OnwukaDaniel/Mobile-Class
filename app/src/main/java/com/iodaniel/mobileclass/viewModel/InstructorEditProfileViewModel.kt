package com.iodaniel.mobileclass.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iodaniel.mobileclass.data_class.InstructorDetails
import java.io.ByteArrayInputStream

class InstructorEditProfileViewModel : ViewModel() {
    var image = MutableLiveData<Pair<ByteArrayInputStream, ByteArray>>()
    var fullName = MutableLiveData<String>()
    var username = MutableLiveData<String>()
    var about = MutableLiveData<String>()
    val stream = MutableLiveData<ByteArrayInputStream>()
    var fileName = MutableLiveData<String>()
    var instructorIdentificationHash = MutableLiveData<HashMap<String, String>>()
    var instructorCertificationHash = MutableLiveData<HashMap<String, String>>()
    var certificateImage = MutableLiveData<Pair<ByteArrayInputStream, ByteArray>>()
    var identificationImage = MutableLiveData<Pair<ByteArrayInputStream, ByteArray>>()
    var certificateVerified = MutableLiveData<Boolean>()
    var identificationVerified = MutableLiveData<Boolean>()
    var instructorDetails = MutableLiveData<InstructorDetails>()

    fun setImageUri(inputImage: Pair<ByteArrayInputStream, ByteArray>) {
        image.value = inputImage
    }

    fun setFullName(inputFullName: String) {
        fullName.value = inputFullName
    }

    fun setInstructorDetails(inputInstructorDetails: InstructorDetails) {
        instructorDetails.value = inputInstructorDetails
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

    fun setStream(inputStream: ByteArrayInputStream) {
        stream.value = inputStream
    }

    fun setFileName(inputName: String) {
        fileName.value = inputName
    }

    fun setInstructorIdentificationHash(inputInstructorIdentificationHash: HashMap<String, String>) {
        instructorIdentificationHash.value = inputInstructorIdentificationHash
    }

    fun setInstructorCertificationHash(inputInstructorCertificationHash: HashMap<String, String>) {
        instructorCertificationHash.value = inputInstructorCertificationHash
    }

    fun setIdentificationImage(inputIdentificationImage: Pair<ByteArrayInputStream, ByteArray>) {
        identificationImage.value = inputIdentificationImage
    }

    fun setCertificateVerified(inputCertificateVerified: Boolean) {
        certificateVerified.value = inputCertificateVerified
    }

    fun setIdentificationVerified(inputIdentificationVerified: Boolean) {
        identificationVerified.value = inputIdentificationVerified
    }
}