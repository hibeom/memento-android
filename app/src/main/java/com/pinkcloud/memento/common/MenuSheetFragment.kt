package com.pinkcloud.memento.common

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pinkcloud.memento.R
import com.pinkcloud.memento.SharedViewModel
import com.pinkcloud.memento.databinding.FragmentMenuSheetBinding
import com.pinkcloud.memento.utils.Configuration
import com.pinkcloud.memento.utils.getFontName
import com.pinkcloud.memento.utils.getMeasuredFontSize
import com.pinkcloud.memento.utils.getTypeface
import timber.log.Timber

class MenuSheetFragment(private val fragmentId: Int?) :
    BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMenuSheetBinding
    @IdRes
    private var trashAction: Int = R.id.action_homeFragment_to_trashFragment
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuSheetBinding.inflate(inflater, container, false)
        sharedViewModel.fontSizeLevel.observe(viewLifecycleOwner, {
            Timber.d("fontType: ${Configuration.fontType}, fontSizeLevel: ${Configuration.fontSizeLevel}")
            binding.textFontSample.textSize = getMeasuredFontSize()
        })
        sharedViewModel.fontType.observe(viewLifecycleOwner, {
            Timber.d("fontType: ${Configuration.fontType}, fontSizeLevel: ${Configuration.fontSizeLevel}")
            val typeface = getTypeface(requireContext())
            binding.textFontFamily.typeface = typeface
            binding.textFontFamily.text = getFontName(requireContext())
            binding.textFontSample.typeface = typeface
            binding.textFontSample.textSize = getMeasuredFontSize()
        })
        binding.buttonCloseMenu.setOnClickListener {
            dismiss()
        }
        when (fragmentId) {
            R.id.trashFragment -> {
                binding.layoutTrash.visibility = View.GONE
            }
            R.id.homeFragment -> {
                trashAction = R.id.action_homeFragment_to_trashFragment
            }
            R.id.addFragment -> {
                trashAction = R.id.action_addFragment_to_trashFragment
            }
        }
        binding.buttonTrash.setOnClickListener {
            findNavController().navigate(trashAction)
            dismiss()
        }
        binding.buttonTextBigger.setOnClickListener {
            sharedViewModel.enlargeFont()
        }
        binding.buttonTextSmaller.setOnClickListener {
            sharedViewModel.reduceFont()
        }
        binding.buttonFontFamily.setOnClickListener {
            sharedViewModel.changeFont()
        }
        binding.textFontFamily.text = getFontName(requireContext())
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentDialog);
        dialog.setOnShowListener { it ->
            (it as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let { view ->
                    val bottomSheetBehavior = BottomSheetBehavior.from(view)
                    // Behavior is working. You need to adjust layout size to make it fill parent height.
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
        }
        return dialog
    }
}