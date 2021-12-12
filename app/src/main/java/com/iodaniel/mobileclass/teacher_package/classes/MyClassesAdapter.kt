package com.iodaniel.mobileclass.teacher_package.classes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.teacher_package.singleclass.AClass
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MyClassesAdapter : RecyclerView.Adapter<MyClassesAdapter.MyCoursesAdapterViewHolder>(),
    View.OnClickListener {

    var dataset: ArrayList<ClassInfo> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var datum: ClassInfo

    class MyCoursesAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.class_image)
        val className: TextView = view.findViewById(R.id.class_name)
        val year: TextView = view.findViewById(R.id.year)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCoursesAdapterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.my_courses_row, parent, false)
        return MyCoursesAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCoursesAdapterViewHolder, position: Int) {
        datum = dataset[position]
        val imageUri = Uri.parse(datum.classImage)
        val red = datum.red
        val green = datum.green
        val blue = datum.blue

        Glide.with(context).load(imageUri).centerCrop().into(holder.image)
        holder.image.setColorFilter(Color.argb(80, red, green, blue))
        holder.className.text = datum.className
        if (holder.year.text != "") {
            holder.year.visibility = View.VISIBLE
            holder.year.text = datum.dateCreated
        }
        holder.itemView.setOnClickListener(this)
    }

    override fun getItemCount(): Int = dataset.size
    override fun onClick(v: View?) {
        when (v?.id){
            R.id.classes_card ->{
                val intent = Intent(context, AClass::class.java)
                val json = Json.encodeToString(datum)
                intent.putExtra("class_data", json)
                context.startActivity(intent)
                activity.overridePendingTransition(0,0)
            }
        }
    }
}