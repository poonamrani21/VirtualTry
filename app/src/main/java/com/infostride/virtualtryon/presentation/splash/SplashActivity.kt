package com.infostride.virtualtryon.presentation.splash

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.animation.AnticipateInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.presentation.select_option.GenderSelectionActivity
import com.infostride.virtualtryon.util.launchActivity
import androidx.core.splashscreen.SplashScreenViewProvider
class SplashActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_splash)
        //setSplashExitAnimation(splashScreen)
        window.insetsController!!.hide(WindowInsets.Type.statusBars())
        launchActivity<GenderSelectionActivity>()
      //  moveToGenderSelectionPage()
    }

    private fun moveToGenderSelectionPage() {
        // Keep the splash screen visible for this Activity
        Handler(Looper.getMainLooper()).postDelayed({
            launchActivity<GenderSelectionActivity>()
            finish()
        }, 2000) // 2000 is the delayed time in milliseconds.
    }
}