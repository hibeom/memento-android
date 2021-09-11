package com.pinkcloud.memento.common

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pinkcloud.memento.R
import com.pinkcloud.memento.databinding.FragmentAlbumBinding
import com.pinkcloud.memento.utils.GlideApp
import com.pinkcloud.memento.utils.getRealPath
import com.pinkcloud.memento.utils.saveEmptyTempImage
import com.pinkcloud.memento.utils.saveViewImage
import com.yashoid.instacropper.InstaCropperView
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
            binding.imagePhoto.crop(
                View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            ) {
                saveViewImage(requireContext(), it)
                findNavController().navigate(R.id.action_albumFragment_to_addFragment)
            }
        }

        return binding.root
    }

    private fun registerContentResult() {
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it == null) findNavController().popBackStack()
            else {
                binding.textContinue.visibility = View.VISIBLE
                binding.textPickImage.visibility = View.VISIBLE
//                val path = getRealPath(requireContext(), it)
//                GlideApp.with(this).load(it).centerCrop()
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(binding.imagePhoto)
//                val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, it))
//                binding.imagePhoto.setImageBitmap(bitmap)
                binding.imagePhoto.setImageUri(it)
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