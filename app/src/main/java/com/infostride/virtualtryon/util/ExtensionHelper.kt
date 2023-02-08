package com.infostride.virtualtryon.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

/***
 * Created by @author Poonam Rani by 23 Jan 2023
 */


/**
 * Extension for smarter launching of Activities
 */
inline fun <reified T : Activity> Context.launchActivity(
    noinline modify: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    intent.modify()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
/**
 * Extension to get the file name
 */
inline val <T : Any> T.kotlinFileName: String
    get() = javaClass.simpleName + ".kt"



