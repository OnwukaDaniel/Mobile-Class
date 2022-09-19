package com.iodaniel.mobileclass.repository

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.ModuleData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.teacher_package.course.FragmentModulesAndPlans
import com.iodaniel.mobileclass.util.Dialogs
import com.iodaniel.mobileclass.util.dialog_fragment.MessageFragment
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel
import kotlinx.coroutines.*

class EditPlanRepo(val activity: Activity, val context: Context, val view: View, val lifecycleOwner: LifecycleOwner) {

    fun uploadPlan(
        ref: DatabaseReference,
        planModulesExercise: ArrayList<PlanModulesExercise>,
        view: View,
        courseCardDataJson: String,
        mfV: MessageFragmentViewModel,
        fragmentEditPlansRoot: Int,
    ) {
        val d = Dialogs().circularProgressDialog(text = "Please wait...", activity = activity)
        val scope = CoroutineScope(Dispatchers.IO)
        var saved = false
        var showDialogFragment = true

        ref.setValue(planModulesExercise).addOnSuccessListener {
            saved = true
            d.dismiss()
            if (showDialogFragment) {
                val mf = MessageFragment()
                (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .addToBackStack("dialog")
                    .replace(fragmentEditPlansRoot, mf)
                    .commit()
                val fragment = FragmentModulesAndPlans()
                val bundle = Bundle()
                bundle.putString(context.getString(R.string.manage_course_data_intent), courseCardDataJson)
                fragment.arguments = bundle
                mfV.showAgain.observe(lifecycleOwner) { showDialogFragment = it }
                mfV.setDisplayText("Saved. Start adding content to plans submitted\n\nAdd content?")
                mfV.setOkFunction((fragment to fragmentEditPlansRoot) to true)
            }
        }.addOnFailureListener {
            d.dismiss()
            Toast.makeText(context, "Unable to save! Retry", Toast.LENGTH_LONG).show()
        }
        runBlocking {
            scope.launch {
                delay(15_000)
                if (!saved) {
                    activity.runOnUiThread { Snackbar.make(view, "Network error. Retry", Snackbar.LENGTH_LONG).show() }
                    d.dismiss()
                }
            }
        }
    }

    fun uploadModule(headerText: String, content: String, courseCode: String, position: Int, urisInputList: ArrayList<MutableMap<String, String>>, view: View) {
        if (content == "") {
            Snackbar.make(view, "Content cannot be empty", Snackbar.LENGTH_LONG).show()
            return
        }
        if (headerText == "") {
            Snackbar.make(view, "Header cannot be empty", Snackbar.LENGTH_LONG).show()
            return
        }
        val ref = FirebaseDatabase.getInstance().reference
            .child(context.getString(R.string.pme_ref))
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(courseCode)
            .child(position.toString())
            .child("modules")

        val uriList: ArrayList<MutableMap<String, String>> = arrayListOf()
        val confirmHttps: ArrayList<String> = arrayListOf()
        val progressMax = urisInputList.size
        val pair = showProgressBar(view)
        val progressDialog = pair.first
        val progressIndicator = pair.second
        progressDialog?.show()
        for (i in urisInputList) if (i["data"]!!.startsWith("https")) confirmHttps.add(i["data"]!!)
        if (confirmHttps.size == urisInputList.size)  {
            uploadOnlyContent(headerText, content, ref, urisInputList, view, progressDialog!!)
            return
        }

        if (urisInputList.isNotEmpty())
            for ((index, i) in urisInputList.withIndex()) {
                val uri = i["data"]
                if (uri!!.startsWith("https")) {
                    uriList.add(i)
                    continue
                }
                val strRef = FirebaseStorage.getInstance().reference
                    .child(context.getString(R.string.pme_ref))
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child(courseCode)
                    .child(position.toString())
                    .child("modules")
                    .child("uris")
                    .child(i["data"]!!.replace("/", "-"))

                val uploadTask = strRef.putFile(Uri.parse(uri))
                uploadTask.continueWith { task ->
                    if (!task.isSuccessful) task.exception?.let {
                        progressDialog?.dismiss()
                        throw it
                    }
                    strRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        strRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val uriMap = i.toMutableMap()
                            uriMap["data"] = downloadUri.toString()
                            uriList.add(uriMap)
                            if (index == uriList.size - 1) {
                                val module = ModuleData(content = content, uris = uriList, extraNote = headerText)

                                ref.setValue(module).addOnSuccessListener {
                                    if (index == progressMax - 1) {
                                        Snackbar.make(view, "Upload Successful", Snackbar.LENGTH_LONG).show()
                                        progressDialog?.dismiss()
                                    }
                                }.addOnFailureListener {
                                    val snackBar = Snackbar.make(view, "Network error", Snackbar.LENGTH_LONG)
                                    snackBar.setAction("Retry") {

                                    }
                                    snackBar.show()
                                    progressDialog?.dismiss()
                                }
                            }
                        }
                    }
                }.addOnFailureListener {
                    val snackBar = Snackbar.make(view, "Network error", Snackbar.LENGTH_LONG)
                    snackBar.setAction("Retry") {

                    }
                    snackBar.show()
                    progressDialog?.dismiss()
                }
                uploadTask.addOnProgressListener { it->
                    val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                    progressIndicator.progress = progress.toInt()
                }.addOnPausedListener {
                }
            }
    }

    private fun uploadOnlyContent(
        extraNote: String, content: String, ref: DatabaseReference, urisInputList: ArrayList<MutableMap<String, String>>, view: View,
        progressDialog: AlertDialog,
    ) {
        val module = ModuleData(content = content, uris = urisInputList, extraNote = extraNote)

        ref.setValue(module).addOnSuccessListener {
            Snackbar.make(view, "Upload Successful", Snackbar.LENGTH_LONG).show()
            progressDialog.dismiss()
        }.addOnFailureListener {
            val snackBar = Snackbar.make(view, "Network error", Snackbar.LENGTH_LONG)
            snackBar.setAction("Retry") {

            }
            snackBar.show()
            progressDialog.dismiss()
        }
    }

    private fun showProgressBar(view: View): Pair<AlertDialog?, ProgressBar> {
        val progressLayout = LayoutInflater.from(context).inflate(R.layout.determinate_progress_bar, null, false)
        val progressBar: ProgressBar = progressLayout.findViewById(R.id.determinate_progress_bar_progress)
        val dialog: AlertDialog.Builder = AlertDialog.Builder(context)
        dialog.setView(view).setCancelable(false)
        dialog.setView(progressLayout)
        return dialog.create() to progressBar
    }
}