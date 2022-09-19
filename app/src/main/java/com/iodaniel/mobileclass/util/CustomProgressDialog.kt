package com.iodaniel.mobileclass.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.iodaniel.mobileclass.R

class CustomProgressDialog {

    lateinit var dialog: CustomDialog

    fun show(context: Context, activity: Activity, title: String = ""): Dialog {
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.progress_dialog, null)
        val cpTitle: TextView = view.findViewById(R.id.cp_title)
        val cpCardView: CardView = view.findViewById(R.id.cp_cardview)
        if (title != null) {
            cpTitle.text = title
        }
        cpCardView.setCardBackgroundColor(Color.parseColor("#70000000"))
        cpTitle.setTextColor(Color.WHITE)

        dialog = CustomDialog(activity)
        dialog.setContentView(view)
        dialog.show()
        return dialog
    }

    class CustomDialog(activity: Activity) : Dialog(activity) {
        init {
            // Set Semi-Transparent Color for Dialog Background
            window?.decorView?.rootView?.setBackgroundResource(R.color.app)
            window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
                insets.consumeSystemWindowInsets()
            }
        }
    }
}