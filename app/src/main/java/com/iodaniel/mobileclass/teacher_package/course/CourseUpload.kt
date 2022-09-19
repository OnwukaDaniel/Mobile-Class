package com.iodaniel.mobileclass.teacher_package.course

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.R.id
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.databinding.CourseUploadBinding
import com.iodaniel.mobileclass.databinding.EducationLevelBinding
import com.iodaniel.mobileclass.repository.CourseUploadRepo
import com.iodaniel.mobileclass.util.Keyboard.hideKeyboard
import com.iodaniel.mobileclass.viewModel.CourseUploadProgressViewModel
import com.iodaniel.mobileclass.viewModel.CourseUploadState
import com.iodaniel.mobileclass.viewModel.CourseUploadViewModel
import com.iodaniel.mobileclass.viewModel.EducationViewModel

class CourseUpload : AppCompatActivity(), View.OnClickListener{

    private val binding by lazy { CourseUploadBinding.inflate(layoutInflater) }
    private lateinit var errorSnackBar: Snackbar
    private lateinit var cn: InternetConnection
    private val educationViewModel: EducationViewModel by viewModels()
    private val courseUploadProgressViewModel: CourseUploadProgressViewModel by viewModels()
    private lateinit var courseUploadRepo: CourseUploadRepo
    private val courseUploadViewModel = CourseUploadViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val txt = "Upload Failed. Please Try again"
        binding.classUploadEducationLevel.setOnClickListener(this)
        binding.classUploadProceedButton.setOnClickListener(this)
        cn = InternetConnection(applicationContext)
        errorSnackBar = Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG)
        courseUploadRepo = CourseUploadRepo(this, applicationContext, binding.root, this)
        courseUploadViewModel.level.observe(this) {
            binding.classUploadEducationLevelText.text = it!!
        }
        progress()
        educationViewModel.education.observe(this){
            binding.classUploadEducationLevelText.text = it
        }
    }

    private fun progress(){
        courseUploadProgressViewModel.setState(CourseUploadState.STEP_ONE)
        courseUploadProgressViewModel.state.observe(this){state->
            when(state){
                CourseUploadState.STEP_ONE->{
                    binding.courseUploadCardOne.setCardBackgroundColor(Color.parseColor("#8BE600"))
                    binding.courseUploadTextOne.setTextColor(Color.WHITE)
                    binding.courseUploadCardTwo.setCardBackgroundColor(Color.WHITE)
                    binding.courseUploadTextTwo.setTextColor(Color.BLACK)
                    binding.courseUploadProgress.setBackgroundColor(Color.parseColor("#EBEBEB"))
                }
                CourseUploadState.STEP_TWO->{
                    binding.courseUploadCardTwo.setCardBackgroundColor(Color.parseColor("#8BE600"))
                    binding.courseUploadTextTwo.setTextColor(Color.WHITE)
                    binding.courseUploadProgress.setBackgroundColor(Color.parseColor("#8BE600"))
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            id.class_upload_education_level -> {
                hideKeyboard()
                supportFragmentManager.beginTransaction()
                    .addToBackStack("education dialog")
                    .replace(R.id.course_upload_root_root, EducationLevelFragment()).commit()
            }
            id.class_upload_proceed_button -> {
                val courseName = binding.classUploadCourseName.text.trim().toString()
                val shortDescription = binding.classUploadShortDescription.text.trim().toString()
                val organisationName = binding.classUploadOrganisationName.text.trim().toString()
                val level = binding.classUploadEducationLevelText.text.trim().toString()
                val detailedDescription = binding.classUploadDetailedDescription.text.trim().toString()
                if (courseName == "") {
                    Snackbar.make(binding.root, "Empty Class Name!!!", Snackbar.LENGTH_LONG).show()
                    return
                }
                if (detailedDescription == "") {
                    Snackbar.make(binding.root, "Detailed description can't be empty", Snackbar.LENGTH_LONG).show()
                    return
                }
                if (shortDescription == "") {
                    Snackbar.make(binding.root, "Short Description can't be empty", Snackbar.LENGTH_LONG).show()
                    return
                }
                if (level == "") {
                    Snackbar.make(binding.root, "Select a difficult level this course will target", Snackbar.LENGTH_LONG).show()
                    return
                }
                if (shortDescription == detailedDescription) {
                    Snackbar.make(binding.root, "Don't use the same description", Snackbar.LENGTH_LONG).show()
                    return
                }

                hideKeyboard()
                val next = FragmentCompleteCreateCourse()
                val bundle = Bundle()
                bundle.putString("Course name", courseName)
                bundle.putString("short description", shortDescription)
                bundle.putString("organisation name", organisationName)
                bundle.putString("level", level)
                bundle.putString("detailed description", detailedDescription)
                next.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .addToBackStack("complete course")
                    .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                    .replace(id.course_upload_root, next)
                    .commit()
                courseUploadProgressViewModel.setState(CourseUploadState.STEP_TWO)
            }
        }
    }
}

class EducationLevelFragment: Fragment(){
    private lateinit var binding: EducationLevelBinding
    private val educationViewModel: EducationViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = EducationLevelBinding.inflate(inflater, container, false)
        binding.dialogEducationBasic.setOnClickListener {
            educationViewModel.setEducation(getString(R.string.education_basic_primary))
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.dialogEducationSecondary.setOnClickListener {
            educationViewModel.setEducation(getString(R.string.education_secondary_high))
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.dialogEducationTertiary.setOnClickListener {
            educationViewModel.setEducation(getString(R.string.education_tertiary_college))
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.dialogEducationBeginner.setOnClickListener {
            educationViewModel.setEducation(getString(R.string.education_beginner))
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.dialogEducationIntermediate.setOnClickListener {
            educationViewModel.setEducation(getString(R.string.education_intermediate))
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.dialogEducationAdvance.setOnClickListener {
            educationViewModel.setEducation(getString(R.string.education_advance))
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
        return binding.root
    }
}