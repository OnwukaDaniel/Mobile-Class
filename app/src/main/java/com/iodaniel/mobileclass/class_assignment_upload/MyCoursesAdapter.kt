package com.iodaniel.mobileclass.class_assignment_upload

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iodaniel.mobileclass.R

class MyCoursesAdapter: RecyclerView.Adapter<MyCoursesAdapter.MyCoursesAdapterViewHolder>() {

    var dataset: ArrayList<MyCourse> = arrayListOf()
    lateinit var context: Context

    class MyCoursesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view){
        val image: ImageView = view.findViewById(R.id.class_image)
        val className: TextView = view.findViewById(R.id.class_name)
        val teacherInCharge: TextView = view.findViewById(R.id.incharge_name)
        val year: TextView = view.findViewById(R.id.year)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCoursesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_courses_row, parent, false)
        return MyCoursesAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCoursesAdapterViewHolder, position: Int) {
        val datum = dataset[position]
        print("dataset[position] ******************************* $datum")
        holder.image.setImageURI(Uri.parse(datum.courseImageUri))
        holder.className.text = datum.courseName
        if (holder.teacherInCharge.text != ""){
            holder.teacherInCharge.visibility = View.VISIBLE
            holder.teacherInCharge.text = datum.teacherInCharge
        }
        if (holder.year.text != ""){
            holder.year.visibility = View.VISIBLE
            holder.year.text = datum.year
        }
    }

    override fun getItemCount(): Int = dataset.size
}