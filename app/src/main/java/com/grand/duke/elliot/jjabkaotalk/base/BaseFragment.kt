package com.grand.duke.elliot.jjabkaotalk.base

import android.widget.Toast
import androidx.fragment.app.Fragment

open class BaseFragment: Fragment() {

    protected fun showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(requireContext(), text, duration).show()
    }
}