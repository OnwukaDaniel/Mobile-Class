package com.iodaniel.mobileclass.shared_classes

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class DownloadHelper(val context: Context, val activity: Activity) {
    var listOfDownload: ArrayList<String> = arrayListOf()
    var datum=""
    private var extension=""
    var classCode=""

    fun downloadFromList(datum: String, extension: String, classCode: String, listOfDownload: ArrayList<String>, position: Int){
        this.listOfDownload= listOfDownload
        this.datum = datum
        this.extension = extension
        this.classCode = classCode
        try {
            val dir = File(listOfDownload[position])
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(dir.toString())
            val dirUri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                dir
            )
            if (dir.exists()) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType((dirUri), mime)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.startActivity(intent)
            } else {
                downloadAndOpenFile()
            }
        } catch (e: Exception) {
            println("Exception ------------------------------------- ${e.printStackTrace()}")
        }
    }
    private fun downloadAndOpenFile() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val helperClass = HelperClass(datum, classCode, extension, context)
            helperClass.download()
            val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val dir = File(helperClass.requestDownloadPath())
                    listOfDownload.add(dir.toString())

                    //AFTER DOWNLOAD IS DONE
                    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(dir.toString())
                    val dirUri = FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".provider",
                        dir
                    )
                    val viewIntent = Intent(Intent.ACTION_VIEW)
                    viewIntent.setDataAndType(dirUri, mime)
                    viewIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    viewIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.startActivity(viewIntent)
                }
            }
            activity.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }
}