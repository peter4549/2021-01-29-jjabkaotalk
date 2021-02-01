package com.grand.duke.elliot.jjabkaotalk.base

import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

open class BaseActivity: AppCompatActivity() {

    private var menuRes: Int? = null
    private var onHomePressed: (() -> Unit)? = null
    private var optionsItemIdToOnSelected = mutableMapOf<Int, () -> Unit>()
    private var showHomeAsUpEnabled = false

    protected fun setDisplayHomeAsUpEnabled(toolbar: Toolbar, onHomePressed: () -> Unit) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.onHomePressed = onHomePressed
        showHomeAsUpEnabled = true
    }

    protected fun setOnOptionsMenu(menuRes: Int, optionsItemIdToOnSelected: Array<Pair<Int, () -> Unit>>) {
        this.menuRes = menuRes

        optionsItemIdToOnSelected.forEach {
            if (this.optionsItemIdToOnSelected.keys.notContains(it.first))
                this.optionsItemIdToOnSelected[it.first] = it.second
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuRes?.let { menuInflater.inflate(it, menu) }
        return menuRes != null || showHomeAsUpEnabled
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onHomePressed?.invoke()
                true
            }
            else -> {
                optionsItemIdToOnSelected[item.itemId]?.invoke()
                true
            }
        }
    }

    protected fun showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, text, duration).show()
    }

    private fun MutableSet<Int>.notContains(element: Int) = !contains(element)
}