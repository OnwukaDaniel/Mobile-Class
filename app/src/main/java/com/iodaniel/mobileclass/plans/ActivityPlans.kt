package com.iodaniel.mobileclass.plans

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityPlansBinding
import com.iodaniel.mobileclass.databinding.FragmentBasicPlanBinding
import com.iodaniel.mobileclass.databinding.FragmentFreePlanBinding
import com.iodaniel.mobileclass.databinding.FragmentPremiumPlanBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

class ActivityPlans : AppCompatActivity(), View.OnClickListener {
    private val fragments: ArrayList<Fragment> = arrayListOf(FragmentFreePlan(), FragmentBasicPlan(), FragmentPremiumPlan())
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val itemDecoration = HorizontalMarginDecoration()
    private val binding by lazy { ActivityPlansBinding.inflate(layoutInflater) }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.activityPlanBack.setOnClickListener(this)
        binding.activityPlansViewpager.offscreenPageLimit = 1

        // PAGE TRANSFORMER
        val currentMarginPx = 124
        val nextMarginPx = 6
        val pageTranslationX = currentMarginPx + nextMarginPx
        val pageTransformer = ViewPager2.PageTransformer { page, position ->
            page.translationX = -pageTranslationX * position
            // Next line scales the item's height. You can remove it if you don't want this effect
            page.scaleY = 1 - (0.25f * abs(position))
            // If you want a fading effect uncomment the next line:
            page.alpha = 0.25f + (1 - abs(position))
        }
        binding.activityPlansViewpager.setPageTransformer(pageTransformer)
        binding.activityPlansViewpager.addItemDecoration(itemDecoration)

        runBlocking { scope.launch { binding.activityPlansViewpager.setCurrentItem(2, true) } }

        viewPagerAdapter = ViewPagerAdapter(this)
        viewPagerAdapter.dataset = fragments
        binding.activityPlansViewpager.adapter = viewPagerAdapter
        (binding.activityPlansViewpager.adapter as RecyclerView.Adapter)
    }

    inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        lateinit var dataset: ArrayList<Fragment>
        override fun getItemCount() = dataset.size
        override fun createFragment(position: Int) = dataset[position]
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.activity_plan_back -> onBackPressed()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
    }
}

class HorizontalMarginDecoration : RecyclerView.ItemDecoration() {
    private val horizontalMarginInPx: Int = 24
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.right = horizontalMarginInPx
        outRect.left = horizontalMarginInPx
    }
}

class FragmentFreePlan : Fragment() {
    private lateinit var binding: FragmentFreePlanBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFreePlanBinding.inflate(inflater, container, false)
        return binding.root
    }
}

class FragmentBasicPlan : Fragment() {
    private lateinit var binding: FragmentBasicPlanBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBasicPlanBinding.inflate(inflater, container, false)
        return binding.root
    }
}

class FragmentPremiumPlan : Fragment() {
    private lateinit var binding: FragmentPremiumPlanBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPremiumPlanBinding.inflate(inflater, container, false)
        return binding.root
    }
}