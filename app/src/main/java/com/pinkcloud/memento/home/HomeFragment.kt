package com.pinkcloud.memento.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.pinkcloud.memento.MainActivity
import com.pinkcloud.memento.R
import com.pinkcloud.memento.SharedViewModel
import com.pinkcloud.memento.common.MemoAdapter
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabase
import com.pinkcloud.memento.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), MemoAdapter.DoubleTapItemListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        val application = requireActivity().application
        val dataSource = MemoDatabase.getInstance(application).memoDatabaseDao
        val homeViewModelFactory = HomeViewModelFactory(dataSource, application)

        viewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)

        val adapter = MemoAdapter(this)
        binding.listMemo.adapter = adapter

        viewModel.memos.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        sharedViewModel.searchText.observe(viewLifecycleOwner, {
            adapter.submitList(viewModel.getFilteredMemos(it))
        })
        sharedViewModel.fontSizeLevel.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })
        sharedViewModel.fontType.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setRequestPermissionLauncher()

        super.onViewCreated(view, savedInstanceState)
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
                requireContext().checkSelfPermission(Manifest.permission.CAMERA).let {
                    if (it == PackageManager.PERMISSION_GRANTED) startCamera()
                    else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                true
            }
            R.id.action_search -> {
                (activity as MainActivity).setSearchVisibility(View.VISIBLE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDoubleTapItem(memo: Memo) {
        memo.isCompleted = true
        viewModel.completeMemo(memo)
    }

    private fun setRequestPermissionLauncher() {
        requestCameraPermissionLauncher =
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
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddFragment())
                }
            }
    }

    private fun startCamera() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCameraFragment())
    }
}