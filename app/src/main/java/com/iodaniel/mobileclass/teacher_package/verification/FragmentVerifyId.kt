package com.iodaniel.mobileclass.teacher_package.verification

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.databinding.FragmentVerifyIdBinding
import com.iodaniel.mobileclass.liveDataClasses.InstructorProfileLiveData
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.util.Dialogs
import com.iodaniel.mobileclass.util.ImageCompressor
import com.iodaniel.mobileclass.viewModel.InstructorEditProfileViewModel
import com.iodaniel.mobileclass.viewModel.UIStateViewModel
import com.iodaniel.mobileclass.viewModel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.net.URL

class FragmentVerifyId : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentVerifyIdBinding
    private val fragmentVerifyCertificate = FragmentVerifyCertificate()
    private val instructorEditProfileViewModel: InstructorEditProfileViewModel by activityViewModels()
    private var instructorDetails: InstructorDetails? = null
    private var idHashMap: HashMap<String, String>? = null
    private val acceptedFormats = arrayListOf("jpg", "png", "jpeg", "doc", "docx", "pdf")
    private val images = arrayListOf("jpg", "png", "jpeg")
    private var instructorDetailsRef = FirebaseDatabase.getInstance().reference
    private lateinit var instructorProfileLiveData: InstructorProfileLiveData
    private val scope = CoroutineScope(Dispatchers.IO)
    private var pro: Dialog? = null
    private var networkButEmpty = false
    private var uIStateViewModel = UIStateViewModel()
    private var dialogs = Dialogs()
    private var fetchedData = false

    private val pickVerificationIdLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        pro?.dismiss()
        try {
            if (it.data!!.data == null) return@registerForActivityResult
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val dataUri = it.data!!.data
                val contentResolver = requireActivity().contentResolver
                val mime = MimeTypeMap.getSingleton()
                val ext = mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
                if (ext in acceptedFormats) {
                    val lastPathSegment = dataUri!!.lastPathSegment!!
                    val name = lastPathSegment.substring(lastPathSegment.lastIndexOf("/") + 1)
                    pro = dialogs.circularProgressDialog(text = "Getting your data..", activity = requireActivity())
                    if (ext in images) instructorEditProfileViewModel.setIdentificationImage(ImageCompressor.compressImage(dataUri, requireContext(), null))
                    instructorEditProfileViewModel.setInstructorIdentificationHash(hashMapOf("filetype" to ext, "filename" to name))
                } else Snackbar.make(binding.root, "File type not supported", Snackbar.LENGTH_LONG).show()
            }
            requireActivity().runOnUiThread { if (pro != null) pro!!.dismiss() }
        } catch (e: Exception) {
            println("Exception ******************************************** ${e.printStackTrace()}")
            requireActivity().runOnUiThread { if (pro != null) pro!!.dismiss() }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVerifyIdBinding.inflate(inflater, container, false)
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        var filename = ""
        instructorDetailsRef = instructorDetailsRef.child(getString(R.string.instructor_details)).child(auth)
        instructorProfileLiveData = InstructorProfileLiveData(instructorDetailsRef)
        binding.verIdSelectType.setOnClickListener(this)
        binding.verIdNext.setOnClickListener(this)
        instructorEditProfileViewModel.instructorIdentificationHash.observe(viewLifecycleOwner) {
            idHashMap = it
            binding.verIdDocName.text = it["filename"]
        }
        instructorProfileLiveData.observe(viewLifecycleOwner) {
            fetchedData = true
            if (it.second == ChildEventTemplate.onDataChange) {
                instructorDetails = it.first
                val idVerificationHash = instructorDetails!!.instructorIdentificationHash
                val certificateVerificationHash = instructorDetails!!.instructorCertificationHash
                if (idVerificationHash.containsKey("fileLink") && filename != idVerificationHash["filename"]!!) { // PREVENTS OVERWRITING OF DATA BY LIVE DATA
                    filename = idVerificationHash["filename"]!!
                    val filetype = idVerificationHash["filetype"]!!
                    val idFileLink = idVerificationHash["fileLink"]!!
                    val certFileLink = certificateVerificationHash["fileLink"]!!
                    instructorEditProfileViewModel.setInstructorIdentificationHash(idVerificationHash)
                    instructorEditProfileViewModel.setInstructorCertificationHash(certificateVerificationHash)
                    var idBytes: ByteArray?
                    scope.launch(Dispatchers.IO) {
                        idBytes = URL(idFileLink).openStream().readBytes()
                        requireActivity().runOnUiThread { instructorEditProfileViewModel.setIdentificationImage(ByteArrayInputStream(idBytes) to idBytes!!) }
                    }
                    var cerBytes: ByteArray?
                    scope.launch(Dispatchers.IO) {
                        cerBytes = URL(certFileLink).openStream().readBytes()
                        requireActivity().runOnUiThread { instructorEditProfileViewModel.setCertificateImage(ByteArrayInputStream(cerBytes) to cerBytes!!) }
                    }
                }
            }
        }
        //uiState()
        return binding.root
    }

    private fun uiState() {
        lateinit var dialog: Dialog
        uIStateViewModel.setUIState(UiState.stateLoading)
        uIStateViewModel.uIState.observe(viewLifecycleOwner) {
            /*when (it) {
                UiState.stateData -> dialog.dismiss()
                UiState.stateLoading -> dialog = dialogs.circularProgressDialog(text = "Please wait", activity = requireActivity())
                UiState.stateNoData -> dialog.dismiss()
                UiState.stateNetworkError -> dialog = dialogs.networkErrorDialog(text = "No network connection", activity = requireActivity())
                UiState.stateSuccess -> dialog = dialogs.successDialog(text = "Success", activity = requireActivity())
            }*/
        }
        scope.launch(Dispatchers.IO) {
            delay(10_000)
            if (!fetchedData) uIStateViewModel.setUIState(UiState.stateNetworkError)
        }
        instructorDetailsRef.get().addOnSuccessListener { dataSnapShot ->
            networkButEmpty = true
            if (dataSnapShot != null) {
                uIStateViewModel.setUIState(UiState.stateData)
            } else {
                uIStateViewModel.setUIState(UiState.stateNoData)
            }
        }
    }

    override fun onClick(v: View?) {
        val intentPickDocument = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intentPickDocument.addCategory(Intent.CATEGORY_OPENABLE)
        intentPickDocument.type = "*/*"
        when (v?.id) {
            R.id.ver_id_select_type -> pickVerificationIdLauncher.launch(intentPickDocument)
            R.id.ver_id_next -> {
                if (idHashMap == null) {
                    val snackBar = Snackbar.make(binding.root, "Upload your ID to proceed", Snackbar.LENGTH_LONG)
                    snackBar.setAction("Pick a File") { pickVerificationIdLauncher.launch(intentPickDocument) }
                    snackBar.show()
                    return
                }
                if (instructorDetails == null) {
                    val snackBar = Snackbar.make(binding.root, "Poor Network Connection. Refresh.", Snackbar.LENGTH_LONG)
                    snackBar.setAction("REFRESH") { requireActivity().supportFragmentManager.beginTransaction().detach(this).attach(this).commit() }
                    snackBar.show()
                    return
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .addToBackStack("certificate")
                    .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                    .replace(R.id.verification_root, fragmentVerifyCertificate)
                    .commit()
            }
        }
    }
}