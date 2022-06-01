package com.iodaniel.mobileclass.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.databinding.FragmentMarketBinding
import com.iodaniel.mobileclass.repository.MarketRepo

class FragmentMarket : Fragment() {
    private lateinit var binding: FragmentMarketBinding
    private lateinit var marketRepo: MarketRepo
    private var ref = FirebaseDatabase.getInstance().reference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMarketBinding.inflate(inflater, container, false)
        marketRepo = MarketRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)
        requireActivity().setActionBar(binding.marketToolbar)
        requireActivity().actionBar!!.title = "Market"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        marketRepo.getTopCourses(binding.marketTopCoursesRv)
        marketRepo.getRelatedPreference(binding.marketPreferenceRv)
        marketRepo.getOtherCourses(binding.marketOtherCoursesRv)
    }
}

class TopCourseAdapter: RecyclerView.Adapter<TopCourseAdapter.ViewHolder>() {
    private lateinit var context: Context
    var dataset: ArrayList<CourseCardData> = arrayListOf()
    var userTagDataset: ArrayList<String> = arrayListOf()
    class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {
        val row_top_course_title: TextView = itemView.findViewById(R.id.row_top_course_title)
        val row_top_course_description: TextView = itemView.findViewById(R.id.row_top_course_description)
        val row_top_course_rating_bar: RatingBar = itemView.findViewById(R.id.row_top_course_rating_bar)
        val row_top_course_price: TextView = itemView.findViewById(R.id.row_top_course_price)
        val row_top_course_image: ImageView = itemView.findViewById(R.id.row_top_course_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_top_course_landing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.row_top_course_image)
        holder.row_top_course_title.text = datum.courseName
        holder.row_top_course_description.text = datum.shortDescription
        holder.row_top_course_rating_bar.rating = datum.rating.toFloat()
        val price = "$ ${datum.price}"
        holder.row_top_course_price.text = price
    }

    override fun getItemCount() = dataset.size
}

class PreferenceTagAdapter: RecyclerView.Adapter<PreferenceTagAdapter.ViewHolder>(){
    private lateinit var context: Context
    private var ref = FirebaseDatabase.getInstance().reference
    var dataset: ArrayList<String> = arrayListOf()
    var datasetSelected: MutableSet<String> = mutableSetOf()

    class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.row_pref_landing_chip_card)
        val text: TextView = itemView.findViewById(R.id.row_pref_landing_chip_text)
        val cancel: ImageView = itemView.findViewById(R.id.row_pref_landing_chip_cancel)
    }

    init {
        setHasStableIds(true)
        ref = ref.child("user pref tag")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_pref_landing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.text.text = datum

        if (datum in datasetSelected) holder.card.setCardBackgroundColor(Color.GREEN)
        else holder.card.setCardBackgroundColor(Color.CYAN)

        holder.cancel.setOnClickListener {
            val removed = dataset.remove(datum)
            if (removed) notifyItemRemoved(holder.adapterPosition)
        }
        holder.card.setOnClickListener {
            if (datum in datasetSelected) {
                holder.card.setCardBackgroundColor(Color.GREEN)
            }
            else {
                ref.setValue(datasetSelected)
                holder.card.setCardBackgroundColor(Color.CYAN)
            }
        }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = dataset.size
}

class OtherCoursesAdapter: RecyclerView.Adapter<OtherCoursesAdapter.ViewHolder>() {
    private lateinit var context: Context
    var dataset: ArrayList<CourseCardData> = arrayListOf()
    var userTagDataset: ArrayList<String> = arrayListOf()
    class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {
        val courseImage: ImageView = itemView.findViewById(R.id.row_other_courses_image)
        val courseName: TextView = itemView.findViewById(R.id.row_other_courses_title)
        val courseDescription: TextView = itemView.findViewById(R.id.row_other_courses_short_description)
        val studentEnrolled: TextView = itemView.findViewById(R.id.row_other_courses_students_enrolled)
        val price: TextView = itemView.findViewById(R.id.row_other_courses_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_other_courses, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.courseImage)
        holder.courseName.text = datum.courseName
        holder.courseDescription.text = datum.shortDescription
        val price = "$ ${datum.price}"
        holder.price.text = price
        val studentEnrolled = if (datum.studentEnrolled == "") "(0)" else "(${datum.studentEnrolled})"
        holder.studentEnrolled.text = studentEnrolled
        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount() = dataset.size
}