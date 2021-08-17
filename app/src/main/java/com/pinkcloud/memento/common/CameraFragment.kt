package com.pinkcloud.memento.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pinkcloud.memento.MainActivity
import com.pinkcloud.memento.R
import com.pinkcloud.memento.databinding.FragmentCameraBinding
import com.pinkcloud.memento.home.HomeFragmentDirections
import com.pinkcloud.memento.utils.Constants
import com.pinkcloud.memento.utils.saveEmptyTempImage
import timber.log.Timber
import java.io.File

class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var imageCapture: ImageCapture

    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().window.statusBarColor = Color.BLACK
        (requireActivity() as MainActivity).hideActionbar()
    }

    override fun onDetach() {
        super.onDetach()
        when (requireContext().resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> {
                requireActivity().window.statusBarColor = Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                requireActivity().window.statusBarColor = Color.WHITE
            }
        }

        (requireActivity() as MainActivity).showActionbar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        setRequestPermissionLauncher()
        setButtonListeners()

        requireContext().checkSelfPermission(Manifest.permission.CAMERA).let {
            if (it == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
            else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        return binding.root
    }

    private fun setButtonListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonTakePhoto.setOnClickListener {
            takePhoto()
        }
        binding.buttonReverseCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }
        binding.buttonWithoutPhoto.setOnClickListener {
            progressWithoutPhoto()
        }
        binding.buttonNext.setOnClickListener {
            val dialog = AlertDialogFragment()
            dialog.content = getString(R.string.ask_progress_without_photo)
            dialog.setOkClickListener(object :AlertDialogFragment.OkClickListener {
                override fun onOkClick() {
                    progressWithoutPhoto()
                }
            })
            dialog.show(parentFragmentManager, tag)
        }
        binding.buttonAlbum.setOnClickListener {
            findNavController().navigate(R.id.action_cameraFragment_to_albumFragment)
        }
    }

    private fun setRequestPermissionLauncher() {
        requestCameraPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    startCamera()
                } else {
                    binding.preview.visibility = View.GONE
                    binding.buttonWithoutPhoto.visibility = View.VISIBLE
                }
            }
    }

    private fun startCamera() {
        binding.groupCameraTools.visibility = View.VISIBLE

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.preview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Timber.e("Error while binding cameraProvider: ${exc.message}")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture

        // Create time-stamped output file to hold the image
        val photoFile = File(
            requireContext().filesDir,
            Constants.TEMP_FILE_NAME
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Timber.e("Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Timber.d("onImageSaved: ${savedUri.path}")
                    TODO("crop image")
                    findNavController().navigate(CameraFragmentDirections.actionCameraFragmentToAddFragment())
                }
            })
    }

    private fun progressWithoutPhoto() {
        saveEmptyTempImage(requireContext())
        findNavController().navigate(R.id.action_cameraFragment_to_addFragment)
    }
}