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
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.teacher_package.course.FragmentModulesAndPlans
import kotlinx.coroutines.*

class EditPlanRepo(val activity: Activity, val context: Context, val view: View, val lifecycleOwner: LifecycleOwner) {

    fun uploadPlan(ref: DatabaseReference, planModulesExercise: ArrayList<PlanModulesExercise>, view: View, courseCardDataJson: String) {
        val pDialog = Util.progressDialog("Please wait...", context, activity)
        pDialog?.show()
        val scope = CoroutineScope(Dispatchers.IO)
        var saved = false
        ref.setValue(planModulesExercise).addOnSuccessListener {
            val snackBar = Snackbar.make(view, "Saved. Start adding content to plans submitted", Snackbar.LENGTH_LONG)
            snackBar.setAction("Add content") {
                val fragment = FragmentModulesAndPlans()
                val bundle = Bundle()
                bundle.putString(context.getString(R.string.manage_course_data_intent), courseCardDataJson)
                fragment.arguments = bundle
                (activity as AppCompatActivity).supportFragmentManager
                    .beginTransaction()
                    .addToBackStack("module")
                    .replace(R.id.fragment_edit_plans_root, fragment)
                    .commit()
            }
            snackBar.show()
            saved = true
            pDialog?.dismiss()
        }.addOnFailureListener {
            pDialog?.dismiss()
            Toast.makeText(context, "Unable to save! Retry", Toast.LENGTH_LONG).show()
        }
        runBlocking {
            scope.launch {
                delay(15_000)
                if (!saved) {
                    activity.runOnUiThread { Snackbar.make(view, "Network error. Retry", Snackbar.LENGTH_LONG).show() }
                    pDialog?.dismiss()
                }
            }
        }
    }

    fun uploadModule(extraNote: String, content: String, courseCode: String, position: Int, urisInputList: ArrayList<MutableMap<String, String>>, view: View) {
        if (content == "") {
            Snackbar.make(view, "Content cannot be empty", Snackbar.LENGTH_LONG).show()
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
        if (confirmHttps.size == urisInputList.size) uploadOnlyContent(extraNote, content, ref, urisInputList, view, progressDialog!!)

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
                            println("Entered here *************************************************** ${((index + 1) / progressMax) * 100}")
                            progressIndicator.progress = ((index + 1) / progressMax) * 100
                            if (index == urisInputList.size - 1) {
                                val module = ModuleData(content = content, uris = uriList, extraNote = extraNote)

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
                    println("Entered here *************************************************** 8")
                    val snackBar = Snackbar.make(view, "Network error", Snackbar.LENGTH_LONG)
                    snackBar.setAction("Retry") {

                    }
                    snackBar.show()
                    progressDialog?.dismiss()
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