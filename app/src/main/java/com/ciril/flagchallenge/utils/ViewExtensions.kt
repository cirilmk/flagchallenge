package com.ciril.flagchallenge.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard(view: View) {
    val imm = getSystemService(InputMethodManager::class.java)
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}