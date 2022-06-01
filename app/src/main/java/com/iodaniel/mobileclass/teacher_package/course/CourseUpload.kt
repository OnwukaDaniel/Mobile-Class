package com.iodaniel.mobileclass.teacher_package.course

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.R.id
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.databinding.CourseUploadBinding
import com.iodaniel.mobileclass.repository.CourseUploadRepo
import com.iodaniel.mobileclass.viewModel.CourseUploadViewModel

class CourseUpload : AppCompatActivity(), View.OnClickListener{

    private val binding by lazy { CourseUploadBinding.inflate(layoutInflater) }
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var errorSnackBar: Snackbar
    private lateinit var cn: InternetConnection
    private lateinit var courseUploadRepo: CourseUploadRepo
    private val courseUploadViewModel = CourseUploadViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.includeTeacherCreateCourseToolbar)
        title = "Create Course"
        val txt = "Upload Failed. Please Try again"
        binding.classUploadEducationLevel.setOnClickListener(this)
        binding.classUploadProceedButton.setOnClickListener(this)
        cn = InternetConnection(applicationContext)
        errorSnackBar = Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG)
        courseUploadRepo = CourseUploadRepo(this, applicationContext, binding.root, this)
        courseUploadViewModel.level.observe(this) {
            binding.classUploadEducationLevelText.text = it!!
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            id.class_upload_education_level -> {
                val pair = courseUploadRepo.pickLevel()
                val view = pair.first
                val alertDialog = pair.second
                val basic: LinearLayout = view.findViewById(R.id.dialog_education_basic)
                val secondary: LinearLayout = view.findViewById(R.id.dialog_education_secondary)
                val tertiary: LinearLayout = view.findViewById(R.id.dialog_education_tertiary)
                val beginner: LinearLayout = view.findViewById(R.id.dialog_education_beginner)
                val intermediate: LinearLayout = view.findViewById(R.id.dialog_education_intermediate)
                val advance: LinearLayout = view.findViewById(R.id.dialog_education_advance)
                basic.setOnClickListener {
                    courseUploadViewModel.setLevel(getString(R.string.education_basic_primary))
                    alertDialog.dismiss()
                }
                secondary.setOnClickListener {
                    courseUploadViewModel.setLevel(getString(R.string.education_secondary_high))
                    alertDialog.dismiss()
                }
                tertiary.setOnClickListener {
                    courseUploadViewModel.setLevel(getString(R.string.education_tertiary_college))
                    alertDialog.dismiss()
                }
                beginner.setOnClickListener {
                    courseUploadViewModel.setLevel(getString(R.string.education_beginner))
                    alertDialog.dismiss()
                }
                intermediate.setOnClickListener {
                    courseUploadViewModel.setLevel(getString(R.string.education_intermediate))
                    alertDialog.dismiss()
                }
                advance.setOnClickListener {
                    courseUploadViewModel.setLevel(getString(R.string.education_advance))
                    alertDialog.dismiss()
                }
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
                if (courseName == "" || shortDescription == "" || level == "" || detailedDescription == "") return

                if (shortDescription == detailedDescription) {
                    Snackbar.make(binding.root, "Don't use the same description", Snackbar.LENGTH_LONG).show()
                    return
                }

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
                    .setCustomAnimations(R.anim.slide_in, R.anim.slide_in)
                    .replace(id.course_upload_root, next)
                    .commit()
            }
        }
    }
}