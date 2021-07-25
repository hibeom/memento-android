package com.pinkcloud.memento.add

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.pinkcloud.memento.R
import com.pinkcloud.memento.common.PhotoDialogFragment
import com.pinkcloud.memento.databinding.FragmentAddBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddFragment : Fragment(), PhotoDialogFragment.PhotoDialogListener {

    private lateinit var binding: FragmentAddBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRequestPermissionLauncher()

        binding.layoutCard.frontCard.buttonPhoto.setOnClickListener {
            val dialog = PhotoDialogFragment(this)
            dialog.show(parentFragmentManager, "PhotoFragment")
        }
    }

    private fun setRequestPermissionLauncher() {
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    startCamera()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.camera_permission_not_allowed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun startCamera() {
        findNavController().navigate(AddFragmentDirections.actionAddFragmentToCameraFragment())
    }

    override fun onDialogItemClick(dialog: DialogFragment, id: Int) {
        when (id) {
            0 -> {
                requireContext().checkSelfPermission(Manifest.permission.CAMERA).let {
                    if (it == PackageManager.PERMISSION_GRANTED) startCamera()
                    else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
            1 -> Toast.makeText(context, "Chose gallery", Toast.LENGTH_SHORT).show()
        }
    }
}