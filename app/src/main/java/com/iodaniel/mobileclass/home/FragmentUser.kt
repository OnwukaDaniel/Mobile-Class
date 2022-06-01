package com.iodaniel.mobileclass.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentUserBinding
import com.iodaniel.mobileclass.student_package.FragmentStudentProfile
import com.iodaniel.mobileclass.teacher_package.profile.FragmentInstructorProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentUser : Fragment() {
    private lateinit var binding: FragmentUserBinding
    private val scope = CoroutineScope(Dispatchers.IO)
    private val fragmentInstructorProfile by lazy {
        FragmentInstructorProfile()
    }
    private val fragmentStudentProfile by lazy {
        FragmentStudentProfile()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = requireActivity().getSharedPreferences(getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        runCatching {
            scope.launch {
                when (pref.getString(getString(R.string.studentTeacherPreference), "")) {
                    getString(R.string.student) -> {
                        println("Student *********************************************")
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in, R.anim.slide_in)
                            .replace(R.id.fragment_user_root, fragmentStudentProfile).commit()
                    }
                    getString(R.string.teacher) -> {
                        println("Teacher *********************************************")
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in, R.anim.slide_in)
                            .replace(R.id.landing_frame, fragmentInstructorProfile).commit()
                    }
                }
            }
        }
    }
}