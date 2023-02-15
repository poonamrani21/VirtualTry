package com.infostride.virtualtryon.presentation.select_option

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.databinding.ActivityGenderSelectionBinding
import com.infostride.virtualtryon.presentation.dashboard.MainActivity
import com.infostride.virtualtryon.util.Constant.GENDER_TYPE
import com.infostride.virtualtryon.util.Constant.men
import com.infostride.virtualtryon.util.Constant.women
import com.infostride.virtualtryon.util.launchActivity
import com.infostride.virtualtryon.util.showSnackbar

class GenderSelectionActivity : AppCompatActivity() {
    /****
     * Created by poonam Rani on 23 Jan 2023
     */
    private lateinit var binding: ActivityGenderSelectionBinding
    private lateinit var layout: View
    private val permissionArray= arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val requestPermissionLauncher  = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result -> Log.d("TAG", ": $result") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenderSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        layout = binding.mainLayout
        onClickListeners()
        onClickRequestPermission()
    }

    /***
     * Check required permission
     */
  private  fun onClickRequestPermission() {
        permissionArray.forEach { permissionArrayObj->
            when {
                ContextCompat.checkSelfPermission(this, permissionArrayObj) == PackageManager.PERMISSION_GRANTED -> { layout.showSnackbar(view = layout, getString(R.string.permission_granted), Snackbar.LENGTH_SHORT, null) {} }
                ActivityCompat.shouldShowRequestPermissionRationale(this, permissionArrayObj) -> { layout.showSnackbar(view = layout, permissionArrayObj + getString(R.string.permission_required), Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) { requestPermissionLauncher.launch(permissionArray) } }
                else -> { requestPermissionLauncher.launch(permissionArray) }
            }
        }
    }
    // Views click event
    private fun onClickListeners() {
        binding.ivMale.setOnClickListener { launchActivity<MainActivity> { putExtra(GENDER_TYPE, men) } }
        binding.ivFemale.setOnClickListener { launchActivity<MainActivity> { putExtra(GENDER_TYPE, women) } }
    }
}