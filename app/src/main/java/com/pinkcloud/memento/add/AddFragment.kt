package com.pinkcloud.memento.add

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.pinkcloud.memento.Constants
import com.pinkcloud.memento.R
import com.pinkcloud.memento.common.PhotoDialogFragment
import com.pinkcloud.memento.databinding.FragmentAddBinding
import com.pinkcloud.memento.glide.GlideApp
import com.pinkcloud.memento.home.HomeFragmentDirections


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddFragment : Fragment(), PhotoDialogFragment.PhotoDialogListener {

    private lateinit var binding: FragmentAddBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRequestPermissionLauncher()
        setImagePickerLauncher()

        binding.layoutCard.frontCard.buttonPhoto.setOnClickListener {
            val dialog = PhotoDialogFragment(this)
            dialog.show(parentFragmentManager, "PhotoFragment")
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Uri>(Constants.KEY_TEMP_IMAGE_PATH)
            ?.observe(viewLifecycleOwner, Observer {
                GlideApp.with(this).load(it).centerCrop()
                    .into(binding.layoutCard.frontCard.buttonPhoto)
            })
    }

    private fun setImagePickerLauncher() {
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                GlideApp.with(this).load(it).centerCrop()
                    .into(binding.layoutCard.frontCard.buttonPhoto)
            }
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
            1 -> {
                getContent.launch("image/*")
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_add).isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_confirm -> {
                // Insert memo

                findNavController().popBackStack()
                true
            }
            R.id.action_settings -> {
                Toast.makeText(context, "Navigate to Settings Fragment", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}