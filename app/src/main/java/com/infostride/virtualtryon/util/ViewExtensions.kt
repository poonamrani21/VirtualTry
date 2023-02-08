package com.infostride.virtualtryon.util

import android.content.Context
import android.view.View
import android.widget.Toast

/**
 * Shorthand [makeGone] extension function to make view gone
 */
fun View.makeGone() {
    this.visibility = View.GONE
}

/**
 * Shorthand [makeVisible] extension function to make view visible
 */
fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

/***
 * [showToast] with display the toast
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}