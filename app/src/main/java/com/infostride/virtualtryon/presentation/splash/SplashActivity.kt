package com.infostride.virtualtryon.presentation.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.presentation.select_option.GenderSelectionActivity
import com.infostride.virtualtryon.util.launchActivity

/****
 * Created by poonam Rani on 23 Jan 2023
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        moveToGenderSelectionPage()
    }

    private fun moveToGenderSelectionPage() {
        // Keep the splash screen visible for this Activity
        Handler(Looper.getMainLooper()).postDelayed({
            launchActivity<GenderSelectionActivity>()
            finish()
        }, 2000) // 2000 is the delayed time in milliseconds.
    }
}