package edu.vanderbilt.crawler.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Keyboard helpers.
 */

fun Activity.hideKeyboard() {
    contentView?.hideKeyboard(
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
}

fun View.hideKeyboard(inputMethodManager: InputMethodManager) {
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}
