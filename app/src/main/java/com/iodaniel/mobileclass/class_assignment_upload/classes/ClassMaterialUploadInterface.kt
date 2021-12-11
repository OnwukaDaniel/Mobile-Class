package com.iodaniel.mobileclass.class_assignment_upload.classes

import android.net.Uri

class ClassMaterialUploadInterface {
    interface ProgressBarController{
        fun showProgressBar()
        fun hideProgressBar()
    }
    interface MediaSupport{
        fun imageReader(uri: Uri)
        fun makeMediaPlayersInvisible()
    }
}