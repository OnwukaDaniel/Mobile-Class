package com.iodaniel.mobileclass.student_package.owned_courses

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.ModuleData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.databinding.FragmentOwnedSingleModuleBinding
import com.iodaniel.mobileclass.util.Util.convertMillieToHMmSs
import com.iodaniel.mobileclass.viewModel.PMEViewModel
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.annotations.NotNull
import java.text.SimpleDateFormat
import java.util.*


class FragmentOwnedSingleModule : Fragment(), ClickHelper {
    private lateinit var binding: FragmentOwnedSingleModuleBinding
    private val pMEViewModel by activityViewModels<PMEViewModel>()
    private val sSingleAllContentAdapter = SingleAllContentAdapter()
    var modulePosition = 0
    private var player: ExoPlayer? = null
    private var videoUrl = ""
    private var fetch: Fetch? = null
    private var name = ""
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    private var file = Uri.EMPTY
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var playerDatum: MutableMap<String, String> = mutableMapOf()
    private val singleContentAdapter = SingleContentAdapter()
    private var uris: ArrayList<MutableMap<String, String>> = arrayListOf()
    private val imagesTypes: ArrayList<String> = arrayListOf("jpg", "png", "jpeg")
    private val videoTypes: ArrayList<String> = arrayListOf("mp4", "3gp")

    private val playbackListener = object : Player.Listener {
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            super.onVideoSizeChanged(videoSize)
            val videoRatio = videoSize.height / videoSize.width
            val systemWidth = getScreenWidth()
            val calculatedHeight = systemWidth * videoRatio
            binding.displayVideo.layoutParams = ViewGroup.LayoutParams(systemWidth, calculatedHeight)
        }

        var videoDuration = 0L
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                }
                Player.STATE_ENDED -> {}
                Player.STATE_IDLE -> {}
                Player.STATE_READY -> {
                    showFetchingTimeProgress()
                    CoroutineScope(Dispatchers.IO).launch {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(videoUrl, HashMap())
                        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        val timeInMillis = time!!.toLong()
                        retriever.release()
                        videoDuration = timeInMillis
                        convertMillieToHMmSs(timeInMillis)
                        playbackMonitor(timeInMillis)
                    }
                    binding.displayVideo.setOnClickListener {
                        if (player!!.isPlaying) myOnPause() else myOnPlay()
                    }
                }
                else -> {}
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun playbackMonitor(allDuration: Long) {
        var releasedTrack = false
        if (activity != null && isAdded) requireActivity().runOnUiThread {
            handler = Handler(Looper.getMainLooper())
            runnable = Runnable {
                hideFetchingTimeProgress()
                if (player == null) return@Runnable
                val currentDuration = SimpleDateFormat("mm:ss").format(Date(player!!.currentPosition))
                val duration = SimpleDateFormat("mm:ss").format(Date(allDuration))
                binding.progressSeekBar.progress = (player!!.currentPosition * 100 / allDuration).toInt()
                binding.progressStartTime.text = currentDuration.toString()
                binding.progressEndTime.text = duration
                handler.postDelayed(runnable, 1000)
                binding.progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser && releasedTrack){
                            releasedTrack = false
                            if (player != null){
                                val seekPosition = (progress / 100) * allDuration
                                player!!.pause()
                                player!!.seekTo(seekPosition)
                                player!!.play()
                            }
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        releasedTrack = true
                    }
                })
                println("Monitor ****************************** currentDuration: $currentDuration---------------allDuration: $duration")
            }
            handler.postDelayed(runnable, 0)
        }
    }

    private fun showFetchingTimeProgress() {
        binding.progressEndTimeProgress.visibility = View.VISIBLE
        binding.progressEndTime.visibility = View.GONE
    }

    private fun hideFetchingTimeProgress() {
        binding.progressEndTime.visibility = View.VISIBLE
        binding.progressEndTimeProgress.visibility = View.GONE
    }

    private fun getScreenWidth() = requireActivity().windowManager.defaultDisplay.width

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnedSingleModuleBinding.inflate(inflater, container, false)
        modulePosition = requireArguments().getInt("modulePosition")
        pMEViewModel.planModulesExercise.observe(viewLifecycleOwner) {
            val plans = arrayListOf<String>()
            initSetContent(it)
            for (i in it) plans.add(i.plan)

            sSingleAllContentAdapter.plansAndModulesList = it
            sSingleAllContentAdapter.dataset = plans
            sSingleAllContentAdapter.clickHelper = this
            binding.displayDialogRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.displayDialogRv.adapter = sSingleAllContentAdapter

            singleContentAdapter.clickHelper = this
            singleContentAdapter.dataset = uris
            binding.displayRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.displayRv.adapter = singleContentAdapter
            filesIndicator()
        }

        binding.displayTitleFirstSeeMore.setOnClickListener {
            val view = (it as TextView)
            if (view.text == "See more") {
                view.text = "See less"
                binding.displayTextFirst.layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            } else {
                view.text = "See more"
                binding.displayTextFirst.layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            }
        }

        return binding.root
    }

    private fun initialiseExo2(file: Uri) {
        player = ExoPlayer.Builder(requireContext()).build()
        binding.displayVideo.player = player
        val mediaItem = MediaItem.fromUri(file)
        player!!.setMediaItem(mediaItem)
        player!!.playWhenReady = playWhenReady
        player!!.prepare()
        player!!.addListener(playbackListener)
    }

    override fun onResume() {
        super.onResume()
        //hideSystemUi()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackListener)
            exoPlayer.seekTo(0)
            exoPlayer.pause()
            exoPlayer.release()
        }
        player = null
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(requireActivity().window, binding.displayVideo).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun filesIndicator() {
        if (uris.isNotEmpty()) binding.filesIndicator.visibility = View.GONE else binding.filesIndicator.visibility = View.VISIBLE
    }

    private fun initSetContent(it: ArrayList<PlanModulesExercise>) {
        val selectedModule = it[modulePosition].modules
        uris = selectedModule.uris
        setContent(selectedModule)
        val firstUri = if (selectedModule.uris.isNotEmpty()) selectedModule.uris[0] else mutableMapOf()
        this.onAssistedClick(firstUri)
    }

    private fun setContent(selectedModule: ModuleData) {
        binding.displayTitleSecond.text = selectedModule.extraNote
        binding.displayTextSecond.text = selectedModule.content
        binding.displayTitleFirst.text = selectedModule.extraNote
        binding.displayTextFirst.text = selectedModule.content
    }

    private fun loadFile(file: Uri) {
        try {
            initialiseExo2(file)
        } catch (e: Exception) {
            println("Playing video error ***************************************** ${e.printStackTrace()}")
        }
        binding.playToggle2.setOnClickListener {
            if (!player!!.isPlaying) myOnPlay() else myOnPause()
        }
    }

    private fun myOnPause() {
        binding.playToggle2.visibility = View.VISIBLE
        binding.playToggle2.setImageResource(R.drawable.ic_play)
        player!!.pause()
        CoroutineScope(Dispatchers.IO).launch {
            delay(2_000)
            if (activity != null && isAdded) requireActivity().runOnUiThread { binding.playToggle2.visibility = View.GONE }
        }
    }

    private fun myOnPlay() {
        binding.playToggle2.visibility = View.VISIBLE
        binding.playToggle2.setImageResource(R.drawable.ic_pause)
        player!!.play()
        CoroutineScope(Dispatchers.IO).launch {
            delay(2_000)
            if (activity != null && isAdded) requireActivity().runOnUiThread { binding.playToggle2.visibility = View.GONE }
        }
    }

    private fun fileDownloaded() {
        binding.playProgress.visibility = View.VISIBLE
        val httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl)
        httpsReference.downloadUrl.addOnCompleteListener { uri ->
            if (uri.isSuccessful) {
                val url = uri.result.toString()

                val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(requireContext())
                    .setDownloadConcurrentLimit(3)
                    .build()
                fetch = Fetch.getInstance(fetchConfiguration)

                val request = Request(url, file)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL
                request.addHeader("clientKey", videoUrl)

                fetch!!.enqueue(request, { updatedRequest ->
                    println("updatedRequest ********************************************* ${updatedRequest.file}")
                }) { error ->
                    println("Error ********************************************* $error")
                }
                fetch!!.addListener(fetchListener)
            }
        }.addOnFailureListener {
            val exception = it.localizedMessage
            Snackbar.make(binding.root, it.localizedMessage ?: "Error", Snackbar.LENGTH_LONG).show()
        }
    }

    private val fetchListener: FetchListener = object : FetchListener {
        override fun onQueued(@NotNull download: Download, waitingOnNetwork: Boolean) {
            //if (request.id == download.id) {
            //}
        }

        override fun onRemoved(download: Download) {
        }

        override fun onResumed(download: Download) {
        }

        override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
        }

        override fun onWaitingNetwork(download: Download) {
        }

        override fun onAdded(download: Download) {

        }

        override fun onCancelled(download: Download) {
        }

        override fun onCompleted(download: Download) {
            binding.playProgress.visibility = View.GONE
            myOnPlay()
        }

        override fun onDeleted(download: Download) {
        }

        override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            println("download.error ********************************************* ${download.error}")
            Snackbar.make(binding.root, error.name, Snackbar.LENGTH_LONG).show()
        }

        override fun onPaused(download: Download) {
        }

        override fun onProgress(@NotNull download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            println("updatedRequest ********************************************* ${download.progress}")
            println("downloadedBytesPerSecond ********************************************* $downloadedBytesPerSecond")
            //if (request.id == download.id) {
            //}
            val progress = download.progress
            if (progress == 1) binding.playProgress.setProgressCompat(progress, true)
            binding.playProgress.progress = download.progress
        }
    }

    private fun getExtension(input: String): String {
        return input.split(".").last()
    }

    private fun setDisplayToImage() {
        binding.displayImage.visibility = View.VISIBLE
        binding.displayVideo.visibility = View.GONE
        binding.displayContentFirst.visibility = View.GONE
    }

    private fun setDisplayToVideo() {
        binding.displayImage.visibility = View.GONE
        binding.displayVideo.visibility = View.VISIBLE
        binding.displayContentFirst.visibility = View.GONE
    }

    private fun setDisplayToText() {
        binding.displayImage.visibility = View.GONE
        binding.displayVideo.visibility = View.GONE
        binding.displayContentFirst.visibility = View.VISIBLE
        binding.displayContentSecond.visibility = View.VISIBLE
    }

    private fun hideSecondTextLayout() {
        binding.displayTextSecond.visibility = View.GONE
        binding.displayTitleSecond.visibility = View.GONE
        binding.displayContentFirst.visibility = View.VISIBLE
    }



    override fun onAssistedClick(datum: MutableMap<String, String>) {
        releasePlayer()
        setDisplayToText()
        if (datum.isEmpty()) {
            hideSecondTextLayout()
            return
        }
        val extension = getExtension(datum["filetype"]!!)
        if (extension in imagesTypes) {
            setDisplayToImage()
            Glide.with(requireContext()).load(datum["data"]).centerCrop().into(binding.displayImage)
        } else if (extension in videoTypes) {
            playerDatum = datum
            setDisplayToVideo()
            videoUrl = datum["data"]!!
            loadFile(Uri.parse(videoUrl))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onAssistedModulesClick(datum: ModuleData) {
        setContent(datum)
        if (datum.uris.isNotEmpty()) {
            this.onAssistedClick(datum.uris[0])
            uris = datum.uris
            singleContentAdapter.clickHelper = this
            singleContentAdapter.dataset = uris
            binding.displayRv.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            binding.displayRv.adapter = singleContentAdapter
            singleContentAdapter.notifyDataSetChanged()
        } else {
            uris = arrayListOf()
            singleContentAdapter.clickHelper = this
            singleContentAdapter.dataset = arrayListOf()
            binding.displayRv.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            binding.displayRv.adapter = singleContentAdapter
            singleContentAdapter.notifyDataSetChanged()
        }
        filesIndicator()
    }
}

class SingleAllContentAdapter : RecyclerView.Adapter<SingleAllContentAdapter.ViewHolder>() {
    lateinit var context: Context
    lateinit var clickHelper: ClickHelper
    lateinit var plansAndModulesList: ArrayList<PlanModulesExercise>
    var dataset: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.module_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_owned_module, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.content.text = datum
        holder.itemView.setOnClickListener { clickHelper.onAssistedModulesClick(plansAndModulesList[position].modules) }
    }

    override fun getItemCount() = dataset.size
}

class SingleContentAdapter : RecyclerView.Adapter<SingleContentAdapter.ViewHolder>() {
    lateinit var clickHelper: ClickHelper
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var plansAndModulesList: ArrayList<PlanModulesExercise>
    var dataset: ArrayList<MutableMap<String, String>> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.upload_attachment_file_attachment_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_attachment2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.content.text = datum["filename"]
        holder.itemView.setOnClickListener { clickHelper.onAssistedClick(datum) }
    }

    override fun getItemCount() = dataset.size
}

interface ClickHelper {
    fun onAssistedClick(datum: MutableMap<String, String>)
    fun onAssistedModulesClick(datum: ModuleData)
}