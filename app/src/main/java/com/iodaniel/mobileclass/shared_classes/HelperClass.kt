package com.iodaniel.mobileclass.shared_classes

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileNotFoundException

class HelperClass(val url: String, val classCode: String, val extension: String, val context: Context) {
    val uniqueName = url.split(extension)[0].split("/").last()
        .replace(".", "_")
        .replace("%", "_")
        .replace(":", "_")

    fun requestDownloadPath(): String {
            val customUri = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/$classCode/$uniqueName.$extension")
        return customUri.toString()
    }

    fun download(){
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            request.setTitle("Download")
            request.setDescription("Downloading File")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/$classCode/$uniqueName.$extension")
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
}