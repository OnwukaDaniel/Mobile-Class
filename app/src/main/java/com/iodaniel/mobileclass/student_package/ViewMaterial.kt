package com.iodaniel.mobileclass.student_package

import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.barteksc.pdfviewer.PDFView
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityViewMaterialBinding
import com.iodaniel.mobileclass.teacher_package.classes.Material
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ViewMaterial : AppCompatActivity() {

    private val binding by lazy {
        ActivityViewMaterialBinding.inflate(layoutInflater)
    }
    private var adapter= ViewMaterialAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (intent.hasExtra("data")) {
            val json = intent.getStringExtra("data")
            val datum: Material = Json.decodeFromString(json!!)
            readData(datum)
        }
    }


    private fun readData(datum: Material) {
        binding.viewMaterialHeading.text = datum.heading
        binding.viewMaterialBody.text = datum.note
        binding.viewMaterialExtraNote.text = datum.extraNote

        binding.rvViewMaterial.adapter = adapter
        binding.rvViewMaterial.layoutManager =
            GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false)
        adapter.dataset = datum.mediaUris
    }
}

class ViewMaterialAdapter : RecyclerView.Adapter<ViewMaterialAdapter.ViewHolder>() {

    var dataset: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.view_material_row_imageview)
        val musicView: View = itemView.findViewById(R.id.view_material_row_uploadAudio)
        val pdfView: PDFView = itemView.findViewById(R.id.view_material_row_pdfView)
        val videoView: VideoView = itemView.findViewById(R.id.view_material_row_uploadVideoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_material_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        println("************************** $datum")
    }

    override fun getItemCount(): Int = dataset.size

}