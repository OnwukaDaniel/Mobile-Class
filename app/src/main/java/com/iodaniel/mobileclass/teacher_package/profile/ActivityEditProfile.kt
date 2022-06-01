package com.iodaniel.mobileclass.teacher_package.profile

import android.app.Dialog
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.databinding.ActivityEditProfileBinding
import com.iodaniel.mobileclass.repository.ProfileRepo
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.util.BackgroundHelper
import com.iodaniel.mobileclass.util.ImageCompressor.compressImage
import com.iodaniel.mobileclass.viewModel.InstructorEditProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.net.URL

class ActivityEditProfile : AppCompatActivity(), View.OnClickListener, BackgroundHelper {
    private val binding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }
    private lateinit var profileRepo: ProfileRepo
    private var instructorEditProfileViewModel = InstructorEditProfileViewModel()
    private var displayImageStream: ByteArrayInputStream? = null
    private var identificationImageStream: ByteArrayInputStream? = null
    private var certificateImageStream: ByteArrayInputStream? = null
    private val activityIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
    private lateinit var cn: InternetConnection
    private val scope = CoroutineScope(Dispatchers.IO)
    private var instructorDetails: InstructorDetails? = null
    private var connectionRef = FirebaseDatabase.getInstance().reference
    private val connectionListener = ConnectionListener()
    private var fetchedData = false
    private var networkButEmpty = false
    private var pDialog: Dialog? = null

    private val pickDisplayImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        try {
            if (it.data!!.data == null) return@ActivityResultCallback
            if (it.resultCode == RESULT_OK) {
                val dataUri = it.data!!.data
                val contentResolver = contentResolver
                val mime = MimeTypeMap.getSingleton()
                when (mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!) {
                    "jpg" -> instructorEditProfileViewModel.setImageUri(compressImage(dataUri!!, applicationContext))
                    "png" -> instructorEditProfileViewModel.setImageUri(compressImage(dataUri!!, applicationContext))
                    "jpeg" -> instructorEditProfileViewModel.setImageUri(compressImage(dataUri!!, applicationContext))
                }
            }
        } catch (e: Exception) {
        }
    })

    private val pickCertificateLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        try {
            if (it.data!!.data == null) return@ActivityResultCallback
            if (it.resultCode == RESULT_OK) {
                val dataUri = it.data!!.data
                val contentResolver = contentResolver
                val mime = MimeTypeMap.getSingleton()
                when (mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!) {
                    "jpg" -> instructorEditProfileViewModel.setCertificateImage(compressImage(dataUri!!, applicationContext))
                    "png" -> instructorEditProfileViewModel.setCertificateImage(compressImage(dataUri!!, applicationContext))
                    "jpeg" -> instructorEditProfileViewModel.setCertificateImage(compressImage(dataUri!!, applicationContext))
                }
            }
        } catch (e: Exception) {
        }
    })

    private val pickVerificationIdLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        try {
            if (it.data!!.data == null) return@ActivityResultCallback
            if (it.resultCode == RESULT_OK) {
                val dataUri = it.data!!.data
                val contentResolver = contentResolver
                val mime = MimeTypeMap.getSingleton()
                when (mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!) {
                    "jpg" -> instructorEditProfileViewModel.setIdentificationImage(compressImage(dataUri!!, applicationContext))
                    "png" -> instructorEditProfileViewModel.setIdentificationImage(compressImage(dataUri!!, applicationContext))
                    "jpeg" -> instructorEditProfileViewModel.setIdentificationImage(compressImage(dataUri!!, applicationContext))
                }
            }
        } catch (e: Exception) {
        }
    })

    inner class ConnectionListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (!fetchedData) this@ActivityEditProfile.empty() else this@ActivityEditProfile.notEmpty()
            networkButEmpty = true
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        pDialog = Util.progressDialog("Please wait...", this, this)
        pDialog?.show()
        binding.instructorEditProfileImageEdit.setOnClickListener(this)
        binding.instructorEditProfileCertificateButton.setOnClickListener(this)
        binding.instructorEditProfileIdentificationButton.setOnClickListener(this)
        binding.instructorEditProfileSave.setOnClickListener(this)
        cn = InternetConnection(applicationContext)
        profileRepo = ProfileRepo(applicationContext, this)
        instructorEditProfileViewModel = InstructorEditProfileViewModel()
        connectionRef.addValueEventListener(connectionListener)
        scope.launch {
            delay(12_000)
            runOnUiThread {
                if (!fetchedData) this@ActivityEditProfile.empty()
                pDialog?.dismiss()
            }
        }

        cn.setCustomInternetListener(object : InternetConnection.CheckInternetConnection {
            override fun isConnected() {
            }

            override fun notConnected() {
                Snackbar.make(binding.root, "Unable to retrieve Instructor's data. No active connection", Snackbar.LENGTH_LONG).show()
                this@ActivityEditProfile.noInternet()
            }
        })

        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        val instructorDetailsRef = FirebaseDatabase.getInstance().reference
            .child(getString(R.string.instructor_details)) // CONFIRM
            .child(auth)
        instructorDetailsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    fetchedData = true
                    val instructorDetails = snapshot.getValue(InstructorDetails::class.java)!!
                    binding.instructorEditProfileFullName.setText(instructorDetails.instructorName)
                    binding.instructorEditProfileEmail.text = instructorDetails.email
                    binding.instructorEditProfileUsername.setText(instructorDetails.username)
                    binding.instructorEditProfileDateJoined.text = instructorDetails.dateJoined
                    binding.instructorEditProfileAbout.setText(instructorDetails.instructorPersonalDescription)
                    if (instructorDetails.instructorImage != "") {
                        Glide.with(applicationContext).load(Uri.parse(instructorDetails.instructorImage)).centerCrop().into(binding.instructorEditProfileImage)
                        scope.launch(Dispatchers.IO) {
                            val bytes = URL(instructorDetails.instructorImage).openStream().readBytes()
                            displayImageStream = ByteArrayInputStream(bytes)
                        }
                    }
                    if (instructorDetails.instructorCertificationLink != "") {
                        Glide.with(applicationContext)
                            .load(Uri.parse(instructorDetails.instructorCertificationLink))
                            .centerCrop()
                            .into(binding.instructorEditProfileCertificate)
                        scope.launch(Dispatchers.IO) {
                            val bytes = URL(instructorDetails.instructorCertificationLink).openStream().readBytes()
                            identificationImageStream = ByteArrayInputStream(bytes)
                        }
                    }
                    if (instructorDetails.instructorIdentification != "") {
                        Glide.with(applicationContext)
                            .load(Uri.parse(instructorDetails.instructorIdentification))
                            .centerCrop()
                            .into(binding.instructorEditProfileIdentification)
                        scope.launch(Dispatchers.IO) {
                            val bytes = URL(instructorDetails.instructorIdentification).openStream().readBytes()
                            certificateImageStream = ByteArrayInputStream(bytes)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        instructorDetails = profileRepo.decoupleTeacherSharedPreferenceProfile()
        profileRepo.setSharedPreferenceDataToUi()
        instructorEditProfileViewModel.fullName.observe(this) {
            binding.instructorEditProfileFullName.setText(it)
        }
        instructorEditProfileViewModel.username.observe(this) {
            binding.instructorEditProfileUsername.setText(it)
        }
        instructorEditProfileViewModel.about.observe(this) {
            binding.instructorEditProfileAbout.setText(it)
        }
        instructorEditProfileViewModel.image.observe(this) {
            Glide.with(this).load(it.second).centerCrop().into(binding.instructorEditProfileImage)
            displayImageStream = it.first
        }
        instructorEditProfileViewModel.certificateImage.observe(this) {
            Glide.with(this).load(it.second).centerCrop().into(binding.instructorEditProfileCertificate)
            identificationImageStream = it.first
        }
        instructorEditProfileViewModel.identificationImage.observe(this) {
            Glide.with(this).load(it.second).centerCrop().into(binding.instructorEditProfileIdentification)
            certificateImageStream = it.first
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionRef.removeEventListener(connectionListener)
        pDialog?.dismiss()
    }

    override fun empty() {
        Snackbar.make(binding.root, "Create your instructor profile", Snackbar.LENGTH_LONG).show()
        pDialog?.dismiss()
    }

    override fun notEmpty() {
        pDialog?.dismiss()
    }

    override fun noInternet() {
        Snackbar.make(binding.root, "Network error", Snackbar.LENGTH_LONG).show()
        pDialog?.dismiss()
    }

    override fun onClick(v: View?) {
        val snackBar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
        when (v?.id) {
            R.id.instructor_edit_profile_image_edit -> pickDisplayImageLauncher.launch(activityIntent)
            R.id.instructor_edit_profile_certificate_button -> pickCertificateLauncher.launch(activityIntent)
            R.id.instructor_edit_profile_identification_button -> pickVerificationIdLauncher.launch(activityIntent)
            R.id.instructor_edit_profile_save -> {
                val instructorName = binding.instructorEditProfileFullName.text.trim().toString()
                val email = binding.instructorEditProfileEmail.text.trim().toString()
                val username = binding.instructorEditProfileUsername.text.trim().toString()
                val dateJoined = binding.instructorEditProfileDateJoined.text.trim().toString()
                val description = binding.instructorEditProfileAbout.text.trim().toString()
                if (instructorDetails == null) {
                    Snackbar.make(binding.root, "Unable to retrieve Instructor's data. Please retry", Snackbar.LENGTH_LONG).show()
                    return
                }
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
                if (identificationImageStream == null) {
                    snackBar.setText("Upload your identification file").show()
                    return
                }
                if (certificateImageStream == null) {
                    snackBar.setText("Upload your verification file").show()
                    return
                }

                profileRepo.upload(
                    instructorName = instructorName, email = email, username = username, dateJoined = dateJoined, description = description,
                    displayImageStream = displayImageStream!!, identificationImageStream = identificationImageStream!!,
                    certificateImageStream = certificateImageStream!!, instructorDetails!!
                )
            }
        }
    }
}