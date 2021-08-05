package com.pinkcloud.memento.add

import android.Manifest
import android.app.AlertDialog
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pinkcloud.memento.R
import com.pinkcloud.memento.common.PhotoDialogFragment
import com.pinkcloud.memento.common.getRealPath
import com.pinkcloud.memento.database.MemoDatabase
import com.pinkcloud.memento.databinding.FragmentAddBinding
import com.pinkcloud.memento.utils.Constants
import com.pinkcloud.memento.utils.GlideApp
import java.io.File

class AddFragment : Fragment() {

    private lateinit var binding: FragmentAddBinding
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var viewModel: AddViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        val application = requireActivity().application
        val dataSource = MemoDatabase.getInstance(application).memoDatabaseDao
        val addViewModelFactory = AddViewModelFactory(dataSource, application)

        viewModel = ViewModelProvider(this, addViewModelFactory).get(AddViewModel::class.java)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setImagePickerLauncher()

        binding.buttonFlip.setOnClickListener {
            binding.memoView.flip()
        }

        binding.memoView.imagePath = File(requireContext().filesDir, Constants.TEMP_FILE_NAME).absolutePath

        viewModel.isInsertCompleted.observe(viewLifecycleOwner, Observer {isInsertCompleted ->
            if (isInsertCompleted) {
                findNavController().popBackStack()
                viewModel.isInsertCompleted.value = false
            }
        })
    }

//    private fun setImagePickerLauncher() {
//        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
//            it?.let {
//                getRealPath(requireContext(), it)
//                GlideApp.with(this).load(it).centerCrop()
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(binding.memoView.imagePhoto)
//            }
//        }
//    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_add).isVisible = false
        menu.findItem(R.id.action_search).isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_confirm -> {
                if (binding.memoView.imagePath == null) {
                    AlertDialog.Builder(requireContext()).setMessage(R.string.dialog_no_photo)
                        .show()
                    true
                } else {
                    binding.memoView.apply {
                        viewModel.insertMemo(frontCaption, backCaption, priority, alarmTime, isAlarmEnabled)
                    }
                    true
                }
            }
            R.id.action_settings -> {
                Toast.makeText(context, "Navigate to Settings Fragment", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_trash -> {
                findNavController().navigate(AddFragmentDirections.actionAddFragmentToTrashFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openGallery() {
        getContent.launch("image/*")
    }
}