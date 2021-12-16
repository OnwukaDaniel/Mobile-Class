package com.iodaniel.mobileclass.teacher_package.singleclass.material

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityMaterialPageBinding
import com.iodaniel.mobileclass.teacher_package.classes.Material
import com.iodaniel.mobileclass.teacher_package.singleclass.material.MaterialsAdapter.ViewHolder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MaterialPage : AppCompatActivity() {

    private val binding by lazy {
        ActivityMaterialPageBinding.inflate(layoutInflater)
    }
    private var adapter: MaterialsAdapter = MaterialsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (intent.hasExtra("material")) {

            try {
                val json = intent.getStringExtra("material")
                val data: Material = Json.decodeFromString(json!!)
                println("******************************* DATA $data")

                binding.createClassHeading.setText(data.heading)
                binding.createClassNote.setText(data.note)
                binding.createClassExtraNote.setText(data.extraNote)

                val materialUris: ArrayList<String> = data.mediaUris

                binding.rvMaterial
                adapter.dataSet = materialUris


            } catch (e: Exception) {
                println("******************************* DATA ${e.printStackTrace()}")
            }
        }
        /*courseName
note
extraNote
heading
mediaUris
classwork
test
teacherInCharge
year
time
dateModified
dateCreated*/

    }
}

class MaterialsAdapter : RecyclerView.Adapter<ViewHolder>() {

    var dataSet: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val v: View = itemView.findViewById(R.id.)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.materials_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataSet[position]
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

}