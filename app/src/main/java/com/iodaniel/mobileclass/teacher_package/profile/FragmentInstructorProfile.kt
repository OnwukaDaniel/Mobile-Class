package com.iodaniel.mobileclass.teacher_package.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.databinding.FragmentInstructorProfileBinding
import com.iodaniel.mobileclass.repository.ProfileRepo
import com.iodaniel.mobileclass.teacher_package.course.ActivityManageUpload
import com.iodaniel.mobileclass.teacher_package.course.CourseUpload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentInstructorProfile : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentInstructorProfileBinding
    private lateinit var profileRepo: ProfileRepo
    private lateinit var instructorDetails: InstructorDetails
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInstructorProfileBinding.inflate(inflater, container, false)
        binding.instructorProfileCreateCourse.setOnClickListener(this)
        binding.instructorProfileCreateScheme.setOnClickListener(this)
        binding.instructorProfileEdit.setOnClickListener(this)
        binding.instructorProfileCompletedCourses.setOnClickListener(this)
        binding.instructorProfileSavedCourses.setOnClickListener(this)
        binding.instructorProfileAuditedCourses.setOnClickListener(this)
        profileRepo = ProfileRepo(requireContext(), requireActivity())
        instructorDetails = profileRepo.decoupleTeacherSharedPreferenceProfile()
        scope.launch { checkIncompleteProfile() }
        scope.launch {
            profileRepo.getAndSetInstructorProfileForProfileFragment(
                viewLifecycleOwner,
                binding.instructorProfileDisplayImage,
                binding.instructorProfileNotification,
                binding.instructorProfileStudentNumber,
                binding.instructorProfileSchemeNumber,
                binding.instructorProfileCourseNumber,
                binding.instructorProfileTopCoursesRoot,
                binding.instructorProfileTopCourseRv,
                binding.instructorProfileTopCoursesSeeMore,
            )
        }
        return binding.root
    }

    private fun checkIncompleteProfile() {
        val incompleteProfile = instructorDetails.instructorImage == "" || instructorDetails.instructorPersonalDescription == ""
                || instructorDetails.instructorCertificationLink == "" || instructorDetails.instructorIdentification == ""
                || instructorDetails.instructorImage == ""
        if (incompleteProfile) {
            startActivity(Intent(requireContext(), ActivityTeacherProfile::class.java))
            requireActivity().overridePendingTransition(0, 0)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.instructor_profile_edit -> {
                requireActivity().startActivity(Intent(context, ActivityEditProfile::class.java))
                requireActivity().overridePendingTransition(0, 0)
            }
            R.id.instructor_profile_create_course -> {
                requireActivity().startActivity(Intent(context, CourseUpload::class.java))
                requireActivity().overridePendingTransition(0, 0)
            }
            R.id.instructor_profile_create_scheme -> {

            }
            R.id.instructor_profile_completed_courses -> {
                val intent = Intent(requireContext(), ActivityManageUpload::class.java)
                intent.putExtra(getString(R.string.manage_course_data_intent), ManageProfileCourseType.COMPLETE)
                startActivity(intent)
            }
            R.id.instructor_profile_saved_courses -> {
                val intent = Intent(requireContext(), ActivityManageUpload::class.java)
                intent.putExtra(getString(R.string.manage_course_data_intent), ManageProfileCourseType.SAVED)
                startActivity(intent)
            }
            R.id.instructor_profile_audited_courses -> {
                val intent = Intent(requireContext(), ActivityManageUpload::class.java)
                intent.putExtra(getString(R.string.manage_course_data_intent), ManageProfileCourseType.AUDITED)
                startActivity(intent)
            }
        }
    }
}

object ManageProfileCourseType {
    const val COMPLETE = 0
    const val SAVED = 1
    const val AUDITED = 2
}