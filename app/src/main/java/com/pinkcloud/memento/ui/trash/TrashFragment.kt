package com.pinkcloud.memento.ui.trash

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import com.pinkcloud.memento.MainActivity
import com.pinkcloud.memento.R
import com.pinkcloud.memento.SharedViewModel
import com.pinkcloud.memento.ui.common.AlertDialogFragment
import com.pinkcloud.memento.ui.common.MemoAdapter
import com.pinkcloud.memento.ui.common.MemoView
import com.pinkcloud.memento.ui.common.OverlapLayoutManager
import com.pinkcloud.memento.databinding.FragmentTrashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null

    private val binding get() = _binding!!

    private val viewModel: TrashViewModel by viewModels()
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = MemoAdapter()
        binding.listTrash.adapter = adapter

        viewModel.memos.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        sharedViewModel.run {
            searchText.observe(viewLifecycleOwner, Observer {
                adapter.submitList(viewModel.getFilteredMemos(it))
            })
            fontSizeLevel.observe(viewLifecycleOwner, {
                adapter.notifyDataSetChanged()
            })
            fontType.observe(viewLifecycleOwner, {
                adapter.notifyDataSetChanged()
            })
            orderBy.observe(viewLifecycleOwner, {
                viewModel.onOrderChanged(it)
            })
        }

        val layoutManager = binding.listTrash.layoutManager as OverlapLayoutManager
        binding.buttonRecovery.setOnClickListener {
            val memo = adapter.getMemo(layoutManager.currentPosition)
            memo?.let {
                viewModel.restoreMemo(memo)
            }
        }
        binding.buttonDelete.setOnClickListener {
            val memo = adapter.getMemo(layoutManager.currentPosition)
            val dialog = AlertDialogFragment()
            dialog.content = getString(R.string.ask_delete_permenantly)
            dialog.setOkClickListener(object : AlertDialogFragment.OkClickListener {
                override fun onOkClick() {
                    memo?.let {
                        viewModel.deleteMemo(memo)
                    }
                }
            })
            dialog.show(parentFragmentManager, tag)
        }
        binding.buttonFlip.setOnClickListener {
            val view = layoutManager.getCurrentView()!!
            val memoView = view.findViewById<MemoView>(R.id.memoView)
            memoView.flip()
        }
        setGuideTextAnimation()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_confirm).isVisible = false
        menu.findItem(R.id.action_add).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_menu -> {
                (requireActivity() as MainActivity).openBottomSheetMenu(R.id.trashFragment)
                true
            }
            R.id.action_search -> {
                (activity as MainActivity).setSearchVisibility(View.VISIBLE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setGuideTextAnimation() {
        lifecycleScope.launch {
            whenResumed {
                binding.textTrashGuide.apply {
                    animate().alpha(1f).setDuration(2000).setListener(null)
                }
                delay(4000)
                binding.textTrashGuide.apply {
                    animate().alpha(0f).setDuration(2000).setListener(null)
                }
            }
        }
    }
}