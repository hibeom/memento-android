package com.pinkcloud.memento.ui.common

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.pinkcloud.memento.R

class PhotoDialogFragment(private val listener: PhotoDialogListener) : DialogFragment() {

    interface PhotoDialogListener {
        fun onDialogItemClick(dialog: DialogFragment, id: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.Dialog)
            builder.setTitle(R.string.add_photo)
                .setItems(
                    arrayOf(getString(R.string.camera), getString(R.string.gallery))
                ) { _, which ->
                    // The 'which' argument contains the index position
                    // of the selected item
                    listener.onDialogItemClick(this, which)
                }
                .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}