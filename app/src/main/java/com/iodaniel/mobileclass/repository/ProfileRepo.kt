package com.iodaniel.mobileclass.repository

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.data_class.StudentDetails
import com.iodaniel.mobileclass.liveDataClasses.InstructorProfileLiveData
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.util.Dialogs
import com.iodaniel.mobileclass.util.dialog_fragment.SuccessFragment
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream


class ProfileRepo(val context: Context, val activity: Activity) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val pref: SharedPreferences = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)

    fun decoupleTeacherSharedPreferenceProfile(): InstructorDetails {
        val json = pref.getString(context.getString(R.string.teacher_details), "")
        return Gson().fromJson(json, InstructorDetails::class.java)
    }

    fun decoupleStudentSharedPreferenceProfile(): StudentDetails {
        val json = pref.getString(context.getString(R.string.student_details), "")
        return Gson().fromJson(json, StudentDetails::class.java)
    }

    fun setSharedPreferenceDataToUi() {
        val json = pref.getString(context.getString(R.string.teacher_details), "")
        val teacherDetails = Gson().fromJson(json, InstructorDetails::class.java)
    }

    fun getInstructorProfile(viewLifecycleOwner: LifecycleOwner) {
        var instructorDetails: InstructorDetails? = null
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        val instructorDetailsRef = FirebaseDatabase.getInstance().reference
            .child(context.getString(R.string.instructor_details)) // CONFIRM
            .child(auth)

        InstructorProfileLiveData(instructorDetailsRef).observe(viewLifecycleOwner) {
            if (it.second == ChildEventTemplate.onDataChange) {
                instructorDetails = it.first
            }
        }
    }

    fun uploadBio(
        instructorName: String,
        email: String,
        username: String,
        dateJoined: String,
        description: String,
        displayImageStream: ByteArrayInputStream,
        instructorDetails: InstructorDetails,
    ) {
        var completeAction = false
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        val instructorDetailsRef = FirebaseDatabase.getInstance().reference
            .child(context.getString(R.string.instructor_details)) // CONFIRM
            .child(auth)
        val cn = InternetConnection(context)
        val dialog = Dialogs().circularProgressDialog(activity = activity)
        dialog.show()

        scope.launch {
            delay(10_000)
            if (!completeAction) {
                dialog.dismiss()
                activity.runOnUiThread { Toast.makeText(context, "No network connection!!! Retry", Toast.LENGTH_LONG).show() }
            }
        }

        cn.setCustomInternetListener(object : InternetConnection.CheckInternetConnection {
            override fun isConnected() {
                val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
                val displayImageRef = FirebaseStorage.getInstance().reference
                    .child(context.getString(R.string.INSTRUCTOR_DISPLAY_IMAGE))
                    .child(auth)
                val displayImageUploadTask = displayImageRef.putStream(displayImageStream)

                displayImageUploadTask.continueWith { task ->
                    if (!task.isSuccessful) task.exception?.let {
                        dialog.dismiss()
                        throw  it
                    }
                    displayImageRef.downloadUrl.addOnSuccessListener { displayUrl ->
                        instructorDetails.instructorName = instructorName
                        instructorDetails.email = email
                        instructorDetails.username = username
                        instructorDetails.dateJoined = dateJoined
                        instructorDetails.instructorPersonalDescription = description
                        instructorDetails.instructorImage = displayUrl.toString()
                        instructorDetailsRef.setValue(instructorDetails).addOnSuccessListener {
                            pref.edit().putString(context.getString(R.string.teacher_details), Gson().toJson(instructorDetails)).apply()
                            completeAction = true
                            Toast.makeText(context, "Profile Updated!!!", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }.addOnFailureListener {
                            dialog.dismiss()
                            Toast.makeText(context, "Error occurred, Upload was Incomplete", Toast.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener {
                        dialog.dismiss()
                        Toast.makeText(context, "Upload failed!!! Retry", Toast.LENGTH_LONG).show()
                        return@addOnFailureListener
                    }
                }
            }

            override fun notConnected() {
                Toast.makeText(context, "Connect to internet and retry!", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
        })
    }

    fun uploadVerification(
        identificationImageStream: ByteArrayInputStream,
        certificateImageStream: ByteArrayInputStream,
        idHash: HashMap<String, String>,
        cerHash: HashMap<String, String>,
        instructorDetails: InstructorDetails,
        viewLifecycleOwner: LifecycleOwner,
        mfV: MessageFragmentViewModel,
    ) {
        var completeAction = false
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        val instructorDetailsRef = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.instructor_details)).child(auth)
        val cn = InternetConnection(context)
        val dialog = Dialogs().circularProgressDialog(activity = activity)
        /*scope.launch {
            delay(10_000)
            if (!completeAction) {
                dialog?.dismiss()
                activity.runOnUiThread{ Toast.makeText(context, "No network connection!!! Retry", Toast.LENGTH_LONG).show() }
            }
        }*/
        cn.setCustomInternetListener(object : InternetConnection.CheckInternetConnection {
            override fun isConnected() {
                val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
                val identificationImageRef = FirebaseStorage.getInstance().reference
                    .child(context.getString(R.string.IDENTIFICATION_IMAGE))
                    .child(auth)
                val certificateImageRef = FirebaseStorage.getInstance().reference
                    .child(context.getString(R.string.CERTIFICATE_IMAGE))
                    .child(auth)

                val identificationImageUploadTask = identificationImageRef.putStream(identificationImageStream)
                val certificateImageUploadTask = certificateImageRef.putStream(certificateImageStream)

                identificationImageUploadTask.continueWith { task ->
                    if (!task.isSuccessful) task.exception?.let { itId ->
                        dialog.dismiss()
                        throw  itId
                    }
                    identificationImageRef.downloadUrl.addOnSuccessListener { identificationUri ->
                        certificateImageUploadTask.continueWith { task ->
                            if (!task.isSuccessful) task.exception?.let {
                                dialog.dismiss()
                                throw  it
                            }
                            certificateImageRef.downloadUrl.addOnSuccessListener { certificateUri ->
                                cerHash["fileLink"] = certificateUri.toString()
                                idHash["fileLink"] = identificationUri.toString()
                                instructorDetails.instructorCertificationHash = cerHash
                                instructorDetails.instructorIdentificationHash = idHash

                                instructorDetailsRef.setValue(instructorDetails).addOnSuccessListener {
                                    pref.edit().putString(context.getString(R.string.teacher_details), Gson().toJson(instructorDetails)).apply()
                                    completeAction = true
                                    successFragment(mfV)
                                    dialog.dismiss()
                                }.addOnFailureListener {
                                    dialog.dismiss()
                                    Toast.makeText(context, "Error occurred, Upload was Incomplete", Toast.LENGTH_LONG).show()
                                }
                            }.addOnFailureListener {
                                dialog.dismiss()
                                Toast.makeText(context, "Upload failed!!! Retry", Toast.LENGTH_LONG).show()
                                return@addOnFailureListener
                            }
                        }
                    }.addOnFailureListener {
                        dialog.dismiss()
                        Toast.makeText(context, "Upload failed!!! Retry", Toast.LENGTH_LONG).show()
                        return@addOnFailureListener
                    }
                }
            }

            override fun notConnected() {
                Toast.makeText(context, "Connect to internet and retry!", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
        })
    }

    private fun successFragment(mfV: MessageFragmentViewModel) {
        val mf = SuccessFragment()
        val dialog = (activity as AppCompatActivity).supportFragmentManager.findFragmentByTag("dialog")
        val ft = activity.supportFragmentManager.beginTransaction()
        if (dialog != null) ft.remove(dialog)

        mfV.setOkFunction((Fragment() to 0) to false)
        mfV.setDisplayText("Verification files uploaded for review.\nYou will be contacted after they are reviewed.")
        mf.show(ft, "dialog")
    }
}