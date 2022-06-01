package com.iodaniel.mobileclass.repository

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.data_class.StudentDetails
import com.iodaniel.mobileclass.liveDataClasses.InstructorProfileLiveData
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.util.ChildEventTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream


class ProfileRepo(val context: Context, val activity: Activity) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun decoupleTeacherSharedPreferenceProfile(): InstructorDetails {
        val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        val json = pref.getString(context.getString(R.string.teacher_details), "")
        return Gson().fromJson(json, InstructorDetails::class.java)
    }

    fun decoupleStudentSharedPreferenceProfile(): StudentDetails {
        val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        val json = pref.getString(context.getString(R.string.student_details), "")
        return Gson().fromJson(json, StudentDetails::class.java)
    }

    fun setSharedPreferenceDataToUi() {
        val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        val json = pref.getString(context.getString(R.string.teacher_details), "")
        val teacherDetails = Gson().fromJson(json, InstructorDetails::class.java)
    }

    fun getAndSetInstructorProfileForProfileFragment(
        viewLifecycleOwner: LifecycleOwner,
        instructorProfileDisplayImage: ImageView,
        instructorProfileNotification: ImageView,
        instructorProfileStudentNumber: TextView,
        instructorProfileSchemeNumber: TextView,
        instructorProfileCourseNumber: TextView,
        instructorProfileTopCoursesRoot: LinearLayout,
        instructorProfileTopCourseRv: RecyclerView,
        instructorProfileTopCoursesSeeMore: TextView,
    ) {
        var instructorDetails: InstructorDetails? = null
        var fetchedData = false
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        val instructorDetailsRef = FirebaseDatabase.getInstance().reference
            .child(context.getString(R.string.instructor_details)) // CONFIRM
            .child(auth)

        scope.launch {
            delay(10_000)
            if (!fetchedData) activity.runOnUiThread {
                //Toast.makeText(context, "No network connection!!! Retry", Toast.LENGTH_LONG).show()
            }
        }

        activity.runOnUiThread {
            InstructorProfileLiveData(instructorDetailsRef).observe(viewLifecycleOwner) {
                if (it.second == ChildEventTemplate.onDataChange) {
                    instructorDetails = it.first
                    fetchedData = true
                    Glide.with(context).load(instructorDetails!!.instructorImage).centerCrop().into(instructorProfileDisplayImage)
                    instructorProfileStudentNumber.text = instructorDetails!!.studentsEnrolled
                    instructorProfileCourseNumber.text = instructorDetails!!.coursesCreated
                    instructorProfileSchemeNumber.text = instructorDetails!!.schemesCreated
                }
            }
        }
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

    fun upload(
        instructorName: String,
        email: String,
        username: String,
        dateJoined: String,
        description: String,
        displayImageStream: ByteArrayInputStream,
        identificationImageStream: ByteArrayInputStream,
        certificateImageStream: ByteArrayInputStream,
        instructorDetails: InstructorDetails,
    ) {
        var completeAction = false
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        val instructorDetailsRef = FirebaseDatabase.getInstance().reference
            .child(context.getString(R.string.instructor_details)) // CONFIRM
            .child(auth)
        val cn = InternetConnection(context)
        val dialog = Util.progressDialog("Please wait...", context, activity)
        dialog?.show()

        scope.launch {
            delay(10_000)
            if (!completeAction) {
                dialog?.dismiss()
                activity.runOnUiThread{ Toast.makeText(context, "No network connection!!! Retry", Toast.LENGTH_LONG).show() }
            }
        }

        cn.setCustomInternetListener(object : InternetConnection.CheckInternetConnection {
            override fun isConnected() {
                val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
                val displayImageRef = FirebaseStorage.getInstance().reference
                    .child(context.getString(R.string.INSTRUCTOR_DISPLAY_IMAGE))
                    .child(auth)
                val identificationImageRef = FirebaseStorage.getInstance().reference
                    .child(context.getString(R.string.IDENTIFICATION_IMAGE))
                    .child(auth)
                val certificateImageRef = FirebaseStorage.getInstance().reference
                    .child(context.getString(R.string.CERTIFICATE_IMAGE))
                    .child(auth)

                val displayImageUploadTask = displayImageRef.putStream(displayImageStream)
                val identificationImageUploadTask = identificationImageRef.putStream(identificationImageStream)
                val certificateImageUploadTask = certificateImageRef.putStream(certificateImageStream)

                displayImageUploadTask.continueWith { task ->
                    if (!task.isSuccessful) task.exception?.let {
                        dialog?.dismiss()
                        throw  it
                    }
                    displayImageRef.downloadUrl.addOnSuccessListener { displayUri ->
                        identificationImageUploadTask.continueWith { task ->
                            if (!task.isSuccessful) task.exception?.let { itId ->
                                dialog?.dismiss()
                                throw  itId
                            }
                            identificationImageRef.downloadUrl.addOnSuccessListener { identificationUri ->
                                certificateImageUploadTask.continueWith { task ->
                                    if (!task.isSuccessful) task.exception?.let {
                                        dialog?.dismiss()
                                        throw  it
                                    }
                                    certificateImageRef.downloadUrl.addOnSuccessListener { certificateUri ->
                                        instructorDetails.instructorName = instructorName
                                        instructorDetails.email = email
                                        instructorDetails.username = username
                                        instructorDetails.dateJoined = dateJoined
                                        instructorDetails.instructorPersonalDescription = description
                                        instructorDetails.instructorImage = displayUri.toString()
                                        instructorDetails.instructorCertificationLink = identificationUri.toString()
                                        instructorDetails.instructorIdentification = certificateUri.toString()
                                        instructorDetailsRef.setValue(instructorDetails).addOnSuccessListener {
                                            pref.edit().putString(context.getString(R.string.teacher_details), Gson().toJson(instructorDetails)).apply()
                                            completeAction = true
                                            Toast.makeText(context, "Profile Updated!!!", Toast.LENGTH_LONG).show()
                                            dialog?.dismiss()
                                        }.addOnFailureListener {
                                            dialog?.dismiss()
                                            Toast.makeText(context, "Error occurred, Upload was Incomplete", Toast.LENGTH_LONG).show()
                                        }
                                    }.addOnFailureListener {
                                        dialog?.dismiss()
                                        Toast.makeText(context, "Upload failed!!! Retry", Toast.LENGTH_LONG).show()
                                        return@addOnFailureListener
                                    }
                                }
                            }.addOnFailureListener {
                                dialog?.dismiss()
                                Toast.makeText(context, "Upload failed!!! Retry", Toast.LENGTH_LONG).show()
                                return@addOnFailureListener
                            }
                        }
                    }.addOnFailureListener {
                        dialog?.dismiss()
                        Toast.makeText(context, "Upload failed!!! Retry", Toast.LENGTH_LONG).show()
                        return@addOnFailureListener
                    }
                }
            }

            override fun notConnected() {
                Toast.makeText(context, "Connect to internet and retry!", Toast.LENGTH_LONG).show()
                dialog?.dismiss()
            }
        })
    }
}