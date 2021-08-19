package com.pinkcloud.memento.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pinkcloud.memento.R
import com.pinkcloud.memento.databinding.FragmentMenuSheetBinding

class MenuSheetFragment: BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMenuSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuSheetBinding.inflate(inflater, container, false)
        binding.buttonCloseMenu.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentDialog);
        dialog.setOnShowListener { it ->
            (it as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { view ->
                val bottomSheetBehavior = BottomSheetBehavior.from(view)
                // Behavior is working. You need to adjust layout size to make it fill parent height.
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        return dialog
    }
}