package com.iodaniel.mobileclass.teacher_package.verification

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.databinding.FragmentCertificateVerificationBinding
import com.iodaniel.mobileclass.repository.ProfileRepo
import com.iodaniel.mobileclass.util.ImageCompressor
import com.iodaniel.mobileclass.viewModel.InstructorEditProfileViewModel
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayInputStream

class FragmentVerifyCertificate : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentCertificateVerificationBinding
    private var certificateImageStream: ByteArrayInputStream? = null
    private val mfV: MessageFragmentViewModel by activityViewModels()
    private var identificationImageStream: ByteArrayInputStream? = null
    private var idHashMap: HashMap<String, String>? = null
    private var certificateHashMap: HashMap<String, String>? = null
    private lateinit var profileRepo: ProfileRepo
    private val instructorEditProfileViewModel : InstructorEditProfileViewModel by activityViewModels()
    private val acceptedFormats = arrayListOf("jpg", "png", "jpeg", "doc", "docx", "pdf", "png")
    private val images = arrayListOf("jpg", "png", "jpeg")
    private var instructorDetails: InstructorDetails? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val pickCertificateLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        try {
            if (it.data!!.data == null) return@ActivityResultCallback
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val dataUri = it.data!!.data
                val contentResolver = requireActivity().contentResolver
                val mime = MimeTypeMap.getSingleton()
                val ext = mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
                if (ext in acceptedFormats) {
                    val lastPathSegment = dataUri!!.lastPathSegment!!
                    val name = lastPathSegment.substring(lastPathSegment.lastIndexOf("/") + 1)
                    if (ext in images)instructorEditProfileViewModel.setCertificateImage(ImageCompressor.compressImage(dataUri, requireContext(), null))
                    instructorEditProfileViewModel.setInstructorCertificationHash(hashMapOf("filetype" to ext, "filename" to name))
                } else Snackbar.make(binding.root, "File type not supported", Snackbar.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
        }
    })

    override fun onSaveInstanceState(oldInstanceState: Bundle) {
        super.onSaveInstanceState(oldInstanceState)
        oldInstanceState.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCertificateVerificationBinding.inflate(inflater, container, false)
        profileRepo = ProfileRepo(requireContext(), requireActivity())
        binding.verCertificateComplete.setOnClickListener(this)
        binding.verCertificateSelectType.setOnClickListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instructorDetails = profileRepo.decoupleTeacherSharedPreferenceProfile()
        instructorEditProfileViewModel.certificateImage.observe(viewLifecycleOwner) {
            certificateImageStream = it.first
        }
        instructorEditProfileViewModel.identificationImage.observe(viewLifecycleOwner) {
            identificationImageStream = it.first
        }
        instructorEditProfileViewModel.instructorCertificationHash.observe(viewLifecycleOwner) {
            certificateHashMap = it
            binding.verCertificateDocName.text = it["filename"]
        }
        instructorEditProfileViewModel.instructorIdentificationHash.observe(viewLifecycleOwner) {
            idHashMap = it
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ver_certificate_complete -> {
                if (instructorDetails == null) return
                if (identificationImageStream == null) return
                if (certificateImageStream == null) {
                    val snackBar = Snackbar.make(binding.root, "Upload your Certificate to proceed", Snackbar.LENGTH_LONG)
                    snackBar.setAction("Pick a File") {
                        pickCertificateLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI))
                    }
                    snackBar.show()
                    return
                }
                profileRepo.uploadVerification(identificationImageStream!!, certificateImageStream!!, idHashMap!!, cerHash = certificateHashMap!!,
                    instructorDetails!!, viewLifecycleOwner, mfV)
            }
            R.id.ver_certificate_select_type -> {
                val intentPickDocument = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intentPickDocument.addCategory(Intent.CATEGORY_OPENABLE)
                intentPickDocument.type = "*/*"
                pickCertificateLauncher.launch(intentPickDocument)
            }
        }
    }
}