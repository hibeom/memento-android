package com.pinkcloud.memento.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.pinkcloud.memento.MainActivity
import com.pinkcloud.memento.R
import com.pinkcloud.memento.SharedViewModel
import com.pinkcloud.memento.common.MemoAdapter
import com.pinkcloud.memento.common.MemoView
import com.pinkcloud.memento.common.OverlapLayoutManager
import com.pinkcloud.memento.database.MemoDatabase
import com.pinkcloud.memento.databinding.FragmentHomeBinding
import com.pinkcloud.memento.utils.Constants
import com.pinkcloud.memento.utils.shareMemo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        val memoId = arguments?.getLong(Constants.MEMO_ID)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = MemoAdapter()
        binding.listMemo.adapter = adapter
        val layoutManager = binding.listMemo.layoutManager as OverlapLayoutManager

        viewModel.memos.observe(viewLifecycleOwner, { memos ->
            adapter.submitList(memos)
            if (memos.isEmpty()) animateStartViews()

            memoId?.let {
                memos.forEachIndexed { index, memo ->
                    if (memo.memoId == it) {
                        layoutManager.currentPosition = index
                        adapter.notifyDataSetChanged()
                    }
                }
                arguments = null
            }
        })

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        sharedViewModel.run {
            searchText.observe(viewLifecycleOwner, {
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

        binding.buttonTrash.setOnClickListener {
            val memo = adapter.getMemo(layoutManager.currentPosition)!!
            viewModel.completeMemo(memo)
        }
        binding.buttonShare.setOnClickListener {
            val view = layoutManager.getCurrentView()!!
            val memoView = view.findViewById<MemoView>(R.id.memoView)
            memoView.resetVisibility()
            shareMemo(requireContext(), memoView)
        }
        binding.buttonEdit.setOnClickListener {
            val memo = adapter.getMemo(layoutManager.currentPosition)!!
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToEditFragment(
                    memo.memoId
                )
            )
        }
        binding.buttonFlip.setOnClickListener {
            val view = layoutManager.getCurrentView()!!
            val memoView = view.findViewById<MemoView>(R.id.memoView)
            memoView.flip()
        }
        binding.groupStart.setOnClickListener {
            startCamera()
        }

        return binding.root
    }

    private fun animateStartViews() {
        binding.textStart.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(2000).setListener(null)
        }
        binding.imageStart.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(2000).setListener(null)
        }
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_menu -> {
                (requireActivity() as MainActivity).openBottomSheetMenu(R.id.homeFragment)
                true
            }
            R.id.action_add -> {
                startCamera()
                true

            }
            R.id.action_search -> {
                (activity as MainActivity).setSearchVisibility(View.VISIBLE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startCamera() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCameraFragment())
    }
}