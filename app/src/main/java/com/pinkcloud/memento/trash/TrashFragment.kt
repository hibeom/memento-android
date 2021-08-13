package com.pinkcloud.memento.trash

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.pinkcloud.memento.MainActivity
import com.pinkcloud.memento.R
import com.pinkcloud.memento.common.MemoAdapter
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabase
import com.pinkcloud.memento.databinding.FragmentTrashBinding
import com.pinkcloud.memento.home.HomeFragmentDirections

class TrashFragment : Fragment(), MemoAdapter.DoubleTapItemListener {

    private var _binding: FragmentTrashBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewModel: TrashViewModel

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

        val adapter = MemoAdapter(this)
        binding.listTrash.adapter = adapter

        viewModel.memos.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

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
        menu.findItem(R.id.action_trash).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(context, "Navigate to Settings Fragment", Toast.LENGTH_LONG).show()
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
        memo.isCompleted = false
        viewModel.restoreMemo(memo)
    }
}