package com.iodaniel.mobileclass.teacher_package.singleclass

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection.CheckInternetConnection
import com.iodaniel.mobileclass.databinding.CreateNewLessonFragmentBinding
import com.iodaniel.mobileclass.databinding.ProgressBarDialogBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.ClassMaterialUploadInterface.*
import com.iodaniel.mobileclass.teacher_package.classes.Material
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.DateFormat
import java.util.*

class FragmentCreateNewLesson : Fragment(), OnClickListener,
    ProgressBarController, MediaSupport {

    private lateinit var binding: CreateNewLessonFragmentBinding
    private var fileName = ""
    private val storageRef = FirebaseStorage.getInstance().reference
    private var stTypeRef = FirebaseDatabase.getInstance().reference
    private lateinit var progressBarController: ProgressBarController
    private lateinit var mediaSupport: MediaSupport
    private val dialog by lazy { Dialog(requireContext()) }
    private var classImage: String = ""
    private lateinit var classInfo: ClassInfo
    private lateinit var cn: InternetConnection

    private lateinit var controller: MediaController
    private var listOfMedia: ArrayList<String> = arrayListOf()
    private var listOfMediaNames: ArrayList<String> = arrayListOf()
    private val videoView: VideoView by lazy { binding.newLessonUploadVideoView }

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                try {
                    if (it.data!!.data == null) {
                        return@ActivityResultCallback
                    }
                    if (it.resultCode == AppCompatActivity.RESULT_OK) {
                        val dataUri = it.data!!.data
                        val split = dataUri.toString().split("/")
                        fileName = (split[split.size - 2] + split.last()).split("%2F").last()
                        val contentResolver = requireActivity().contentResolver
                        val mime = MimeTypeMap.getSingleton()
                        var extensionType = ""
                        try {
                            extensionType =
                                mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
                        } catch (e: Exception) {
                            binding.newLessonFilename.text = "Unsupported File"
                            return@ActivityResultCallback
                        }
                        when (extensionType) {
                            "mp4" -> {
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.videoPlayer(dataUri!!)
                                binding.newLessonFilename.text = fileName
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                listOfMediaNames.add(fileName)
                            }
                            "3gp" -> {
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.videoPlayer(dataUri!!)
                                binding.newLessonFilename.text = fileName
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                listOfMediaNames.add(fileName)
                            }
                            "mp3" -> {
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                binding.newLessonFilename.text = fileName
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                listOfMediaNames.add(fileName)
                            }
                            "aac" -> {
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                binding.newLessonFilename.text = fileName
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                listOfMediaNames.add(fileName)
                            }
                            "wav" -> {
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.musicReader(dataUri!!)
                                binding.newLessonFilename.text = fileName
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                listOfMediaNames.add(fileName)
                            }
                            "pdf" -> {
                                mediaSupport.makeMediaPlayersInvisible()
                                mediaSupport.pdfReader(dataUri!!)
                                binding.newLessonFilename.text = fileName
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                listOfMediaNames.add(fileName)
                            }
                            "jpg" -> {
                                mediaSupport.makeMediaPlayersInvisible()
                                classImage = dataUri!!.toString()
                                mediaSupport.imageReader(dataUri)
                                binding.newLessonFilename.text = fileName
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                listOfMediaNames.add(fileName)
                                binding.newLessonImageview.setImageURI(dataUri)
                            }
                            "png" -> {
                                mediaSupport.makeMediaPlayersInvisible()
                                classImage = dataUri!!.toString()
                                mediaSupport.imageReader(dataUri)
                                binding.newLessonFilename.text = fileName
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                binding.newLessonImageview.setImageURI(dataUri)
                                listOfMediaNames.add(fileName)
                            }
                            "jpeg" -> {
                                classImage = dataUri!!.toString()
                                mediaSupport.imageReader(dataUri)
                                binding.newLessonFilename.text = fileName
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                binding.newLessonImageview.setImageURI(dataUri)
                                listOfMediaNames.add(fileName)
                            }
                            else -> {
                                binding.newLessonFilename.text =
                                    "Added but, View is currently unsupported"
                                binding.uploadFileLayout.visibility = View.GONE
                                listOfMedia.add(dataUri.toString() + "filename$fileName")
                                listOfMediaNames.add(fileName)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("GetExternal Uri Exception ******************* ${e.printStackTrace()}")
                }
            })

    override fun onStart() {
        super.onStart()
        cn = InternetConnection(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = CreateNewLessonFragmentBinding.inflate(inflater, container, false)

        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        classInfo = Json.decodeFromString(json!!)

        stTypeRef = stTypeRef
            .child("materials")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(classInfo.classCode)
            .push()

        binding.newLessonUploadImage.setOnCloseIconClickListener(this)
        initialiseAllClassInterface()
        initialiseUtils()
        return binding.root
    }

    private fun initialiseAllClassInterface() {
        progressBarController = this
        mediaSupport = this
    }

    private fun initialiseUtils() {
        binding.newLessonUploadButton.setOnClickListener(this)
        binding.newLessonUploadImage.setOnClickListener(this)
        //binding.newLessonRenameFile.setOnClickListener(this)
    }

    private fun upload() {
        val arrayDownloadUris = arrayListOf<String>()
        val className: String = classInfo.className
        val heading = binding.createClassHeading.text.toString()
        val note = binding.createClassNote.text.toString()
        val extraNote = binding.createClassExtraNote.text.toString()

        val datetime = Calendar.getInstance().time.time
        val dateString = DateFormat.getInstance().format(datetime)
        val split = dateString.split(' ')
        val date = split[0].trim()
        val time = split[1].trim() + split[2].trim()

        if (heading == "") return
        if (note == "") return
        if (extraNote == "") return

        progressBarController.showProgressBar()
        if (listOfMedia.isEmpty()) {
            val material = Material(
                courseName = className, note = note, extraNote = extraNote,
                heading = heading, time = time, dateCreated = classInfo.datetime
            )

            stTypeRef.setValue(material).addOnCompleteListener {
                requireActivity().onBackPressed()
                progressBarController.hideProgressBar()
            }.addOnFailureListener {
                Snackbar.make(
                    binding.root,
                    "Error occurred!!!", Snackbar.LENGTH_LONG
                ).show()
                progressBarController.hideProgressBar()
            }
            return
        }
        if (classInfo.classCode == "") {
            Snackbar.make(binding.root, "Empty Class Code!!!", Snackbar.LENGTH_LONG).show()
            progressBarController.hideProgressBar()
            return
        }

        for (file in listOfMedia) { //fileUris
            val fileUri = Uri.parse(file.split("filename").first())
            val contentResolver = requireContext().contentResolver
            val mime = MimeTypeMap.getSingleton()
            val extension = mime.getExtensionFromMimeType(contentResolver?.getType(fileUri))!!
            val event = (date + time + file).replace("//", ".").replace("/", ".")
            val finalStorageRef = storageRef.child("$event.$extension")
            val uploadTask = finalStorageRef.putFile(fileUri)

            uploadTask.continueWith { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                finalStorageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    finalStorageRef.downloadUrl.addOnSuccessListener {
                        val downloadUri = it.toString()
                        arrayDownloadUris.add(downloadUri)
                        if (arrayDownloadUris.size == listOfMedia.size) {
                            val material = Material()
                            material.courseName = className
                            material.note = note
                            material.extraNote = extraNote
                            material.heading = heading
                            material.time = time
                            material.listOfMediaNames = listOfMediaNames
                            material.mediaUris = arrayDownloadUris
                            material.dateCreated = datetime.toString()

                            stTypeRef.setValue(material).addOnCompleteListener {
                                requireActivity().onBackPressed()
                                progressBarController.hideProgressBar()
                            }.addOnFailureListener {
                                Snackbar.make(
                                    binding.root,
                                    "Error occurred!!!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                progressBarController.hideProgressBar()
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                progressBarController.hideProgressBar()
                val txt = "Upload Failed. Please Try again"
                Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                return@addOnFailureListener
            }
        }
    }

    private fun scaleBitmap(path: String): Bitmap {
        val bm = BitmapFactory.decodeFile(path)
        val inh = (bm.height * (512.0 / bm.width)).toInt()
        return Bitmap.createScaledBitmap(bm, 512, inh, true)
    }

    private fun selectFileFromStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        pickFileLauncher.launch(intent)
    }

    override fun showProgressBar() {
        val progressBarBinding = ProgressBarDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(progressBarBinding.root)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun hideProgressBar() {
        dialog.dismiss()
    }

    override fun videoPlayer(uri: Uri) = try {
        binding.uploadFileLayout.visibility = View.VISIBLE
        videoView.visibility = View.VISIBLE
        controller = MediaController(requireActivity())
        controller.setAnchorView(videoView)
        videoView.setMediaController(controller)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun pdfReader(uri: Uri) = try {
        binding.uploadFileLayout.visibility = View.VISIBLE
        binding.newLessonPdfView.visibility = View.VISIBLE
        binding.newLessonPdfView.fromUri(uri).load()
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun musicReader(uri: Uri) {
        binding.uploadFileLayout.visibility = View.VISIBLE
        binding.newLessonUploadAudio.visibility = View.VISIBLE
        val mp = MediaPlayer()
        mp.setDataSource(requireContext(), uri)
        mp.isLooping = true
        mp.setVolume(0.9F, 0.9F)
    }

    override fun imageReader(uri: Uri) = try {
        binding.uploadFileLayout.visibility = View.VISIBLE
        binding.newLessonImageview.visibility = View.VISIBLE
        binding.newLessonImageview.setImageURI(uri)
    } catch (e: Exception) {
        print("ACTIVITY RESULT ERROR ******************* ${e.printStackTrace()}")
    }

    override fun makeMediaPlayersInvisible() {
        binding.newLessonImageview.visibility = View.INVISIBLE
        binding.newLessonUploadAudio.visibility = View.INVISIBLE
        binding.newLessonPdfView.visibility = View.INVISIBLE
        binding.newLessonUploadVideoView.visibility = View.INVISIBLE
    }

    private fun renameFile() {
        if (fileName == "") return
        val view = layoutInflater.inflate(R.layout.layout_rename_dialog, null)
        val renameTxt = view.findViewById<EditText>(R.id.rename_edit)
        renameTxt.setText(fileName)
        val alertDialog = AlertDialog.Builder(
            requireContext(),
            R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
        )
        alertDialog.setTitle("Rename")
        alertDialog.setView(view)
        alertDialog.setCancelable(true)

        alertDialog.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        alertDialog.setPositiveButton("Done") { dialog, which ->
            fileName = renameTxt.text.toString().trim()
            binding.newLessonFilename.text = fileName

            val oldName = listOfMedia.removeLast()
            val urlPlusExtension = oldName.split("---").first()
            val extension = urlPlusExtension.split(".").last()
            val newName = "$urlPlusExtension----$fileName.$extension"
            if (listOfMedia.add(newName)) {
                Snackbar.make(binding.root, fileName, Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        alertDialog.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.new_lesson_upload_image -> {
                selectFileFromStorage()
            }
            R.id.new_lesson_upload_button -> {
                if (cn != null) {
                    cn.setCustomInternetListener(object : CheckInternetConnection {
                        override fun isConnected() {
                            upload()
                        }

                        override fun notConnected() {
                            val txt = "No active internet!!! Retry"
                            Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                        }
                    })
                } else {
                    val txt = "Retry"
                    Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}