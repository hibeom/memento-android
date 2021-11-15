package com.pinkcloud.memento

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.pinkcloud.memento.ui.common.MenuSheetFragment
import com.pinkcloud.memento.databinding.ActivityMainBinding
import com.pinkcloud.memento.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var menuSheetFragment: MenuSheetFragment
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.search.buttonClose.setOnClickListener {
            binding.search.editSearch.setText("")
            setSearchVisibility(View.GONE)
        }
        binding.search.editSearch.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(this, v)
            } else {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                    showSoftInput(v, 0)
                }
            }
        }
        binding.search.editSearch.doAfterTextChanged {
            sharedViewModel.changeSearchText(it.toString())
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun setSearchVisibility(visibility: Int) {
        binding.search.layoutSearch.visibility = visibility
        if (visibility == View.VISIBLE) {
            requestFocusOnEditSearch()
            binding.toolbar.visibility = View.GONE
        } else binding.toolbar.visibility = View.VISIBLE
    }

    private fun requestFocusOnEditSearch() {
        binding.search.editSearch.requestFocus()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            binding.search.editSearch.let {
                if (it.isFocused) {
                    val rect = Rect()
                    it.getGlobalVisibleRect(rect)
                    if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        it.clearFocus()
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun showActionbar() {
        binding.layoutAppbar.visibility = View.VISIBLE
        supportActionBar?.show()
    }

    fun hideActionbar() {
        binding.layoutAppbar.visibility = View.GONE
        supportActionBar?.hide()
    }

    fun openBottomSheetMenu(@IdRes fragmentId: Int?) {
        menuSheetFragment =
            MenuSheetFragment(fragmentId ?: R.id.homeFragment)
        menuSheetFragment.show(supportFragmentManager, menuSheetFragment.tag)
    }
}