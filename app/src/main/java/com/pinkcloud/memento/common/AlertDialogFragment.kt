package com.pinkcloud.memento.common

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.pinkcloud.memento.R
import com.pinkcloud.memento.databinding.LayoutAlertDialogBinding

class AlertDialogFragment: DialogFragment() {

    private var listener: OkClickListener? = null
    private lateinit var binding: LayoutAlertDialogBinding
    var content: String? = null

    interface OkClickListener {
        fun onOkClick()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.TransparentDialog)
            val inflater = requireActivity().layoutInflater

            binding = LayoutAlertDialogBinding.inflate(inflater, null, false)
            binding.textContent.text = content?: ""
            binding.buttonOk.setOnClickListener {
                listener?.onOkClick()
                dismiss()
            }
            binding.buttonCancel.setOnClickListener {
                dismiss()
            }
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setOkClickListener(okClickListener: OkClickListener) {
        listener = okClickListener
    }
}