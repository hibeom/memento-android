package com.pinkcloud.memento.trash

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.pinkcloud.memento.MainActivity
import com.pinkcloud.memento.R
import com.pinkcloud.memento.SharedViewModel
import com.pinkcloud.memento.common.MemoAdapter
import com.pinkcloud.memento.common.OverlapLayoutManager
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabase
import com.pinkcloud.memento.databinding.FragmentTrashBinding
import timber.log.Timber

class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewModel: TrashViewModel
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        val application = requireActivity().application
        val dataSource = MemoDatabase.getInstance(application).memoDatabaseDao
        val trashViewModelFactory = TrashViewModelFactory(dataSource, application)

        viewModel = ViewModelProvider(this, trashViewModelFactory).get(TrashViewModel::class.java)

        val adapter = MemoAdapter()
        binding.listTrash.adapter = adapter

        viewModel.memos.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            setToolButtonsVisible(it.isNotEmpty())
        })

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        sharedViewModel.searchText.observe(viewLifecycleOwner, Observer {
            adapter.submitList(viewModel.getFilteredMemos(it))
        })
        sharedViewModel.fontSizeLevel.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })
        sharedViewModel.fontType.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })
        sharedViewModel.orderBy.observe(viewLifecycleOwner, {
            viewModel.orderBy.value = it
        })

        val layoutmanager = binding.listTrash.layoutManager as OverlapLayoutManager
        binding.buttonRecovery.setOnClickListener {
            Timber.d("currentPosition:${layoutmanager.currentPosition}")
            val memo = adapter.getMemo(layoutmanager.currentPosition)
            memo?.let {
                viewModel.restoreMemo(memo)
            }
        }
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

    private fun setToolButtonsVisible(visible: Boolean) {
        if (visible) {
            binding.buttonRecovery.visibility = View.VISIBLE
        } else {
            binding.buttonRecovery.visibility = View.GONE
        }
    }
}