package com.pinkcloud.memento.ui.add

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.pinkcloud.memento.MainActivity
import com.pinkcloud.memento.R
import com.pinkcloud.memento.SharedViewModel
import com.pinkcloud.memento.databinding.FragmentAddBinding
import com.pinkcloud.memento.utils.Constants
import com.pinkcloud.memento.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class AddFragment : Fragment() {

    private lateinit var binding: FragmentAddBinding
    private val viewModel: AddViewModel by viewModels()
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        setKeyboardHideAction()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFlip.setOnClickListener {
            binding.memoView.flip()
        }

        binding.memoView.imagePath = File(requireContext().filesDir, Constants.TEMP_FILE_NAME).absolutePath

        viewModel.isInsertCompleted.observe(viewLifecycleOwner, Observer {isInsertCompleted ->
            if (isInsertCompleted) {
                findNavController().popBackStack()
                viewModel.onInsertCompleted()
            }
        })
        sharedViewModel.fontType.observe(viewLifecycleOwner, {
            binding.memoView.setCaptionTextStyle()
        })
        sharedViewModel.fontSizeLevel.observe(viewLifecycleOwner, {
            binding.memoView.setCaptionTextStyle()
        })
    }

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
            R.id.action_menu -> {
                (requireActivity() as MainActivity).openBottomSheetMenu(R.id.addFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setKeyboardHideAction() {
        binding.root.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    hideKeyboard(requireContext(), v)
                }
            }
            true
        }
    }
}