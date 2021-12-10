package com.iodaniel.mobileclass.class_assignment_upload

import android.net.Uri

class ClassMaterialUploadInterface {
    interface ProgressBarController{
        fun showProgressBar()
        fun hideProgressBar()
    }
    interface MediaSupport{
        fun videoPlayer(uri: Uri)
        fun pdfReader(uri: Uri)
        fun musicReader(uri: Uri)
        fun imageReader(uri: Uri)
        fun youTubePlayer()
        fun makeMediaPlayersInvisible()

        fun listOfMediaListener(listLength: Int)
    }
}