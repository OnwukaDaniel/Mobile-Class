package com.iodaniel.mobileclass.accessing_mobile_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentTeacherStudentSignUpBinding
import com.iodaniel.mobileclass.databinding.SignInOrSignUpBinding

class SignInOrSignUp : AppCompatActivity() {

    private val binding by lazy { SignInOrSignUpBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val sfm = supportFragmentManager
        val signIn = FragmentSignIn()
        val teacherStudentSignUp = FragmentTeacherStudentSignUp()

        binding.welcomeSignUp.setOnClickListener {
            sfm.beginTransaction().addToBackStack("signup").replace(R.id.sign_up_sign_in, teacherStudentSignUp).commit()
        }
        binding.welcomeSignIn.setOnClickListener {
            sfm.beginTransaction().addToBackStack("signup").replace(R.id.sign_up_sign_in, signIn).commit()
        }
    }
}

class FragmentTeacherStudentSignUp : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentTeacherStudentSignUpBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTeacherStudentSignUpBinding.inflate(inflater, container, false)
        binding.signupStudent.setOnClickListener(this)
        binding.signupTeacher.setOnClickListener(this)
        return binding.root
    }

    override fun onClick(v: View?) {
        val sfm = requireActivity().supportFragmentManager
        val fragment = FragmentSignUp()
        val bundle = Bundle()
        when (v?.id) {
            R.id.signup_student -> {
                bundle.putString("student_teacher", getString(R.string.student))
                fragment.arguments = bundle
                sfm.beginTransaction().addToBackStack("student").replace(R.id.teacher_student_root, fragment).commit()
            }
            R.id.signup_teacher -> {
                bundle.putString("student_teacher", getString(R.string.teacher))
                fragment.arguments = bundle
                sfm.beginTransaction().addToBackStack("student").replace(R.id.teacher_student_root, fragment).commit()
            }
        }
    }
}