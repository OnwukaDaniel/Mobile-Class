package com.iodaniel.mobileclass.teacher_package.course

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.iodaniel.mobileclass.databinding.FragmentCompleteCreateClassBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.repository.CourseUploadRepo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface
import com.iodaniel.mobileclass.util.ImageCompressor.compressImage
import java.io.ByteArrayInputStream

class FragmentCompleteCreateCourse : Fragment(), ClassMaterialUploadInterface.ProgressBarController {
    private lateinit var binding: FragmentCompleteCreateClassBinding
    private lateinit var courseUploadRepo: CourseUploadRepo
    private var imageByteArray: ByteArray? = null
    private var imageByteInputString: ByteArrayInputStream? = null
    private val minimumPrice = 1.2
    private val maximumPrice = 12
    private val acceptedImageFormat = arrayListOf("jpg", "png", "jpeg")
    private val dialog by lazy { Dialog(requireContext()) }
    private lateinit var progressBarController: ClassMaterialUploadInterface.ProgressBarController

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        try {
            if (it.data!!.data == null) {
                return@ActivityResultCallback
            }
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val dataUri = it.data!!.data
                val pair = compressImage(dataUri!!, requireContext())
                imageByteInputString = pair.first
                imageByteArray = pair.second
                val contentResolver = requireContext().contentResolver
                val mime = MimeTypeMap.getSingleton()
                if (mime.getExtensionFromMimeType(contentResolver?.getType(dataUri))!! in acceptedImageFormat){
                    Glide.with(requireContext())
                        .load(imageByteArray)
                        .centerCrop()
                        .into(binding.completeCreateCourseDisplayPreview)
                }
            }
        } catch (e: Exception) {
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCompleteCreateClassBinding.inflate(inflater, container, false)
        courseUploadRepo = CourseUploadRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)
        requireActivity().setActionBar(binding.completeCreateCourseToolbar)
        requireActivity().actionBar!!.title = "Save course"
        progressBarController = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val courseName = args.getString("Course name")!!
        val shortDescription = args.getString("short description")!!
        val organisationName = args.getString("organisation name")!!
        val level = args.getString("level")!!
        val detailedDescription = args.getString("detailed description")!!

        binding.completeCreateCourseReadPricingInstruction.setOnClickListener {

        }
        binding.completeCreateCourseUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            pickFileLauncher.launch(intent)
        }
        binding.completeCreateCourseUploadButton.setOnClickListener {
            val price = binding.completeCreateCoursePrice.text.trim().toString()
            if (price == "") return@setOnClickListener

            if (price.toInt() < minimumPrice) {
                Snackbar.make(binding.root, "Read the pricing instruction", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (price.toInt() > maximumPrice) {
                Snackbar.make(binding.root, "Read the pricing instruction", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            courseUploadRepo.upload(
                courseName = courseName,
                shortDescription = shortDescription,
                organisationName = organisationName,
                level = level,
                detailedDescription = detailedDescription,
                price = price,
                courseImageInput = imageByteInputString,
                progressBarController
            )
        }
    }

    override fun showProgressBar() {
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        val paramWrap = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.setLayout(paramWrap, paramWrap)
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }
}