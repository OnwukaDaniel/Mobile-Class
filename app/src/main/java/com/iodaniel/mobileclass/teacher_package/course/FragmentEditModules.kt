package com.iodaniel.mobileclass.teacher_package.course

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.databinding.FragmentEditModulesBinding
import com.iodaniel.mobileclass.repository.EditPlanRepo
import com.iodaniel.mobileclass.util.DirectoryManagement
import com.iodaniel.mobileclass.util.dialog_fragment.InputFragment
import com.iodaniel.mobileclass.util.dialog_fragment.OneActionFragment
import com.iodaniel.mobileclass.viewModel.EditModuleViewModel
import com.iodaniel.mobileclass.viewModel.FabStateForEditModule.CLOSED
import com.iodaniel.mobileclass.viewModel.FabStateForEditModule.OPEN
import com.iodaniel.mobileclass.viewModel.FabStateForEditModuleViewModel
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel

class FragmentEditModules : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentEditModulesBinding
    private val moduleMaterialAdapter = ModuleMaterialAdapter()
    private lateinit var mediaController: MediaController

    private val oneActionFragment = OneActionFragment()
    private lateinit var preference: SharedPreferences
    private val fabStateViewModel = FabStateForEditModuleViewModel()
    private val mfV: MessageFragmentViewModel by activityViewModels()

    private var bold = false
    private var italic = false
    private var underline = false
    private var leftAlign = false
    private var centerAlign = false
    private var rightAlign = false
    private var fontRoot = false
    private var startTextChange = 0
    private var endTextChange = 0

    //private var medialPairList: ArrayList<Pair<String, String>> = arrayListOf()
    private var courseCardData: CourseCardData? = null
    private var fabState = 1
    private var courseCardDataJson = ""
    private val editModuleViewModel = EditModuleViewModel()
    private var modulePosition = 0
    private lateinit var editPlanRepo: EditPlanRepo
    private var plansAndModulesList: ArrayList<PlanModulesExercise> = arrayListOf()
    private val acceptedVideoTypes: ArrayList<String> = arrayListOf("mp4", "3gp")
    private val acceptedTypes: ArrayList<String> = arrayListOf("mp4", "3gp", "mp3", "aac", "wav", "pdf", "jpg", "png", "jpeg", "doc", "docx")
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

                //val directory = directoryManagement.createDirectory(path, dataUri.toString().replace("/", "_"), requireContext())
                //val inputStream = requireContext().contentResolver.openInputStream(dataUri!!)
                //val outputStream = FileOutputStream(directory)
                //inputStream?.copyTo(outputStream, 4 * 1024)
                //inputStream!!.close()
                //outputStream.close()

                for (type in plansAndModulesList[modulePosition].modules.uris) {
                    if (type["filetype"] in acceptedVideoTypes && ext in acceptedVideoTypes) {
                        Snackbar.make(binding.root, "You can only add one video.", Snackbar.LENGTH_LONG).show()
                        return@ActivityResultCallback
                    }
                }
                if (ext in acceptedVideoTypes) showVideo(dataUri!!)

                val uriString = dataUri.toString()
                val lastPathSegment = dataUri!!.lastPathSegment!!
                val name = lastPathSegment.substring(lastPathSegment.lastIndexOf("/") + 1)
                if (ext in acceptedTypes) {
                    plansAndModulesList[modulePosition].modules.uris.add(mutableMapOf("data" to uriString, "filetype" to ext, "filename" to name))
                }
                if (ext in acceptedTypes) {
                    editModuleViewModel.setMediaPresence(true)
                    moduleMaterialAdapter.notifyItemInserted(plansAndModulesList[modulePosition].modules.uris.size)
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
        preference = requireActivity().getSharedPreferences(getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        onClickListeners()
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
        if (plansAndModulesList[modulePosition].modules.uris.isNotEmpty())
            editModuleViewModel.setMediaPresence(true) else editModuleViewModel.setMediaPresence(false)
        val modules = plansAndModulesList[modulePosition]
        requireActivity().title = modules.plan
        editPlanRepo = EditPlanRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)
        binding.editModuleContent.setText(modules.modules.content)
        binding.editModuleHeader.setText(modules.modules.extraNote)
        viewModels()
        moduleMaterialAdapter.activity = requireActivity()
        binding.editModuleRv.adapter = moduleMaterialAdapter
        binding.editModuleRv.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL)
        return binding.root
    }

    private fun spanTesting() {
        class InputWatcher : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                println("Before Text changed *************** TEXT = $s, START = $start, AFTER = $after, COUNT = $count")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                startTextChange = start
                endTextChange = startTextChange + count
                println("After Text changed *************** TEXT = $s, START = $start, BEFORE = $before, COUNT = $count")
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        binding.editModuleContent.addTextChangedListener(InputWatcher())
    }

    private fun onClickListeners() {
        binding.editModuleFab.setOnClickListener(this)
        binding.editModuleSave.setOnClickListener(this)
        binding.editModuleAdd.setOnClickListener(this)
        binding.editModuleNoMediaRoot.setOnClickListener(this)

        binding.editModuleFab.setOnClickListener(this)
        binding.emBold.setOnClickListener(this)
        binding.emItalic.setOnClickListener(this)
        binding.emUnderline.setOnClickListener(this)
        binding.emRightAlign.setOnClickListener(this)
        binding.emLeftAlign.setOnClickListener(this)
        binding.emCenterAlign.setOnClickListener(this)
        binding.emFontRoot.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        when (v?.id) {
            R.id.edit_module_fab -> if (fabState == OPEN) fabStateViewModel.setState(CLOSED) else fabStateViewModel.setState(OPEN)
            R.id.edit_module_add -> pickFileLauncher.launch(intent)
            R.id.edit_module_save -> {
                val content = binding.editModuleContent.text.trim().toString()
                val headerText = binding.editModuleHeader.text.trim().toString()
                editPlanRepo.uploadModule(content = content, headerText = headerText, courseCode = courseCardData!!.courseCode, position = modulePosition,
                    urisInputList = plansAndModulesList[modulePosition].modules.uris, view = binding.root)
            }
            R.id.em_bold -> {
            }
            R.id.em_italic -> {
            }
            R.id.em_underline -> {
            }
            R.id.em_left_align -> {
            }
            R.id.em_center_align -> {
            }
            R.id.em_right_align -> {
            }
            R.id.em_font_root -> {
            }
            R.id.edit_module_no_media_root -> pickFileLauncher.launch(intent)
        }
    }

    private fun viewModels() {
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
        editModuleViewModel.mediaPresenceListener.observe(viewLifecycleOwner) {
            if (it) {
                binding.editModuleNoMediaRoot.visibility = View.GONE
                binding.editModuleRv.visibility = View.VISIBLE
                if (preference.getBoolean(getString(R.string.SHOW_LONG_PRESS_INFO), true)) showLongPressMessage()
            } else {
                binding.editModuleNoMediaRoot.visibility = View.VISIBLE
                binding.editModuleRv.visibility = View.GONE
            }
        }
    }

    private fun showLongPressMessage(){
        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack("dialog")
            .replace(R.id.edit_module_root, oneActionFragment)
            .commit()
        mfV.setDisplayText("Long press on a file to rename it")
        mfV.setOkFunction((Fragment() to 0) to false)
        mfV.showAgain.observe(viewLifecycleOwner){
            val checked = !it
            preference.edit().putBoolean(getString(R.string.SHOW_LONG_PRESS_INFO), checked).apply()
        }
    }

    private fun showVideo(uri: Uri) {
        mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.editModuleVideoView)
        binding.editModuleVideoView.setMediaController(mediaController)
        binding.editModuleVideoView.setVideoURI(uri)
        //binding.editModuleVideoView.requestFocus()
        binding.editModuleVideoCard.visibility = View.VISIBLE
    }

    private fun hideVideo() {
        binding.editModuleVideoCard.visibility = View.GONE
    }

    inner class ModuleMaterialAdapter : RecyclerView.Adapter<ModuleMaterialAdapter.ViewHolder>(), GestureDetector.OnGestureListener {
        private lateinit var context: Context
        lateinit var activity: Activity
        private val mfV: MessageFragmentViewModel by activityViewModels()
        private val inputFragment = InputFragment()
        private lateinit var gestureDetector: GestureDetector
        var dataset: ArrayList<Pair<String, String>> = arrayListOf()
        private var editPosition = 0

        init {
            setHasStableIds(true)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val text: TextView = itemView.findViewById(R.id.row_pref_landing_chip_text)
            val option: ImageView = itemView.findViewById(R.id.row_pref_landing_chip_cancel)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            gestureDetector = GestureDetector(context, this)
            val view = LayoutInflater.from(context).inflate(R.layout.row_pref_landing, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val datum = plansAndModulesList[modulePosition].modules.uris[position]
            holder.text.text = datum["filename"]

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
                if (plansAndModulesList[modulePosition].modules.uris.isNotEmpty())
                    editModuleViewModel.setMediaPresence(true) else editModuleViewModel.setMediaPresence(false)
            }
            holder.text.setOnTouchListener { v, event ->
                editPosition = holder.bindingAdapterPosition
                if (v?.id == R.id.row_pref_landing_chip_text) gestureDetector.onTouchEvent(event)
                return@setOnTouchListener true
            }
        }

        override fun getItemId(position: Int) = position.toLong()

        override fun getItemCount() = plansAndModulesList[modulePosition].modules.uris.size

        override fun onLongPress(e: MotionEvent?) {
            val ft = requireActivity().supportFragmentManager.beginTransaction()
            val pRunnable = Runnable { fun run() {} }
            val nRunnable = Runnable { fun run() {} }

            val dialog = requireActivity().supportFragmentManager.findFragmentByTag("dialog")
            val posMap = plansAndModulesList[modulePosition].modules.uris[editPosition]
            val oldFileName = posMap["filename"]
            if (dialog != null) ft.remove(dialog)
            mfV.setDisplayText("Rename\n$oldFileName\nto")
            mfV.setCancelFunction(nRunnable.run() to true)
            mfV.setOkFunction((Fragment() to 0) to false)
            inputFragment.show(ft, "dialog")

            mfV.editTextInput.observe(viewLifecycleOwner) { newFileName->
                if (newFileName != oldFileName) {
                    plansAndModulesList[modulePosition].modules.uris[editPosition]["filename"] = newFileName
                    notifyItemChanged(editPosition)
                    Snackbar.make(binding.root, "RENAMED TO: $newFileName", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return false
        }

        override fun onShowPress(e: MotionEvent?) {
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return false
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return false
        }
    }
}