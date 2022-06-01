package com.iodaniel.mobileclass.shared_classes

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.webkit.MimeTypeMap
import com.iodaniel.mobileclass.util.CustomProgressDialog
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


object Util {
    fun functionTimeout(delay: Long, networkFunction: Unit): Boolean {
        val scope = CoroutineScope(Dispatchers.IO)
        val uploadTask = scope.async { networkFunction }
        val counterTask = scope.async { delay(delay) }
        if (counterTask.isCompleted && !uploadTask.isCompleted) {
            uploadTask.cancel("Error Timeout!!! Retry")
            return false
        }
        return true
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }

    fun digitToMonth(position: Int): String {
        val months = arrayListOf(
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"
        )
        return months[position]
    }

    fun getExtension(fileUri: Uri, context: Context): String {
        val contentResolver = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver?.getType(fileUri))!!
    }

    fun progressDialog(title: String, context: Context, activity: Activity): Dialog? {
        val progressDialog = CustomProgressDialog()
        return progressDialog.show(context, activity, title)
    }
}

object BlurBuilder {
    private const val BITMAP_SCALE = 0.4f
    private const val BLUR_RADIUS = 7.5f
    fun blur(context: Context?, image: Bitmap): Bitmap {
        val width = (image.width * BITMAP_SCALE).roundToInt()
        val height = (image.height * BITMAP_SCALE).roundToInt()
        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }
}