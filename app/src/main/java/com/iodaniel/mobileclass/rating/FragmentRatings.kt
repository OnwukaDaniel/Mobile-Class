package com.iodaniel.mobileclass.rating

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.PersonRatingData
import com.iodaniel.mobileclass.databinding.FragmentRatingsBinding
import com.iodaniel.mobileclass.viewModel.RatingDisplayViewModel

class FragmentRatings : Fragment() {
    private lateinit var binding: FragmentRatingsBinding
    private val ratingsAdapter = RatingsRatingsAdapter()
    private val dataset: ArrayList<PersonRatingData> = arrayListOf()
    private val ratingDisplayViewModel: RatingDisplayViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRatingsBinding.inflate(inflater, container, false)
        ratingsAdapter.dataset = dataset
        binding.ratingsRv.adapter = ratingsAdapter
        binding.ratingsRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        var rating = 0.0
        ratingDisplayViewModel.ratingDisplayList.observe(viewLifecycleOwner) {
            for (i in it) if (i !in dataset) {
                dataset.add(i)
                ratingsAdapter.notifyItemInserted(dataset.size)
                rating = (rating + i.ratingStars.toDouble()) / dataset.size
                binding.rating.text = rating.toString()
                binding.ratingEmpty.visibility = View.GONE
            }
        }
        if (dataset.isEmpty()) binding.ratingEmpty.visibility = View.GONE
        return binding.root
    }
}

class RatingsRatingsAdapter : RecyclerView.Adapter<RatingsRatingsAdapter.ViewHolder>() {
    private lateinit var context: Context
    lateinit var activity: Activity
    var dataset: ArrayList<PersonRatingData> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.rating_user_image)
        val name: TextView = itemView.findViewById(R.id.rating_name)
        val ratingText: TextView = itemView.findViewById(R.id.rating_text)
        val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        dataset = if (dataset.size < 3) dataset else dataset.subList(0, 2).toMutableList() as ArrayList<PersonRatingData>
        val view = LayoutInflater.from(context).inflate(R.layout.row_rating_long, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.name.text = datum.name
        holder.ratingText.text = datum.ratingText
        holder.ratingBar.rating = if (datum.ratingStars == "") 0F else datum.ratingStars.toFloat()
        Glide.with(context).load(datum.image).centerCrop().into(holder.image)
    }

    override fun getItemCount() = dataset.size
}