package com.iodaniel.mobileclass.accessing_mobile_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.SignUpBinding
import com.iodaniel.mobileclass.shared_classes.ActivityMyClasses
import com.iodaniel.mobileclass.student_package.StudentInitPage

class SignUp : AppCompatActivity(), View.OnClickListener, HelperListener.LoadingListener {

    private val binding by lazy {
        SignUpBinding.inflate(layoutInflater)
    }
    private lateinit var loadingListener: HelperListener.LoadingListener

    private val auth = FirebaseAuth.getInstance()
    private val stTypeRef = FirebaseDatabase.getInstance().reference.child("usersInfo")
    private lateinit var pref: SharedPreferences
    private var accountType = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadingListener = this
        binding.accountType.setOnClickListener(this)
        binding.signUp.setOnClickListener(this)
    }

    private fun selectAccountType() {
        val popupMenu = PopupMenu(applicationContext, binding.accountType)
        val menuInflater = popupMenu.menuInflater
        menuInflater.inflate(R.menu.account_type, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when (it?.itemId) {
                R.id.student -> {
                    binding.accountType.text = "Create your student account"
                    accountType = "student"
                    return@setOnMenuItemClickListener true
                }
                R.id.teacher -> {
                    binding.accountType.text = "Create your teacher account"
                    accountType = "teacher"
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
    }

    private fun hideKeyboard(context: Context, windowToken: IBinder) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun signUp() {
        val email = binding.signupEmail.text.toString()
        val password = binding.signupPassword.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        if (email == "") return
        if (password == "") return
        if (password.length < 6) {
            var snackbar = Snackbar.make(
                binding.root,
                "Password must be greater than 6 characters",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        if (confirmPassword == "") return
        if (password != confirmPassword) {
            var snackbar = Snackbar.make(
                binding.root,
                "Passwords don't match",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        loadingListener.loadingProgressBar()
        hideKeyboard(applicationContext, binding.root.windowToken)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                pref = getSharedPreferences("userType", Context.MODE_PRIVATE)
                pref.edit().putString("studentTeacher", accountType).apply()
                val userType = pref.getString("studentTeacher", "")
                println(" *************************************** $userType")

                val pair: HashMap<String, String> =
                    hashMapOf("accountType" to accountType, "UID" to auth.currentUser!!.uid)

                stTypeRef.push().setValue(pair).addOnCompleteListener {
                    val teacherIntent = Intent(this, ActivityMyClasses::class.java)
                    teacherIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    val studentIntent = Intent(this, StudentInitPage::class.java)
                    studentIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    when (binding.accountType.text) {
                        "Create your student account" -> {
                            startActivity(studentIntent)
                            overridePendingTransition(0, 0)
                        }
                        "Create your teacher account" -> {
                            startActivity(teacherIntent)
                            overridePendingTransition(0, 0)
                        }
                    }
                    loadingListener.notLoadingProgressBar()
                }.addOnFailureListener {
                    loadingListener.notLoadingProgressBar()
                    val snackbar =
                        Snackbar.make(binding.root, it.localizedMessage, Snackbar.LENGTH_LONG)
                            .show()
                }
            }.addOnFailureListener {
                loadingListener.notLoadingProgressBar()
                val snackbar =
                    Snackbar.make(binding.root, it.localizedMessage, Snackbar.LENGTH_LONG).show()
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.account_type -> selectAccountType()
            R.id.sign_up -> signUp()
        }
    }

    override fun loadingProgressBar() {
        binding.signUpText.visibility = View.INVISIBLE
        binding.signUpProgressbar.visibility = View.VISIBLE
    }

    override fun notLoadingProgressBar() {
        binding.signUpText.visibility = View.VISIBLE
        binding.signUpProgressbar.visibility = View.INVISIBLE
    }
}