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
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.transition.Fade
import android.view.*
import android.view.GestureDetector.OnDoubleTapListener
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.barteksc.pdfviewer.PDFView
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityViewMaterialBinding
import com.iodaniel.mobileclass.shared_classes.HelperClass
import com.iodaniel.mobileclass.student_package.ViewMaterialAdapter.FragmentInflater
import com.iodaniel.mobileclass.student_package.single_class.FragmentViewMaterialPopUp
import com.iodaniel.mobileclass.teacher_package.classes.Material
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File




class ViewMaterial : AppCompatActivity(), FragmentInflater, View.OnClickListener {

    private val binding by lazy {
        ActivityViewMaterialBinding.inflate(layoutInflater)
    }
    private lateinit var datum: Material
    private lateinit var classCode: String
    private lateinit var fragmentInflater: FragmentInflater
    private var adapter = ViewMaterialAdapter()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            readData(datum)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewMaterialBackArrow.setOnClickListener(this)
        fragmentInflater = this
        if (intent.hasExtra("materialData")) {
            val json = intent.getStringExtra("materialData")
            classCode = intent.getStringExtra("classCode")!!
            datum = Json.decodeFromString(json!!)

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                readData(datum)
            } else permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun readData(datum: Material) {
        if (datum.mediaUris.size > 0) binding.viewMaterialNumMaterials.text =
            datum.mediaUris.size.toString()
        binding.viewMaterialHeading.text = datum.heading
        binding.viewMaterialBody.text = datum.note
        binding.viewMaterialExtraNote.text = datum.extraNote

        binding.rvViewMaterial.adapter = adapter
        binding.rvViewMaterial.layoutManager =
            GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false)
        adapter.dataset = datum.mediaUris
        adapter.activity = this
        adapter.classCode = classCode
        adapter.fragmentInflater = fragmentInflater
    }

    override fun inflateFragment(selectedFragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.view_material_nested_root, selectedFragment)
            .addToBackStack("view_pop_up")
            .commit()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.view_material_back_arrow-> onBackPressed()
        }
    }
}

class ViewMaterialAdapter : RecyclerView.Adapter<ViewMaterialAdapter.ViewHolder>(), OnDoubleTapListener, GestureDetector.OnGestureListener {

    var dataset: ArrayList<String> = arrayListOf()
    var listOfDownload: ArrayList<String> = arrayListOf()
    lateinit var gestureDetector: GestureDetector
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var classCode: String
    private lateinit var controller: MediaController
    lateinit var fragmentInflater: FragmentInflater
    var gTouchPosition: Int=0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.view_material_row_imageview)
        val downloadIcon: ImageView = itemView.findViewById(R.id.view_material_download)
        val musicView: View = itemView.findViewById(R.id.view_material_row_uploadAudio)
        val uploadLayout: View = itemView.findViewById(R.id.view_material_upload_file_layout)
        val pdfView: PDFView = itemView.findViewById(R.id.view_material_row_pdfView)
        val videoView: VideoView = itemView.findViewById(R.id.view_material_row_uploadVideoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        gestureDetector = GestureDetector(context, this)
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_material_row, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (dataset.size > 0) holder.uploadLayout.visibility = View.VISIBLE
        val datum = dataset[position]
        val extension = dataset[position].split(".").last().split("?")[0]
        val helperClass = HelperClass(datum, classCode, extension, context)
        val fullyQualifiedName = "/$classCode/${helperClass.uniqueName}.$extension"
        val proposedDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + fullyQualifiedName)
        if (proposedDir != null) listOfDownload.add(proposedDir.toString())

        if (proposedDir.exists()) loadMediaToScreen(proposedDir, holder, extension)

        holder.downloadIcon.setOnClickListener {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    helperClass.download()
                    val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
                        override fun onReceive(ctxt: Context, intent: Intent) {
                            val dir  = File(helperClass.requestDownloadPath())
                            listOfDownload.add(dir.toString())
                            loadMediaToScreen(dir, holder, extension)
                        }
                    }
                    activity.registerReceiver(onComplete,  IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                } catch (e: Exception) {
                    println("Exception occurred ***************************** ${e.printStackTrace()}")
                }
            }
        }

        holder.itemView.setOnTouchListener { v, event->
            v.performClick()
            if (v?.id == R.id.view_material_upload_base_layout){
                gTouchPosition = position
                gestureDetector.onTouchEvent(event)
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

        holder.pdfView.setOnTouchListener{ v, event->
            v.performClick()
            if (v?.id == R.id.view_material_upload_base_layout){
                gTouchPosition = position
                gestureDetector.onTouchEvent(event)
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
    }

    fun loadMediaToScreen(dir: File, holder: ViewHolder, extension: String){
        activity.runOnUiThread{
            when (extension.split("?")[0]) {
                "mp4" -> {
                    holder.videoView.visibility = View.VISIBLE
                    controller = MediaController(context)
                    controller.setAnchorView(holder.videoView)
                    holder.videoView.setMediaController(controller)
                    holder.videoView.setVideoURI(Uri.fromFile(dir))
                    holder.videoView.requestFocus()
                    //holder.videoView.start()
                    holder.downloadIcon.visibility = View.INVISIBLE
                }
                "3gp" -> {
                    holder.videoView.visibility = View.VISIBLE
                    controller = MediaController(context)
                    controller.setAnchorView(holder.videoView)
                    holder.videoView.setMediaController(controller)
                    holder.videoView.setVideoURI(Uri.fromFile(dir))
                    holder.videoView.requestFocus()
                    //holder.videoView.start()
                    holder.downloadIcon.visibility = View.INVISIBLE
                }
                "jpeg" -> {
                    //holder.image.load()
                    holder.image.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(Uri.fromFile(dir))
                        .centerCrop()
                        .into(holder.image)
                    holder.downloadIcon.visibility = View.INVISIBLE
                }
                "png" -> {
                    holder.image.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(Uri.fromFile(dir))
                        .centerCrop()
                        .into(holder.image)
                    holder.downloadIcon.visibility = View.INVISIBLE
                }
                "jpg" -> {
                    holder.image.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(Uri.fromFile(dir))
                        .centerCrop()
                        .into(holder.image)
                    holder.downloadIcon.visibility = View.INVISIBLE
                }
                "wav" -> {

                }
                "aac" -> {

                }
                "pdf" -> {
                    holder.pdfView.visibility = View.VISIBLE
                    holder.pdfView.fromFile(dir).load()
                    holder.downloadIcon.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int = dataset.size

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        when(e?.action){
            MotionEvent.ACTION_DOWN->{
                println("ACTION_DOWN ------------------------------------- ACTION_DOWN")
                return true
            }
        }
        return false
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        val selectedFragment = FragmentViewMaterialPopUp(listOfDownload, classCode, gTouchPosition)
        val enterFade = Fade()
        enterFade.startDelay = 1
        enterFade.duration = 400

        selectedFragment.enterTransition = enterFade

        try{
            val dir =File(listOfDownload[gTouchPosition])
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(dir.toString())
            val dirUri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                dir
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType((dirUri), mime)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        }catch (e: Exception){
            println("Exception ------------------------------------- ${e.printStackTrace()}")
        }

        //fragmentInflater.inflateFragment(selectedFragment)
        println("onDoubleTap ------------------------------------- onDoubleTap")
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        println("onDoubleTapEvent ------------------------------------- onDoubleTapEvent")
        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        println("onDown ------------------------------------- onDown")
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
        println("onShowPress ------------------------------------- onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        println("onSingleTapUp ------------------------------------- onSingleTapUp")
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        println("onScroll ------------------------------------- onScroll")
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        println("onLongPress ------------------------------------- onLongPress")
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        println("onFling ------------------------------------- onFling")
        return false
    }
    interface FragmentInflater{
        fun inflateFragment(selectedFragment: Fragment)
    }
}