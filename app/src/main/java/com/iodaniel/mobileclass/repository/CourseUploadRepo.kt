package com.iodaniel.mobileclass.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.liveDataClasses.InstructorProfileLiveData
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.ProgressBarController
import com.iodaniel.mobileclass.teacher_package.course.CourseUpload
import com.iodaniel.mobileclass.teacher_package.profile.ManageProfileCourseType
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.util.ImageCompressor.compressImage
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.util.*
import kotlin.math.roundToInt

class CourseUploadRepo(val activity: Activity, val context: Context, val view: View, val lifecycleOwner: LifecycleOwner) {
    private val customImages = arrayListOf(R.drawable.study1, R.drawable.study2, R.drawable.study4)
    private val firebaseUser = FirebaseAuth.getInstance().currentUser
    val scope = CoroutineScope(Dispatchers.IO)
    private var auth = ""

    fun upload(
        courseName: String,
        shortDescription: String,
        organisationName: String,
        level: String,
        detailedDescription: String,
        price: String,
        courseImageInput: ByteArrayInputStream?,
        progressBarController: ProgressBarController,
    ) {
        var foundData = false
        progressBarController.showProgressBar()
        auth = firebaseUser?.uid.toString()
        val profileRepo = ProfileRepo(context, activity)
        val split = UUID.randomUUID().toString().split("-")
        val courseCode = split[1] + split[2].substring(0, 1) + split[4].substring(6, 8)
        val instructorRef = FirebaseDatabase.getInstance().reference
            .child(context.getString(R.string.instructor_details))
            .child(auth)

        //val singleCourseCardLiveData = SingleCourseCardLiveData()
        val instructorProfileLiveData = InstructorProfileLiveData(instructorRef)
        val courseReference = FirebaseDatabase.getInstance()
            .reference
            .child(context.getString(R.string.course_path))
            .child(courseCode)
        val allCourseCodesRef = FirebaseDatabase.getInstance().reference
            .child(context.getString(R.string.course_codes))
            .push()
        val datetime = Calendar.getInstance().time.time.toString()

        var courseImage = courseImageInput
        if (courseImage == null) {
            val rand = (Math.random() * 2).roundToInt()
            val byteIS = compressImage(context, BitmapFactory.decodeResource(activity.resources, customImages[rand]))
            courseImage = byteIS.first
        }
        val storageRef = FirebaseStorage.getInstance().reference.child(context.getString(R.string.course_path)).child(auth).child(courseName + datetime)
        val uploadTask = storageRef.putStream(courseImage)

        uploadTask.continueWith { task ->
            if (!task.isSuccessful) task.exception?.let {
                showErrorOrTimeOut(it.localizedMessage!!.toString(), progressBarController)
                throw it
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageRef.downloadUrl.addOnCompleteListener {
                }
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val detailsX = hashMapOf(context.getString(R.string.course_codes) to courseCode, context.getString(R.string.uid) to auth)
                    instructorProfileLiveData.observe(lifecycleOwner) { instructorPair ->
                        foundData = true
                        if (instructorPair.second == ChildEventTemplate.onDataChange) {
                            val instructorDetails = instructorPair.first

                            val courseCardData = CourseCardData(
                                courseName = courseName,
                                courseImage = downloadUri.toString(),
                                organisationName = organisationName,
                                rating = "0",
                                shortDescription = shortDescription,
                                price = price,
                                level = level,
                                studentsEnrolled = "0",
                                description = detailedDescription,
                                dateCreated = datetime,
                                courseCode = courseCode,
                                courseCodePushId = allCourseCodesRef.key!!,
                                others = "",
                                materialLink = "",
                                instructorName = instructorDetails.instructorName,
                                instructorInChargeUID = instructorDetails.uid,
                            )
                            allCourseCodesRef.setValue(detailsX).addOnSuccessListener {
                                courseReference.setValue(courseCardData).addOnSuccessListener {
                                    instructorRef.setValue(instructorDetails).addOnSuccessListener {
                                        showErrorOrTimeOut("", progressBarController)
                                        val intent = Intent(context, CourseUpload::class.java)
                                        Toast.makeText(context, "Course Saved.", Toast.LENGTH_LONG).show()
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        activity.startActivity(intent)
                                        activity.overridePendingTransition(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                                    }
                                }.addOnFailureListener {
                                    showErrorOrTimeOut("Could not create course. Please retry", progressBarController)
                                    return@addOnFailureListener
                                }
                            }.addOnFailureListener {
                                showErrorOrTimeOut("Could not create course. Please retry", progressBarController)
                                return@addOnFailureListener
                            }
                        }
                    }
                    runBlocking {
                        scope.launch {
                            delay(25_000)
                            if (!foundData) {
                                // TODO STOP LOADING
                                showErrorOrTimeOut("Upload Failed. No active internet. Please Try again", progressBarController)
                            }
                        }
                    }
                }
            }
        }.addOnFailureListener {
            showErrorOrTimeOut("Upload Failed. Please Try again", progressBarController)
            return@addOnFailureListener
        }
    }

    fun uploadCompletelySavedCourse(activity: Activity, courseCode: String, progressBarController: ProgressBarController) {
        auth = firebaseUser?.uid.toString()
        val courseReference = FirebaseDatabase.getInstance()
            .reference
            .child(context.getString(R.string.course_path))
            .child(courseCode)
            .child("manageProfileCourseType")
        courseReference.setValue(ManageProfileCourseType.COMPLETE).addOnSuccessListener {
            scope.launch {
                delay(2_000)
                activity.runOnUiThread { showErrorOrTimeOut("Course Uploaded", progressBarController) }
            }
        }.addOnFailureListener {
            showErrorOrTimeOut("Could not upload course. Please retry", progressBarController)
            return@addOnFailureListener
        }
    }

    private fun showErrorOrTimeOut(text: String, progressBarController: ProgressBarController) {
        if (text != "") Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
        progressBarController.hideProgressBar()
    }
}