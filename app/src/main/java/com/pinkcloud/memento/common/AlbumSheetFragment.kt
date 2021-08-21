package com.pinkcloud.memento.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pinkcloud.memento.databinding.FragmentAlbumSheetBinding

class AlbumSheetFragment: BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAlbumSheetBinding
    private lateinit var requestMediaPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumSheetBinding.inflate(inflater, container, false)
        requestMediaPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // TODO fetch image files from media
                } else {
                    Toast.makeText(requireContext(), "No media permission", Toast.LENGTH_SHORT).show()
                }
            }
        requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE).let {
            if (it == PackageManager.PERMISSION_GRANTED) {
                // TODO fetch image files from media
            } else {
                requestMediaPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        return binding.root
    }
}