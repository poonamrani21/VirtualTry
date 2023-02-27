package com.infostride.virtualtryon.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

/****
 * Created by poonam on 23 Jan 2023
 */


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
fun Context.showToast(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

/*fun Context.convertImageToByteArray(image: Int): ByteArray {
    val largeIcon: Bitmap = BitmapFactory.decodeResource(resources, image)
    val stream = ByteArrayOutputStream()
    val processedBmp =  ImageProcessor().extractOutfit(largeIcon, 20, background_color)
    processedBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}*/
//convertImageToBitmap
fun Context.convertDrawableToBitmap(image: Int): Bitmap {
    val largeIcon: Bitmap = BitmapFactory.decodeResource(resources, image)
    val stream = ByteArrayOutputStream()
    val processedBmp =  ImageProcessor().extractOutfit(largeIcon, 3)
    processedBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return largeIcon
}
/*fun Context.convertImageToByteArray(image: Int): ByteArray {
    val res: Resources = resources
    val drawable: Drawable = res.getDrawable(image)
    val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
    val stream = ByteArrayOutputStream()
    val processedBmp =  ImageProcessor().extractOutfit(bitmap, 15, background_color)
    processedBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}*/
fun getCategoryName(categoryName: String, genderType: String): String {
    var category:String?=null
    if (genderType== Constant.men){
        if (categoryName == "Shirts")  category = "top"
        if (categoryName == "Long Wears")  category = "long_wears"
        if (categoryName == "Trousers")  category = "trousers"
        if (categoryName == "Shorts")  category = "shorts_n_skirts"
    }else{
        if (categoryName == "Tops")  category = "top"
        if (categoryName == "Long Wears")  category = "long_wears"
        if (categoryName == "Trousers")  category = "trousers"
        if (categoryName == "Shorts and Skirts")  category = "shorts_n_skirts"
    }

    return category!!

}