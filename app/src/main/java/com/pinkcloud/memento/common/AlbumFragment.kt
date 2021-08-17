package com.pinkcloud.memento.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pinkcloud.memento.R
import com.pinkcloud.memento.databinding.FragmentAlbumBinding
import com.pinkcloud.memento.utils.GlideApp
import com.pinkcloud.memento.utils.getRealPath
import com.pinkcloud.memento.utils.saveEmptyTempImage
import com.pinkcloud.memento.utils.saveViewImage
import timber.log.Timber

class AlbumFragment: Fragment() {

    private lateinit var binding: FragmentAlbumBinding
    private lateinit var requestMediaPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumBinding.inflate(inflater, container, false)
        setPermissionRequestLauncher()
        registerContentResult()

        requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE).let {
            if (it == PackageManager.PERMISSION_GRANTED) {
                getContent.launch("image/*")
            } else {
                requestMediaPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonWithoutPhoto.setOnClickListener {
            saveEmptyTempImage(requireContext())
            findNavController().navigate(R.id.action_albumFragment_to_addFragment)
        }
        binding.textPickImage.setOnClickListener {
            getContent.launch("image/*")
        }
        binding.textContinue.setOnClickListener {
            saveViewImage(requireContext(), binding.imagePhoto)
            findNavController().navigate(R.id.action_albumFragment_to_addFragment)
        }
        return binding.root
    }

    private fun registerContentResult() {
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                binding.textContinue.visibility = View.VISIBLE
                binding.textPickImage.visibility = View.VISIBLE

                Timber.d("uri : ${it.path}")
                val path = getRealPath(requireContext(), it)
                Timber.d("real path : $path")
                GlideApp.with(this).load(path).centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.imagePhoto)
            }
        }
    }

    private fun setPermissionRequestLauncher() {
        requestMediaPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    getContent.launch("image/*")
                } else {
                    binding.buttonWithoutPhoto.visibility = View.VISIBLE
                }
            }
    }
}