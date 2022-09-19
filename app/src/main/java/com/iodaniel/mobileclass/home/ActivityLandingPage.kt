package com.iodaniel.mobileclass.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.databinding.ActivityLandingPageBinding
import com.iodaniel.mobileclass.databinding.ActivityLandingPageTeacherBinding
import com.iodaniel.mobileclass.student_package.FragmentStudentProfile
import com.iodaniel.mobileclass.teacher_package.profile.FragmentInstructorProfile
import com.iodaniel.mobileclass.util.Dialogs
import kotlinx.coroutines.*

class ActivityLandingPage : AppCompatActivity() {
    private val binding by lazy { ActivityLandingPageBinding.inflate(layoutInflater) }
    private val bindingTeacher by lazy { ActivityLandingPageTeacherBinding.inflate(layoutInflater) }
    private lateinit var cn: InternetConnection
    private val market = FragmentHome()
    private val learning = FragmentMyLearning()
    private val userSetting = FragmentUserSettings()
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var pref: SharedPreferences
    private var dialogs = Dialogs()
    private var userType = ""
    private val fragmentInstructorProfile = FragmentInstructorProfile()
    private val fragmentStudentProfile = FragmentStudentProfile()
    private var userTypeFragment: Fragment? = null
    private var isTeacher = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cn = InternetConnection(applicationContext)
        pref = getSharedPreferences(getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        userType = pref.getString(getString(R.string.studentTeacherPreference), "")!!
        when (userType) {
            getString(R.string.student) -> {
                userTypeFragment = fragmentStudentProfile
                setContentView(binding.root)
                studentMethods()
            }
            getString(R.string.teacher) -> {
                userTypeFragment = fragmentInstructorProfile
                setContentView(bindingTeacher.root)
                teacherMethods();isTeacher = true
            }
        }
        cn.setCustomInternetListener(object : InternetConnection.CheckInternetConnection {
            override fun isConnected() {
                supportFragmentManager.beginTransaction().replace(R.id.landing_frame, market).commit()
            }

            override fun notConnected() {
                if(isTeacher) supportFragmentManager.beginTransaction().replace(R.id.landing_frame, fragmentInstructorProfile).commit()
                else supportFragmentManager.beginTransaction().replace(R.id.landing_frame, learning).commit()
            }
        })
    }

    private fun teacherMethods(){
        bindingTeacher.landingBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_my_classes -> {
                    supportFragmentManager.beginTransaction().replace(R.id.landing_frame, market).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.menu_my_profile -> {
                    runBlocking {
                        scope.launch{
                            delay(500)
                            runOnUiThread{
                                val dialog = dialogs.circularProgressDialog(text = "Please wait", activity = this@ActivityLandingPage)
                                supportFragmentManager.beginTransaction().replace(R.id.landing_frame, userTypeFragment!!).commit()
                                dialog.dismiss()
                            }
                        }
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.menu_my_settings -> {
                    supportFragmentManager.beginTransaction().replace(R.id.landing_frame, userSetting).commit()
                    return@setOnItemSelectedListener true
                }
                else -> return@setOnItemSelectedListener false
            }
        }
        bindingTeacher.landingBottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.menu_my_classes -> Unit
                R.id.menu_my_profile -> Unit
                R.id.menu_my_settings -> Unit
            }
        }
    }

    private fun studentMethods(){
        binding.landingBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_my_classes -> {
                    supportFragmentManager.beginTransaction().replace(R.id.landing_frame, market).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.menu_my_learning -> {
                    supportFragmentManager.beginTransaction().replace(R.id.landing_frame, learning).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.menu_my_profile -> {
                    runBlocking {
                        scope.launch{
                            delay(500)
                            runOnUiThread{
                                val dialog = dialogs.circularProgressDialog(text = "Please wait", activity = this@ActivityLandingPage)
                                supportFragmentManager.beginTransaction().replace(R.id.landing_frame, userTypeFragment!!).commit()
                                dialog.dismiss()
                            }
                        }
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.menu_my_settings -> {
                    supportFragmentManager.beginTransaction().replace(R.id.landing_frame, userSetting).commit()
                    return@setOnItemSelectedListener true
                }
                else -> return@setOnItemSelectedListener false
            }
        }
        binding.landingBottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.menu_my_classes -> Unit
                R.id.menu_my_learning -> Unit
                R.id.menu_my_profile -> Unit
                R.id.menu_my_settings -> Unit
            }
        }
    }
}