package com.pinkcloud.memento.edit

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pinkcloud.memento.MainActivity
import com.pinkcloud.memento.R
import com.pinkcloud.memento.SharedViewModel
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabase
import com.pinkcloud.memento.databinding.FragmentEditBinding
import com.pinkcloud.memento.utils.*
import java.io.File

class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private lateinit var viewModel: EditViewModel
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    val args: EditFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        val application = requireActivity().application
        val dataSource = MemoDatabase.getInstance(application).memoDatabaseDao
        val editViewModelFactory = EditViewModelFactory(args.memoId, dataSource, application)

        viewModel = ViewModelProvider(this, editViewModelFactory).get(EditViewModel::class.java)

        viewModel.memo.observe(viewLifecycleOwner, {
            setMemo(binding.memoView, it)
        })
        binding.buttonFlip.setOnClickListener {
            binding.memoView.flip()
        }

        viewModel.isEditCompleted.observe(viewLifecycleOwner, { isEditCompleted ->
            if (isEditCompleted) {
                findNavController().popBackStack()
                viewModel.onEditCompleted()
            }
        })
        sharedViewModel.fontType.observe(viewLifecycleOwner, {
            binding.memoView.setCaptionTextStyle()
        })
        sharedViewModel.fontSizeLevel.observe(viewLifecycleOwner, {
            binding.memoView.setCaptionTextStyle()
        })
        setKeyboardHideAction()

        return binding.root
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
                binding.memoView.apply {
                    val memo = viewModel.memo.value!!
                    memo.frontCaption = frontCaption
                    memo.backCaption = backCaption
                    memo.priority = priority
                    if (isAlarmEnabled) {
                        if (memo.isAlarmEnabled) {
                            memo.alarmId?.let { cancelAlarm(context, memo.alarmId!!) }
                        }
                        val alarmId = scheduleAlarm(context, memo.memoId, frontCaption, alarmTime, imagePath)
                        memo.alarmId = alarmId
                    } else {
                        if (memo.isAlarmEnabled) {
                            memo.alarmId?.let { cancelAlarm(context, memo.alarmId!!) }
                            memo.alarmId = null
                        }
                    }
                    memo.alarmTime = alarmTime
                    memo.isAlarmEnabled = isAlarmEnabled

                    viewModel.updateMemo(memo)
                }
                true
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