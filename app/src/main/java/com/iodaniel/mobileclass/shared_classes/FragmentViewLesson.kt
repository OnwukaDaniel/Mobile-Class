package com.iodaniel.mobileclass.shared_classes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentViewLessonBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.Material
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class FragmentViewLesson : Fragment() {
    private lateinit var binding: FragmentViewLessonBinding
    private lateinit var material: Material
    private lateinit var classInfo: ClassInfo
    private var adapter = ViewMaterialAdapter()

    override fun onStart() {
        super.onStart()
        val json = requireArguments().getString("lesson")
        val classInfoJson = requireArguments().getString("classInfo")
        classInfo = Json.decodeFromString(classInfoJson!!)
        material = Json.decodeFromString(json!!)
        readData(material)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentViewLessonBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun readData(datum: Material) = CoroutineScope(Dispatchers.IO).launch {
        adapter.dataset = datum.mediaUris
        adapter.listOfMediaNames = datum.listOfMediaNames
        adapter.activity = requireActivity()
        adapter.classCode = classInfo.classCode

        requireActivity().runOnUiThread {
            if (datum.mediaUris.size < 1) binding.materialRoot.visibility = View.GONE
            binding.rvViewLesson.adapter = adapter
            binding.rvViewLesson.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        requireActivity().runOnUiThread {
            binding.viewMaterialHeading.text = datum.heading
            binding.viewMaterialBody.text = datum.note
            binding.viewMaterialExtraNote.text = datum.extraNote
        }
    }
}

class ViewMaterialAdapter : RecyclerView.Adapter<ViewMaterialAdapter.ViewHolder>() {

    var dataset: ArrayList<String> = arrayListOf()
    var listOfDownload: ArrayList<String> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var classCode: String
    var listOfMediaNames: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.view_material_file_attachment_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_material_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataset.size

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        val fileName = listOfMediaNames[position]
        val extension = dataset[position].split(".").last().split("?")[0]
        holder.textView.text = fileName

        val helperClass = HelperClass(datum, classCode, extension, context)
        val fullyQualifiedName = "/$classCode/${helperClass.uniqueName}.$extension"
        val proposedDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + fullyQualifiedName)
        listOfDownload.add(proposedDir.toString())
        holder.itemView.setOnClickListener {
            val downloadHelper = DownloadHelper(context, activity)
            downloadHelper.downloadFromList(datum, extension, classCode, listOfDownload, position)
        }
    }
}