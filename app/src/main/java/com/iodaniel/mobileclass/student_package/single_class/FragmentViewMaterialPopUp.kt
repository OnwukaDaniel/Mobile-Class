package com.iodaniel.mobileclass.student_package.single_class

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.barteksc.pdfviewer.PDFView
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentViewMaterialPopUpBinding
import java.io.File

class FragmentViewMaterialPopUp(
    private val listOfDownload: ArrayList<String>,
    val classCode: String,
    val gTouchPosition: Int
) : Fragment() {

    private lateinit var binding: FragmentViewMaterialPopUpBinding
    val adapter = PopUpAdapter()
    private lateinit var controller: MediaController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewMaterialPopUpBinding.inflate(inflater, container, false)

        val dir = listOfDownload[gTouchPosition]
        val extension = dir.split(".").last().split("?")[0]
        loadMediaToScreen(File(dir), extension)

        binding.rvViewMaterialPopup.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvViewMaterialPopup.adapter = adapter
        adapter.dataset = listOfDownload
        adapter.activity = requireActivity()
        adapter.classCode = classCode
        return binding.root
    }
    private fun loadMediaToScreen(dir: File, extension: String){
        requireActivity().runOnUiThread{
            when (extension.split("?")[0]) {
                "mp4" -> {
                    binding.viewMaterialPopupUploadVideoView.visibility = View.VISIBLE
                    controller = MediaController(context)
                    controller.setAnchorView(binding.viewMaterialPopupUploadVideoView)
                    binding.viewMaterialPopupUploadVideoView.setMediaController(controller)
                    binding.viewMaterialPopupUploadVideoView.setVideoURI(Uri.fromFile(dir))
                    binding.viewMaterialPopupUploadVideoView.requestFocus()
                    binding.viewMaterialPopupUploadVideoView.start()
                }
                "3gp" -> {
                    binding.viewMaterialPopupUploadVideoView.visibility = View.VISIBLE
                    controller = MediaController(requireContext())
                    controller.setAnchorView(binding.viewMaterialPopupUploadVideoView)
                    binding.viewMaterialPopupUploadVideoView.setMediaController(controller)
                    binding.viewMaterialPopupUploadVideoView.setVideoURI(Uri.fromFile(dir))
                    binding.viewMaterialPopupUploadVideoView.requestFocus()
                    binding.viewMaterialPopupUploadVideoView.start()
                }
                "jpeg" -> {
                    //holder.image.load()
                    binding.viewMaterialPopupImageview.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(Uri.fromFile(dir))
                        .centerCrop()
                        .into(binding.viewMaterialPopupImageview)
                }
                "png" -> {
                    binding.viewMaterialPopupImageview.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(Uri.fromFile(dir))
                        .centerCrop()
                        .into(binding.viewMaterialPopupImageview)
                }
                "jpg" -> {
                    binding.viewMaterialPopupImageview.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(Uri.fromFile(dir))
                        .centerCrop()
                        .into(binding.viewMaterialPopupImageview)
                }
                "wav" -> {

                }
                "aac" -> {

                }
                "pdf" -> {
                    binding.viewMaterialPopupPdfView.visibility = View.VISIBLE
                    binding.viewMaterialPopupPdfView.fromFile(dir).load()
                }
            }
        }
    }
}
class PopUpAdapter: RecyclerView.Adapter<PopUpAdapter.ViewHolder>(){

    lateinit var context: Context
    lateinit var dataset: ArrayList<String>
    lateinit var activity: Activity
    lateinit var classCode: String
    private lateinit var controller: MediaController

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.view_material_popup_row_imageview)
        val musicView: View = itemView.findViewById(R.id.view_material_popup_row_uploadAudio)
        val textView: TextView = itemView.findViewById(R.id.view_material_popup_row_text)
        val uploadLayout: View = itemView.findViewById(R.id.view_material_popup_row_file_layout)
        val pdfView: PDFView = itemView.findViewById(R.id.view_material_popup_row_pdfView)
        val videoView: VideoView = itemView.findViewById(R.id.view_material_popup_row_uploadVideoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_material_popup_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        val extension = dataset[position].split(".").last().split("?")[0]
        loadMediaToScreen(File(datum), holder, extension)
    }

    override fun getItemCount(): Int = dataset.size

    fun loadMediaToScreen(dir: File, holder: ViewHolder, extension: String){
        activity.runOnUiThread{
            holder.textView.text = dir.toString()
            when (extension.split("?")[0]) {
                "mp4" -> {
                    holder.videoView.visibility = View.VISIBLE
                    controller = MediaController(context)
                    controller.setAnchorView(holder.videoView)
                    holder.videoView.setMediaController(controller)
                    holder.videoView.setVideoURI(Uri.fromFile(dir))
                    holder.videoView.requestFocus()
                    //holder.videoView.start()
                }
                "3gp" -> {
                    holder.videoView.visibility = View.VISIBLE
                    controller = MediaController(context)
                    controller.setAnchorView(holder.videoView)
                    holder.videoView.setMediaController(controller)
                    holder.videoView.setVideoURI(Uri.fromFile(dir))
                    holder.videoView.requestFocus()
                    //holder.videoView.start()
                }
                "jpeg" -> {
                    //holder.image.load()
                    holder.image.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(Uri.fromFile(dir))
                        .centerCrop()
                        .into(holder.image)
                }
                "png" -> {
                    holder.image.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(Uri.fromFile(dir))
                        .centerCrop()
                        .into(holder.image)
                }
                "jpg" -> {
                    holder.image.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(Uri.fromFile(dir))
                        .centerCrop()
                        .into(holder.image)
                }
                "wav" -> {

                }
                "aac" -> {

                }
                "pdf" -> {
                    holder.pdfView.visibility = View.VISIBLE
                    holder.pdfView.fromFile(dir).load()
                }
            }
        }
    }
}