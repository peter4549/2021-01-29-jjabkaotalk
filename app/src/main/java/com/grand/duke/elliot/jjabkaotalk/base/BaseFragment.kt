package com.grand.duke.elliot.jjabkaotalk.base

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

open class BaseFragment: Fragment() {

    private var menuRes: Int? = null
    private var onBackPressed: (() -> Unit)? = null
    private var onHomePressed: (() -> Unit)? = null
    private var optionsItemIdToOnSelected = mutableMapOf<Int, () -> Unit>()

    private var searchViewItemId: Int? = null
    private var onQueryTextListener: SearchView.OnQueryTextListener? = null

    protected fun setOnBackPressed(onBackPressed: () -> Unit) {
        this.onBackPressed = onBackPressed
        val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    this@BaseFragment.onBackPressed?.invoke()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                onBackPressedCallback
        )
    }

    protected fun setDisplayHomeAsUpEnabled(
            toolbar: Toolbar,
            @DrawableRes navigationIcon: Int? = null,
            onHomePressed: () -> Unit
    ) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationIcon?.let { toolbar.setNavigationIcon(it) }
        setHasOptionsMenu(true)
        this.onHomePressed = onHomePressed
    }

    protected fun showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(requireContext(), text, duration).show()
    }

    protected fun setOnOptionsMenu(toolbar: Toolbar, menuRes: Int, optionsItemIdToOnSelected: Array<Pair<Int, () -> Unit>>) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        this.menuRes = menuRes
        optionsItemIdToOnSelected.forEach {
            if (this.optionsItemIdToOnSelected.keys.notContains(it.first))
                this.optionsItemIdToOnSelected[it.first] = it.second
        }
    }

    protected fun setupSearchView(searchViewItemId: Int, onQueryTextListener: SearchView.OnQueryTextListener) {
        this.searchViewItemId = searchViewItemId
        this.onQueryTextListener = onQueryTextListener
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        menuRes?.let {
            inflater.inflate(it, menu)

            searchViewItemId?.let { searchViewItemId ->
                (menu.findItem(searchViewItemId).actionView as? SearchView)?.setOnQueryTextListener(onQueryTextListener)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> onHomePressed?.invoke()
            else -> optionsItemIdToOnSelected[item.itemId]?.invoke()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun MutableSet<Int>.notContains(element: Int) = !contains(element)
}