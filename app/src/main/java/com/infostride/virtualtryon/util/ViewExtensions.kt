package com.infostride.virtualtryon.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

/**
 * Shorthand [makeGone] extension function to make view gone
 */
fun View.makeGone() { this.visibility = View.GONE }

/**
 * Shorthand [makeVisible] extension function to make view visible
 */
fun View.makeVisible() { this.visibility = View.VISIBLE }

/***
 * Shorthand [showSnackbar] extension to show message with "Snackbar"
 */
fun View.showSnackbar(view: View, msg: String, length: Int, actionMessage: CharSequence?, action: (View) -> Unit) {
    val snackbarObj = Snackbar.make(view, msg, length)
    if (actionMessage != null) { snackbarObj.setAction(actionMessage) {action(this) }.show() } else { snackbarObj.show() }
}
/***
 * [showToast] with display the toast
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.convertImageToByteArray(image: Int): ByteArray {
    val res: Resources = resources
    val drawable: Drawable = res.getDrawable(image)
    val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()

}
fun getCategoryName(categoryName: String): String {
    var category:String?=null
    if (categoryName == "Tops")  category = "top"
    if (categoryName == "Long Wear")  category = "long_wears"
    if (categoryName == "Trousers")  category = "trousers"
    if (categoryName == "Shorts")  category = "shorts_n_skirts"
    return category!!

}