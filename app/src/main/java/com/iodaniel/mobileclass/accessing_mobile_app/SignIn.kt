package com.iodaniel.mobileclass.accessing_mobile_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.SignInBinding
import com.iodaniel.mobileclass.student_package.StudentInitPage
import com.iodaniel.mobileclass.teacher_package.classes.TeacherPage

class SignIn : AppCompatActivity(), View.OnClickListener, HelperListener.LoadingListener {
    private val binding by lazy {
        SignInBinding.inflate(layoutInflater)
    }
    private val auth = FirebaseAuth.getInstance()
    private lateinit var loadingProgressBar: HelperListener.LoadingListener
    private val stTypeRef = FirebaseDatabase.getInstance().reference.child("usersInfo")
    private lateinit var teacherIntent: Intent
    private lateinit var studentIntent: Intent
    private lateinit var pref: SharedPreferences
    private var accountType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        teacherIntent = Intent(this, TeacherPage::class.java)
        studentIntent = Intent(this, StudentInitPage::class.java)
        studentIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        teacherIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        binding.signIn.setOnClickListener(this)
        loadingProgressBar = this
    }

    private fun signIn() {
        val email = binding.signupEmail.text.toString()
        val password = binding.signupPassword.text.toString()
        if (email == "") return
        if (password == "") return
        loadingProgressBar.loadingProgressBar()
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val arrayOfUserInfo: ArrayList<HashMap<*, *>> = arrayListOf()

                stTypeRef.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val snap = snapshot.value as HashMap<*, *>
                        println("ACCOUNT **************************** $snap")
                        arrayOfUserInfo.add(snap)
                        for (user in arrayOfUserInfo) {
                            if (user["UID"] == auth.currentUser!!.uid){
                                accountType = user["accountType"].toString()

                                pref = getSharedPreferences("userType", Context.MODE_PRIVATE)
                                pref.edit().putString("studentTeacher", accountType).apply()
                                val userType = pref.getString("studentTeacher", "")

                                when (accountType) {
                                    "student" -> {
                                        startActivity(studentIntent)
                                        overridePendingTransition(0, 0)
                                    }
                                    "teacher" -> {
                                        startActivity(teacherIntent)
                                        overridePendingTransition(0, 0)
                                    }
                                }
                                loadingProgressBar.notLoadingProgressBar()
                            }
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?,
                    ) {
                        val snap = snapshot.value as Map<*, *>
                        accountType = snap["accountType"].toString()
                        when (accountType) {
                            "student" -> {
                                startActivity(studentIntent)
                                overridePendingTransition(0, 0)
                            }
                            "teacher" -> {
                                startActivity(teacherIntent)
                                overridePendingTransition(0, 0)
                            }
                        }
                        loadingProgressBar.notLoadingProgressBar()
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onCancelled(error: DatabaseError) {}
                })
            }.addOnFailureListener {
                loadingProgressBar.notLoadingProgressBar()
                val snackbar =
                    Snackbar.make(binding.root, it.localizedMessage!!, Snackbar.LENGTH_LONG).show()
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_in -> signIn()
        }
    }

    override fun loadingProgressBar() {
        binding.signInText.visibility = View.INVISIBLE
        binding.signInProgressbar.visibility = View.VISIBLE
    }

    override fun notLoadingProgressBar() {
        binding.signInText.visibility = View.VISIBLE
        binding.signInProgressbar.visibility = View.INVISIBLE
    }
}