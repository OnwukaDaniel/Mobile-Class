package com.iodaniel.mobileclass.util.dialog_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.iodaniel.mobileclass.databinding.FragmentMessageBinding
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel

class MessageFragment : Fragment() {
    var type = 0
    private lateinit var binding: FragmentMessageBinding
    private val viewModel: MessageFragmentViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        binding.fragmentMessageCancel.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.fragmentMessageOk.setOnClickListener {
            viewModel.okFunction.observe(viewLifecycleOwner) { func ->
                requireActivity().supportFragmentManager.popBackStack()
                when (func.second) {
                    true -> {
                        requireActivity().supportFragmentManager.beginTransaction()
                            .addToBackStack("module")
                            .replace(func.first.second, func.first.first)
                            .commit()
                    }
                }
            }
        }
        viewModel.cancelText.observe(viewLifecycleOwner) {
            binding.fragmentMessageCancel.text = it
        }
        viewModel.okText.observe(viewLifecycleOwner) {
            binding.fragmentMessageOk.text = it
        }
        viewModel.displayText.observe(viewLifecycleOwner) {
            binding.fragmentMessageMessage.text = it
        }
        binding.fragmentMessageCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowAgain(isChecked)
        }
        binding.fragmentMessageCheckboxRoot.setOnClickListener {
            binding.fragmentMessageCheckbox.isChecked = !binding.fragmentMessageCheckbox.isChecked
            viewModel.setShowAgain(binding.fragmentMessageCheckbox.isChecked)
        }
        return binding.root
    }
}
