package com.iodaniel.mobileclass.util

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.iodaniel.mobileclass.R

class Dialogs{
    fun circularProgressDialog(color: Int = Color.parseColor("#FFA742"), text: String = "Please wait...", activity: Activity): Dialog {
        val view = activity.layoutInflater.inflate(R.layout.progress_dialog, null, false)
        val cardView: CardView = view.findViewById(R.id.cp_cardview)
        val progressText: TextView = view.findViewById(R.id.cp_title)
        progressText.text = text
        cardView.setCardBackgroundColor(color)
        view.visibility = View.VISIBLE
        val dialogs = Dialog(activity)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialogs.addContentView(view, params)
        dialogs.setCancelable(false)
        dialogs.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogs.show()
        return dialogs
    }

    fun networkErrorDialog(color: Int = Color.parseColor("#FFA742"), text: String = "No network connection", activity: Activity): Dialog {
        val view = activity.layoutInflater.inflate(R.layout.network_error_dialog, null, false)
        val rootView: ConstraintLayout = view.findViewById(R.id.msg_root_view)
        val progressText: TextView = view.findViewById(R.id.msg_title)
        rootView.setBackgroundColor(color)
        val dialogs = Dialog(activity)
        dialogs.setCancelable(true)
        progressText.text = text
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialogs.addContentView(view, params)
        dialogs.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogs.show()
        return dialogs
    }

    fun messageDialog(color: Int = Color.parseColor("#FFA742"), text: String = "No network connection", activity: Activity): Dialog {
        val view = activity.layoutInflater.inflate(R.layout.message_dialog, null, false)
        val rootView: ConstraintLayout = view.findViewById(R.id.msg_root_view)
        val progressText: TextView = view.findViewById(R.id.msg_title)
        rootView.setBackgroundColor(color)
        val dialogs = Dialog(activity)
        dialogs.setCancelable(true)
        progressText.text = text
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialogs.addContentView(view, params)
        dialogs.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return dialogs
    }

    fun successDialog(color: Int = Color.parseColor("#FFFFFFFF"), text: String = "No network connection", activity: Activity): Dialog {
        val view = activity.layoutInflater.inflate(R.layout.success_dialog, null, false)
        val rootView: ConstraintLayout = view.findViewById(R.id.success_root_view)
        val progressText: TextView = view.findViewById(R.id.success_title)
        rootView.setBackgroundColor(color)
        val dialogs = Dialog(activity)
        dialogs.setCancelable(true)
        progressText.text = text
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialogs.addContentView(view, params)
        dialogs.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogs.show()
        return dialogs
    }
}