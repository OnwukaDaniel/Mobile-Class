package com.iodaniel.mobileclass.teacher_package.course

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.MediaController
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.databinding.FragmentEditModulesBinding
import com.iodaniel.mobileclass.repository.EditPlanRepo
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.util.DirectoryManagement
import com.iodaniel.mobileclass.viewModel.FabStateForEditModule.CLOSED
import com.iodaniel.mobileclass.viewModel.FabStateForEditModule.OPEN
import com.iodaniel.mobileclass.viewModel.FabStateForEditModuleViewModel

class FragmentEditModules : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentEditModulesBinding
    private val moduleMaterialAdapter = ModuleMaterialAdapter()
    private lateinit var mediaController: MediaController
    private val fabStateViewModel = FabStateForEditModuleViewModel()

    //private var medialPairList: ArrayList<Pair<String, String>> = arrayListOf()
    private var courseCardData: CourseCardData? = null
    private var fabState = 1
    private var courseCardDataJson = ""
    private var modulePosition = 0
    private lateinit var editPlanRepo: EditPlanRepo
    private var plansAndModulesList: ArrayList<PlanModulesExercise> = arrayListOf()
    private var pDialog: Dialog? = null
    private val acceptedVideoTypes: ArrayList<String> = arrayListOf("mp4", "3gp")
    private val condition: ArrayList<String> = arrayListOf("mp4", "3gp", "mp3", "aac", "wav", "pdf", "jpg", "png", "jpeg", "doc", "docx")
    private val directoryManagement = DirectoryManagement()
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
        try {
            val path = "${courseCardData!!.instructorInChargeUID}/${courseCardData!!.courseCode}"
            if (it.data!!.data == null) return@ActivityResultCallback
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val dataUri = it.data!!.data
                val contentResolver = requireActivity().contentResolver
                val mime = MimeTypeMap.getSingleton()
                val ext = mime.getExtensionFromMimeType(contentResolver?.getType(dataUri!!))!!
                if (ext in condition) moduleMaterialAdapter.notifyItemInserted(plansAndModulesList[modulePosition].modules.uris.size)

                //val directory = directoryManagement.createDirectory(path, dataUri.toString().replace("/", "_"), requireContext())
                //val inputStream = requireContext().contentResolver.openInputStream(dataUri!!)
                //val outputStream = FileOutputStream(directory)
                //inputStream?.copyTo(outputStream, 4 * 1024)
                //inputStream!!.close()
                //outputStream.close()

                for(type in plansAndModulesList[modulePosition].modules.uris){
                    if (type["filetype"] in acceptedVideoTypes && ext in acceptedVideoTypes){
                        Snackbar.make(binding.root, "You can only add one video.", Snackbar.LENGTH_LONG).show()
                        return@ActivityResultCallback
                    }
                }

                if (ext in acceptedVideoTypes) showVideo(dataUri!!)

                val uriString = dataUri.toString()
                val lastPathSegment = dataUri!!.lastPathSegment!!
                val name = lastPathSegment.substring(lastPathSegment.lastIndexOf("/") + 1)
                when (ext) {
                    "mp4" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "mp4", "filename" to name))
                    "3gp" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "3gp", "filename" to name))
                    "mp3" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "mp3", "filename" to name))
                    "aac" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "aac", "filename" to name))
                    "wav" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "wav", "filename" to name))
                    "pdf" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "pdf", "filename" to name))
                    "jpg" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "jpg", "filename" to name))
                    "png" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "png", "filename" to name))
                    "jpeg" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "jpeg", "filename" to name))
                    "doc" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "doc", "filename" to name))
                    "docx" -> plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to "docx", "filename" to name))
                }
            }
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Unsupported fileType", Snackbar.LENGTH_LONG).show()
            println("GetExternal Uri Exception ******************* ${e.printStackTrace()}")
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditModulesBinding.inflate(inflater, container, false)
        requireActivity().setActionBar(binding.editModuleToolbar)
        pDialog = Util.progressDialog("Please wait...", requireContext(), requireActivity())
        pDialog?.show()

        courseCardDataJson = requireArguments().getString(getString(R.string.manage_course_data_intent))!!
        courseCardData = Gson().fromJson(courseCardDataJson, CourseCardData::class.java)
        modulePosition = requireArguments().getInt("module_position")
        val pmeJson = requireArguments().getString("plansAndModulesList")
        val dummyPMEList = Gson().fromJson(pmeJson, ArrayList::class.java) as ArrayList<*>
        for (pme in dummyPMEList) {
            val json = Gson().toJson(pme)
            val pmeResult: PlanModulesExercise = Gson().fromJson(json, PlanModulesExercise::class.java)
            plansAndModulesList.add(pmeResult)
        }
        editPlanRepo = EditPlanRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)
        val modules = plansAndModulesList[modulePosition]
        requireActivity().title = modules.plan
        binding.editModuleContent.setText(modules.modules.content)
        binding.editModuleExtraNote.setText(modules.modules.extraNote)

        binding.editModuleAddExtraNote.setOnClickListener(this)
        binding.editModuleFab.setOnClickListener(this)
        binding.editModuleSave.setOnClickListener(this)
        binding.editModuleAdd.setOnClickListener(this)
        moduleMaterialAdapter.activity = requireActivity()
        binding.editModuleRv.adapter = moduleMaterialAdapter
        binding.editModuleRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        fabStateViewModel.state.observe(viewLifecycleOwner) {
            fabState = it
            when (fabState) {
                CLOSED -> {
                    binding.editModuleFab.setImageResource(R.drawable.ic_add)
                    binding.editModuleSave.visibility = View.GONE
                    binding.editModuleAdd.visibility = View.GONE
                }
                OPEN -> {
                    binding.editModuleFab.setImageResource(R.drawable.ic_close)
                    binding.editModuleSave.visibility = View.VISIBLE
                    binding.editModuleAdd.visibility = View.VISIBLE
                }
            }
        }
        pDialog?.dismiss()
        return binding.root
    }

    override fun onClick(v: View?) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        when (v?.id) {
            R.id.edit_module_add_extra_note -> if (binding.editModuleExtraNote.visibility == View.VISIBLE) binding.editModuleExtraNote.visibility = View.GONE
            else binding.editModuleExtraNote.visibility = View.VISIBLE
            R.id.edit_module_fab -> if (fabState == OPEN) fabStateViewModel.setState(CLOSED) else fabStateViewModel.setState(OPEN)
            R.id.edit_module_add -> pickFileLauncher.launch(intent)
            R.id.edit_module_save -> {
                val content = binding.editModuleContent.text.trim().toString()
                val extraNote = binding.editModuleExtraNote.text.trim().toString()
                editPlanRepo.uploadModule(content = content, extraNote = extraNote, courseCode = courseCardData!!.courseCode, position = modulePosition,
                    urisInputList = plansAndModulesList[modulePosition].modules.uris, view = binding.root)
            }
        }
    }

    private fun showVideo(uri: Uri){
        mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.editModuleVideoView)
        binding.editModuleVideoView.setMediaController(mediaController)
        binding.editModuleVideoView.setVideoURI(uri)
        //binding.editModuleVideoView.requestFocus()
        binding.editModuleVideoCard.visibility = View.VISIBLE
    }

    private fun hideVideo(){
        binding.editModuleVideoCard.visibility = View.GONE
    }

    inner class ModuleMaterialAdapter : RecyclerView.Adapter<ModuleMaterialAdapter.ViewHolder>() {
        private lateinit var context: Context
        lateinit var activity: Activity
        var dataset: ArrayList<Pair<String, String>> = arrayListOf()
        private val imagesExtList: ArrayList<String> = arrayListOf("jpg", "png", "jpeg")

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val image: ImageView = itemView.findViewById(R.id.row_module_material_image)
            val option: ImageView = itemView.findViewById(R.id.row_module_material_option)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            val view = LayoutInflater.from(context).inflate(R.layout.row_module_material, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val datum = plansAndModulesList[modulePosition].modules.uris[position]
            if (datum["filetype"] in imagesExtList) Glide.with(context).load(datum["data"]).centerCrop().into(holder.image)
            when (datum["filetype"]) {
                "mp4" -> Glide.with(context).load(datum["data"]).centerCrop().into(holder.image)
                "3gp" -> Glide.with(context).load(datum["data"]).centerCrop().into(holder.image)
                "pdf" -> holder.image.setImageResource(R.drawable.pdf_icon)
                "mp3" -> holder.image.setImageResource(R.drawable.music_icon)
                "aac" -> holder.image.setImageResource(R.drawable.music_icon)
                "wav" -> holder.image.setImageResource(R.drawable.music_icon)
                "doc" -> holder.image.setImageResource(R.drawable.doc_icon)
                "docx" -> holder.image.setImageResource(R.drawable.doc_icon)
                else -> holder.image.setImageResource(R.drawable.file)
            }

            holder.itemView.setOnClickListener {
                val local = !datum["data"]!!.startsWith("https")
                if (local) {// A just added material.
                    try {
                        //val dirUri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", datum.second)
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(Uri.parse(datum["data"]), context.contentResolver.getType(Uri.parse(datum["data"])))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        println("STACK ***************************************${e.printStackTrace()}")
                        Snackbar.make(holder.itemView, e.localizedMessage!!.toString(), Snackbar.LENGTH_LONG).show()
                    }
                } else { // A downloaded material.1
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Download")
                    alertDialog.setMessage("Do you want to download this file?")
                    alertDialog.setPositiveButton("Ok") { dialog, _ ->
                        Snackbar.make(holder.itemView, "Implement online download", Snackbar.LENGTH_LONG).show()
                        dialog.dismiss()
                    }
                    alertDialog.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.create().show()
                }
            }
            holder.option.setOnClickListener {
                val pos = plansAndModulesList[modulePosition].modules.uris.indexOf(datum)
                //dataset.removeAt(pos)
                notifyItemRemoved(pos)
                val data = plansAndModulesList[modulePosition].modules.uris[pos]["filetype"]
                if (data in acceptedVideoTypes) hideVideo()
                plansAndModulesList[modulePosition].modules.uris.removeAt(pos)
            }
        }

        override fun getItemCount() = plansAndModulesList[modulePosition].modules.uris.size
    }
}