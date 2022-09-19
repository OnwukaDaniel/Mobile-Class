package com.iodaniel.mobileclass.util.dialog_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.iodaniel.mobileclass.databinding.OneMessageFragmentBinding
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel

class OneActionFragment: Fragment() {
    private lateinit var binding: OneMessageFragmentBinding

    private val viewModel: MessageFragmentViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = OneMessageFragmentBinding.inflate(inflater, container, false)
        viewModel.okFunction.observe(viewLifecycleOwner) { func ->
            binding.oneMessageOk.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
        viewModel.okText.observe(viewLifecycleOwner) {
            binding.oneMessageOk.text = it
        }
        viewModel.displayText.observe(viewLifecycleOwner) {
            binding.oneMessageMessage.text = it
        }
        binding.oneMessageCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setShowAgain(isChecked)
        }
        binding.oneMessageCheckboxRoot.setOnClickListener{
            binding.oneMessageCheckbox.isChecked = !binding.oneMessageCheckbox.isChecked
            viewModel.setShowAgain(binding.oneMessageCheckbox.isChecked)
        }
        /*
    private val mfV: MessageFragmentViewModel by activityViewModels()

        val dialog = requireActivity().supportFragmentManager.findFragmentByTag("dialog")
        if (dialog != null) ft.remove(dialog)

    val mf = MessageFragment()
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        mfV.setDisplayText("Loren Ipsum")
        mf.show(ft, "dialog")*/
        return binding.root
    }
}