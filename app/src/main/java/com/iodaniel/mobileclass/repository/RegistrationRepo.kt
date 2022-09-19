package com.iodaniel.mobileclass.repository

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.HelperListener
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.data_class.StudentDetails
import com.iodaniel.mobileclass.home.ActivityLandingPage
import com.iodaniel.mobileclass.liveDataClasses.InstructorProfileLiveData
import com.iodaniel.mobileclass.liveDataClasses.StudentProfileLiveData
import com.iodaniel.mobileclass.liveDataClasses.UserProfileLiveData
import com.iodaniel.mobileclass.util.Keyboard.hideKeyboard
import java.util.*

class RegistrationRepo(
    val context: Context, val activity: Activity, val view: View, private var accountType: String,
    private val loadingListener: HelperListener
    .LoadingListener,
    private val owner: LifecycleOwner,
) {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var pref: SharedPreferences
    private lateinit var userIntent: Intent

    fun signIn(email: String, password: String) {
        if (email == "") return
        if (password == "") return
        userIntent = Intent(context, ActivityLandingPage::class.java)
        userIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        loadingListener.loadingProgressBar()
        activity.hideKeyboard()
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
            val ref = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.userInfo)).child(authResult.user!!.uid)
            val userProfileLiveData = UserProfileLiveData(ref)
            userProfileLiveData.observe(owner) { pair ->
                val accountType = pair.first
                pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
                pref.edit().putString(context.getString(R.string.studentTeacherPreference), accountType).apply()

                when (accountType.lowercase(Locale.getDefault())) {
                    context.getString(R.string.student) -> {
                        editStudentDetails(email = email, age = "", fullName = "", username = "", uid = authResult.user!!.uid)
                        activity.startActivity(userIntent)
                    }
                    context.getString(R.string.teacher) -> {
                        editInstructorDetailsOnSignIn(authResult.user!!.uid)
                        activity.startActivity(userIntent)
                    }
                }
                activity.overridePendingTransition(0, 0)
                loadingListener.notLoadingProgressBar()
            }
        }.addOnFailureListener {
            loadingListener.notLoadingProgressBar()
            Snackbar.make(view, it.localizedMessage!!, Snackbar.LENGTH_LONG).show()
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String, age: String, fullName: String, username: String) {
        val snackBar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)
        if (email == "") return
        if (password == "") return
        if (password.length < 6) {
            snackBar.setText("Password must be greater than 6 characters").show()
            return
        }
        if (password != confirmPassword) {
            snackBar.setText("Passwords don't match").show()
            return
        }
        if (fullName == "") {
            snackBar.setText("Empty fields: FullName").show()
            return
        }
        if (username == "") {
            snackBar.setText("Empty fields: Username").show()
            return
        }
        if (age == "") {
            snackBar.setText("Pick your age range").show()
            return
        }

        loadingListener.loadingProgressBar()
        (activity as AppCompatActivity).hideKeyboard()
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
            val typeRef = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.userInfo)).child(it.user!!.uid)
            val instructorDetailsRef = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.instructor_details)).child(auth.uid!!)
            val studentDetailsRef = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.student_details)).child(auth.uid!!)
            val instructorDetails = InstructorDetails(
                instructorName = fullName,
                email = email,
                age = age,
                uid = auth.currentUser!!.uid,
                dateJoined = Calendar.getInstance().timeInMillis.toString(),
                username = username
            )
            val studentDetails = StudentDetails(
                fullName = fullName,
                email = email,
                age = age,
                uid = auth.currentUser!!.uid,
                accountType = accountType,
                dateJoined = Calendar.getInstance().timeInMillis.toString(),
                username = username
            )
            val ref: DatabaseReference = if (accountType == context.getString(R.string.teacher)) instructorDetailsRef else studentDetailsRef

            when (accountType) {
                context.getString(R.string.teacher) -> {
                    typeRef.setValue(accountType).addOnSuccessListener {
                        ref.setValue(instructorDetails).addOnSuccessListener {
                            transition(email, age, fullName, username, auth.uid!!)
                        }.addOnFailureListener { exp ->
                            loadingListener.notLoadingProgressBar()
                            Snackbar.make(view, exp.localizedMessage!!, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                context.getString(R.string.student) -> {
                    typeRef.setValue(accountType).addOnSuccessListener {
                        ref.setValue(studentDetails).addOnSuccessListener {
                            transition(email, age, fullName, username, auth.uid!!)
                        }.addOnFailureListener { exp ->
                            loadingListener.notLoadingProgressBar()
                            Snackbar.make(view, exp.localizedMessage!!, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }.addOnFailureListener {
            loadingListener.notLoadingProgressBar()
            Snackbar.make(view, it.localizedMessage!!, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun transition(email: String, age: String, fullName: String, username: String, uid: String){
        pref.edit().putString(context.getString(R.string.studentTeacherPreference), accountType).apply()
        val userIntent = Intent(context, ActivityLandingPage::class.java)
        userIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        if (accountType == context.getString(R.string.teacher)) {
            editInstructorDetails(email = email, age = age, fullName = fullName, username = username, uid = uid)
        } else {
            editStudentDetails(email = email, age = age, fullName = fullName, username = username, uid = uid)
        }
        activity.startActivity(userIntent)
        activity.overridePendingTransition(0, 0)
        loadingListener.notLoadingProgressBar()
    }

    private fun editInstructorDetailsOnSignIn(uid: String) {
        val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        val ref = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.instructor_details)).child(uid)
        val instructorProfileLiveData = InstructorProfileLiveData(ref)
        instructorProfileLiveData.observe(owner) {
            val instructorDetails = it.first
            val jsonEdited = Gson().toJson(instructorDetails)
            pref.edit().putString(context.getString(R.string.teacher_details), jsonEdited).apply()
        }
    }

    private fun editInstructorDetails(email: String, age: String, fullName: String, username: String, uid: String) {
        val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        val json = pref.getString(context.getString(R.string.teacher_details), "")
        var instructorDetails = InstructorDetails()
        if (json != "") instructorDetails = Gson().fromJson(json, InstructorDetails::class.java)
        instructorDetails.age = age
        instructorDetails.dateJoined = Calendar.getInstance().timeInMillis.toString()
        instructorDetails.email = email
        instructorDetails.instructorName = fullName
        instructorDetails.username = username
        instructorDetails.uid = uid
        val jsonEdited = Gson().toJson(instructorDetails)
        pref.edit().putString(context.getString(R.string.teacher_details), jsonEdited).apply()
    }

    private fun editStudentDetailsOnSignIn(uid: String) {
        val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        val ref = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.student_details)).child(uid)
        val studentProfileLiveData = StudentProfileLiveData(ref)
        studentProfileLiveData.observe(owner) {
            val studentDetails = it.first
            val jsonEdited = Gson().toJson(studentDetails)
            pref.edit().putString(context.getString(R.string.student_details), jsonEdited).apply()
        }
    }

    private fun editStudentDetails(email: String, age: String, fullName: String, username: String, uid: String) {
        val pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        val json = pref.getString(context.getString(R.string.student_details), "")
        var studentDetails = StudentDetails()
        if (json != "") studentDetails = Gson().fromJson(json, StudentDetails::class.java)
        studentDetails.age = age
        studentDetails.dateJoined = Calendar.getInstance().timeInMillis.toString()
        studentDetails.email = email
        studentDetails.fullName = fullName
        studentDetails.username = username
        studentDetails.uid = uid
        val jsonEdited = Gson().toJson(studentDetails)
        pref.edit().putString(context.getString(R.string.student_details), jsonEdited).apply()
    }

    fun agePicker(): Pair<View, AlertDialog> {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_age, null, false)
        val alertDialog = AlertDialog.Builder(context)
            .setView(view)
            .create()
        alertDialog.show()
        return view to alertDialog
    }

    fun completeProfile(fullName: String, username: String, age: String) {
        val email = auth.currentUser!!.email!!
        pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        pref.edit().putString(context.getString(R.string.studentTeacherPreference), accountType).apply()

        when (accountType) {
            context.getString(R.string.teacher) -> {
                val instructorDetailsRef = FirebaseDatabase.getInstance().reference
                    .child(context.getString(R.string.instructor_details))
                    .child(auth.uid!!)
                val instructorDetails = InstructorDetails(
                    instructorName = fullName,
                    email = email,
                    age = age,
                    uid = auth.currentUser!!.uid,
                    dateJoined = Calendar.getInstance().timeInMillis.toString(),
                    username = username
                )
                instructorDetailsRef.setValue(instructorDetails).addOnSuccessListener {
                    val teacherIntent = Intent(context, ActivityLandingPage::class.java)
                    teacherIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    editInstructorDetails(email = email, age = age, fullName = fullName, username = username, uid = auth.uid!!)
                    activity.startActivity(teacherIntent)
                    activity.overridePendingTransition(0, 0)
                    loadingListener.notLoadingProgressBar()
                }.addOnFailureListener {
                    loadingListener.notLoadingProgressBar()
                    Snackbar.make(view, it.localizedMessage!!, Snackbar.LENGTH_LONG).show()
                }
            }
            context.getString(R.string.student) -> {
                val studentDetailsRef = FirebaseDatabase.getInstance().reference
                    .child(context.getString(R.string.student_details))
                    .child(auth.uid!!)
                val studentDetails = StudentDetails(
                    fullName = fullName,
                    email = email,
                    age = age,
                    uid = auth.currentUser!!.uid,
                    accountType = accountType,
                    dateJoined = Calendar.getInstance().timeInMillis.toString(),
                    username = username
                )
                studentDetailsRef.setValue(studentDetails).addOnSuccessListener {
                    val studentIntent = Intent(context, ActivityLandingPage::class.java)
                    studentIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    editStudentDetails(email = email, age = age, fullName = fullName, username = username, uid = auth.uid!!)
                    activity.startActivity(studentIntent)
                    activity.overridePendingTransition(0, 0)
                    loadingListener.notLoadingProgressBar()
                }.addOnFailureListener {
                    loadingListener.notLoadingProgressBar()
                    Snackbar.make(view, it.localizedMessage!!, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}