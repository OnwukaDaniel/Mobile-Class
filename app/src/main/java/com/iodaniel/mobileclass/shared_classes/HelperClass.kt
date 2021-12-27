package com.iodaniel.mobileclass.shared_classes

import android.os.Environment
import com.iodaniel.mobileclass.shared_classes.HelperClass.FileDownloader.downloadFile
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class HelperClass {
    object FileDownloader {
        private const val MEGABYTE = 1024 * 1024
        fun downloadFile(fileUrl: String?, directory: File?) {
            try {
                val url = URL(fileUrl)
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection

                urlConnection.connect()
                val inputStream: InputStream = urlConnection.inputStream
                val fileOutputStream = FileOutputStream(directory)
                val totalSize: Int = urlConnection.contentLength
                println("TOTAL FILE LENGTH ************************ ${totalSize}")
                val buffer = ByteArray(MEGABYTE)
                var bufferLength: Int
                while (inputStream.read(buffer).also { bufferLength = it } > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength)
                }
                fileOutputStream.close()
                println("IT IS DONE ************************ ${totalSize}")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    fun requestDownload(url: String, saveName: String, extension: String){
        val fileUrl: String = url // -> http://maven.apache.org/maven-1.x/maven.pdf

        val fileName: String = saveName + extension // -> maven.pdf

        val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
        val folder = File(extStorageDirectory, "testthreepdf")
        folder.mkdir()

        val pdfFile = File(folder, fileName)

        try {
            pdfFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        downloadFile(fileUrl, pdfFile)
        return
    }
}