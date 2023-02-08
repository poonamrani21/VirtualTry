package com.infostride.virtualtryon.presentation.select_option

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infostride.virtualtryon.databinding.ActivityGenderSelectionBinding
import com.infostride.virtualtryon.presentation.dashboard.MainActivity
import com.infostride.virtualtryon.util.Constant.GENDER_TYPE
import com.infostride.virtualtryon.util.Constant.men
import com.infostride.virtualtryon.util.Constant.women
import com.infostride.virtualtryon.util.PermissionManager
import com.infostride.virtualtryon.util.launchActivity

class GenderSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenderSelectionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenderSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Check camera and media permissions
        val permissionManager = PermissionManager(this)
        permissionManager.requestPerms()
        onClickListeners()
    }

    // Views click event
    private fun onClickListeners() {
        binding.ivMale.setOnClickListener { launchActivity<MainActivity> { putExtra(GENDER_TYPE, men) } }
        binding.ivFemale.setOnClickListener { launchActivity<MainActivity> { putExtra(GENDER_TYPE, women) } }
    }
}