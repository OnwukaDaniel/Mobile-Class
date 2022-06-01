package com.iodaniel.mobileclass.util

import android.content.Context
import java.io.File

class DirectoryManagement {
    fun createDirectory(path: String, data: String, context: Context): File {
        val file = File("${context.filesDir}/$path")
        if (!file.exists()) file.mkdirs()
        val directory = File(file, "/${data}")
        println("************************************************************************ ${directory.exists()}")
        if (!directory.exists()) directory.createNewFile()
        return directory
    }
}