package com.iodaniel.mobileclass.teacher_package.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.databinding.ActivityEditProfileBinding
import com.iodaniel.mobileclass.liveDataClasses.InstructorProfileLiveData
import com.iodaniel.mobileclass.repository.ProfileRepo
import com.iodaniel.mobileclass.teacher_package.verification.ActivityVerification
import com.iodaniel.mobileclass.util.Dialogs
import com.iodaniel.mobileclass.util.ImageCompressor.compressImage
import com.iodaniel.mobileclass.util.Keyboard.hideKeyboard
import com.iodaniel.mobileclass.viewModel.InstructorEditProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.net.URL
import java.util.*

class ActivityEditProfile : AppCompatActivity(), View.OnClickListener {
    private val binding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }
    private lateinit var profileRepo: ProfileRepo
    private var instructorEditProfileViewModel = InstructorEditProfileViewModel()
    private var displayImageStream: ByteArrayInputStream? = null
    private val activityIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
    private lateinit var cn: InternetConnection
    private var instructorDetailsRef = FirebaseDatabase.getInstance().reference
    private var instructorDetails = InstructorDetails()
    private lateinit var instructorProfileLiveData: InstructorProfileLiveData
    private val scope = CoroutineScope(Dispatchers.IO)
    private val acceptedImageTypes: ArrayList<String> = arrayListOf("jpg", "png", "jpeg")
    private val pickDisplayImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        scope.launch {
            try {
                if (it.data!!.data == null) return@launch
                if (it.resultCode == RESULT_OK) {
                    val dataUri = it.data!!.data
                    val contentResolver = contentResolver
                    val mime = MimeTypeMap.getSingleton()
                    if (mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!! in acceptedImageTypes) {
                        runOnUiThread { instructorEditProfileViewModel.setImageUri(compressImage(dataUri!!, applicationContext, null)) }
                    } else Snackbar.make(binding.root, "Unsupported image type. Supported types are '.jpg', '.png' ", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.instructorEditProfileImageEdit.setOnClickListener(this)
        binding.instructorEditProfileVerification.setOnClickListener(this)
        binding.instructorEditProfileSave.setOnClickListener(this)
        val json = intent.getStringExtra("instructorDetails")
        instructorDetails = Gson().fromJson(json, InstructorDetails::class.java)
        setInitialData()
        cn = InternetConnection(applicationContext)
        profileRepo = ProfileRepo(applicationContext, this)
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        instructorDetailsRef = instructorDetailsRef.child(getString(R.string.instructor_details)).child(auth)
        instructorProfileLiveData = InstructorProfileLiveData(instructorDetailsRef)
        instructorEditProfileViewModel = InstructorEditProfileViewModel()
        instructorEditProfileViewModel.fullName.observe(this) { binding.instructorEditProfileFullName.setText(it) }
        instructorEditProfileViewModel.username.observe(this) { binding.instructorEditProfileUsername.setText(it) }
        instructorEditProfileViewModel.about.observe(this) { binding.instructorEditProfileAbout.setText(it) }
        instructorEditProfileViewModel.image.observe(this) {
            displayImageStream = it.first
            Glide.with(this).load(it.second).centerCrop().into(binding.instructorEditProfileImage)
        }
    }

    private fun setInitialData() {
        val instance = Calendar.getInstance(Locale.getDefault())
        instance.timeInMillis = instructorDetails.dateJoined.toLong()
        val year = instance.get(Calendar.YEAR)
        val month = instance.get(Calendar.MONTH) + 1
        val day = instance.get(Calendar.DAY_OF_MONTH)
        val date = "$day-$month-$year"
        binding.instructorEditProfileFullName.setText(instructorDetails.instructorName)
        binding.instructorEditProfileUsername.setText(instructorDetails.username)
        binding.instructorEditProfileEmail.text = instructorDetails.email
        binding.instructorEditProfileDateJoined.text = date
        binding.instructorEditProfileAbout.setText(instructorDetails.instructorPersonalDescription)
        Glide.with(this).load(Uri.parse(instructorDetails.instructorImage)).centerCrop().into(binding.instructorEditProfileImage)

        if (instructorDetails.instructorImage != "") {
            scope.launch(Dispatchers.IO) {
                val imageBytes: ByteArray = URL(instructorDetails.instructorImage).openStream().readBytes()
                displayImageStream = ByteArrayInputStream(imageBytes)
            }
        }
    }

    override fun onClick(v: View?) {
        val snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        when (v?.id) {
            R.id.instructor_edit_profile_image_edit -> pickDisplayImageLauncher.launch(activityIntent)
            R.id.instructor_edit_profile_verification -> {
                val incompleteProfile = instructorDetails.instructorImage == "" || instructorDetails.instructorPersonalDescription == ""
                        || instructorDetails.instructorName == ""

                if (incompleteProfile) snackBar.setText("Complete your profile to proceed").show()
                if (!incompleteProfile) {
                    val intent = Intent(this, ActivityVerification::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
                }
            }
            R.id.instructor_edit_profile_save -> {
                hideKeyboard()
                val instructorName = binding.instructorEditProfileFullName.text.trim().toString()
                val email = binding.instructorEditProfileEmail.text.trim().toString()
                val username = binding.instructorEditProfileUsername.text.trim().toString()
                val dateJoined = instructorDetails.dateJoined
                val description = binding.instructorEditProfileAbout.text.trim().toString()
                if (instructorName == "") {
                    snackBar.setText("Empty field: Instructor's name").show()
                    return
                }
                if (email == "") {
                    snackBar.setText("Empty field: Instructor's Email").show()
                    return
                }
                if (username == "") {
                    snackBar.setText("Empty field: Username").show()
                    return
                }
                if (description == "") {
                    snackBar.setText("Empty field: Instructor's description").show()
                    return
                }
                if (displayImageStream == null) {
                    snackBar.setText("Select a display image").show()
                    return
                }
                if (displayImageStream == null && instructorDetails.instructorImage != "") {
                    val dialogs = Dialogs()
                    val d = dialogs.circularProgressDialog(activity = this)
                    scope.launch {
                        delay(5_000)
                        if (displayImageStream == null) d.dismiss()
                        runOnUiThread { dialogs.networkErrorDialog(activity = this@ActivityEditProfile) }
                        return@launch
                    }
                }

                if (displayImageStream != null) profileRepo.uploadBio(
                    instructorName = instructorName, email = email, username = username, dateJoined = dateJoined, description = description,
                    displayImageStream = displayImageStream!!, instructorDetails
                )
            }
        }
    }
}