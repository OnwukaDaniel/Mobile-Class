package com.iodaniel.mobileclass.student_package

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityViewMaterialBinding
import com.iodaniel.mobileclass.shared_classes.HelperClass
import com.iodaniel.mobileclass.teacher_package.classes.Material
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File


class ViewMaterial : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityViewMaterialBinding.inflate(layoutInflater)
    }
    private lateinit var datum: Material
    private lateinit var classCode: String
    private var adapter = ViewMaterialAdapter()
    private var listOfMediaNames: java.util.ArrayList<String> = arrayListOf()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            readData(datum)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewMaterialBackArrow.setOnClickListener(this)
        if (intent.hasExtra("material")) {
            val json = intent.getStringExtra("material")
            classCode = intent.getStringExtra("classCode")!!
            datum = Json.decodeFromString(json!!)

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                readData(datum)
            } else permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun readData(datum: Material) {
        if (datum.mediaUris.size > 0) binding.viewMaterialNumMaterials.text =
            datum.mediaUris.size.toString()
        println("fileName fileName ************************************ ${datum.listOfMediaNames}")
        binding.viewMaterialHeading.text = datum.heading
        binding.viewMaterialBody.text = datum.note
        binding.viewMaterialExtraNote.text = datum.extraNote
        adapter.dataset = datum.mediaUris
        adapter.listOfMediaNames = datum.listOfMediaNames
        adapter.activity = this
        adapter.classCode = classCode
        binding.rvViewMaterial.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        binding.rvViewMaterial.adapter = adapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.view_material_back_arrow -> onBackPressed()
        }
    }
}

class ViewMaterialAdapter : RecyclerView.Adapter<ViewMaterialAdapter.ViewHolder>() {

    var dataset: ArrayList<String> = arrayListOf()
    var listOfDownload: ArrayList<String> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var classCode: String
    var listOfMediaNames: java.util.ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.view_material_file_attachment_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_material_row, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        val fileName = listOfMediaNames[position]
        val extension = dataset[position].split(".").last().split("?")[0]
        holder.textView.text = fileName
        println("DATA NAME *************************************** $datum")

        val helperClass = HelperClass(datum, classCode, extension, context)
        val fullyQualifiedName = "/$classCode/${helperClass.uniqueName}.$extension"
        val proposedDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + fullyQualifiedName
        )
        listOfDownload.add(proposedDir.toString())
        holder.itemView.setOnClickListener {
            try {
                val dir = File(listOfDownload[position])
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(dir.toString())
                val dirUri = FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
                    dir
                )
                if (dir.exists()) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType((dirUri), mime)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.startActivity(intent)
                } else {
                    downloadAndOpenFile(datum, extension)
                }
            } catch (e: Exception) {
                println("Exception ------------------------------------- ${e.printStackTrace()}")
            }
        }
    }

    private fun downloadAndOpenFile(datum: String, extension: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val helperClass = HelperClass(datum, classCode, extension, context)
            helperClass.download()
            val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val dir = File(helperClass.requestDownloadPath())
                    listOfDownload.add(dir.toString())

                    //AFTER DOWNLOAD IS DONE
                    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(dir.toString())
                    val dirUri = FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".provider",
                        dir
                    )
                    val viewIntent = Intent(Intent.ACTION_VIEW)
                    viewIntent.setDataAndType(dirUri, mime)
                    viewIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    viewIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.startActivity(viewIntent)
                }
            }
            activity.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    override fun getItemCount(): Int = dataset.size
}