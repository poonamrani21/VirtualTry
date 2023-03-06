package com.infostride.virtualtryon.presentation.select_option

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.databinding.ActivityGenderSelectionBinding
import com.infostride.virtualtryon.presentation.dashboard.MainActivity
import com.infostride.virtualtryon.util.*
import com.infostride.virtualtryon.util.Constant.BACK_CAMERA
import com.infostride.virtualtryon.util.Constant.CAMERA_TYPE
import com.infostride.virtualtryon.util.Constant.FRONT_CAMERA
import com.infostride.virtualtryon.util.Constant.GENDER_TYPE
import com.infostride.virtualtryon.util.Constant.men
import com.infostride.virtualtryon.util.Constant.women

/****
 * Created by poonam on 23 Jan 2023
 */
class GenderSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenderSelectionBinding
    private lateinit var layout: View
    private val permissionArray= arrayOf(Manifest.permission.CAMERA/*,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE*/)
    private var isPermissionsGiven=false
    private val requestPermissionLauncher  = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            result -> Log.d(kotlinFileName, "result : $result")
            result.forEach { (_, isPermissionGive) ->
                if (!isPermissionGive){
                    isPermissionsGiven=isPermissionGive
                }
            }
    }
    private var cameraType= BACK_CAMERA
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenderSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        layout = binding.mainLayout
        onClickListeners()
        onClickRequestPermission()
    }

    /***
     * [onClickRequestPermission] will check required permission
     */
  private  fun onClickRequestPermission() {
        permissionArray.forEach { permissionArrayObj->
            when {
                ContextCompat.checkSelfPermission(this, permissionArrayObj) == PackageManager.PERMISSION_GRANTED -> {
                    isPermissionsGiven=true }
                ActivityCompat.shouldShowRequestPermissionRationale(this, permissionArrayObj) -> {
                    layout.showSnackbar(view = layout, "$permissionArrayObj ${getString(R.string.permission_required)}", Snackbar.LENGTH_INDEFINITE, getString(R.string.ok))
                    { requestPermissionLauncher.launch(permissionArray) } }
                else -> { requestPermissionLauncher.launch(permissionArray) }
            }
        }
    }

    /***
     * [onClickListeners] use for view click events
     */
    private fun onClickListeners() {
        binding.ivMale.setOnClickListener { goToMainActivity(men) }
        binding.ivFemale.setOnClickListener { goToMainActivity(women) }
        binding.ivSettings.setOnClickListener { openSettingMenu() }
    }

    /**
     * [goToMainActivity]
     */
    private fun goToMainActivity(genderType: String) {
        /*if (isPermissionsGiven) {
            launchActivity<MainActivity> {
            putExtra(GENDER_TYPE, genderType)
            putExtra(CAMERA_TYPE, cameraType)
        }
        }
        else showToast(getString(R.string.provide_camera_permission))*/
        launchActivity<MainActivity> {
            putExtra(GENDER_TYPE, genderType)
            putExtra(CAMERA_TYPE, cameraType)
        }
  }

    /***
     * [openSettingMenu] will  show setting icon options menu
     */
    private fun openSettingMenu() {
            val popupMenu = PopupMenu(this, binding.ivSettings)
            popupMenu.menuInflater.inflate(R.menu.menu_settings, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem -> cameraType = if (menuItem.equals(R.string.front_camera)) FRONT_CAMERA else BACK_CAMERA
                false } //end setOnMenuItemClickListener
            popupMenu.gravity = Gravity.CENTER
            popupMenu.show()
    }

    /***
     * [requestPermissionLauncher] launcher will unregister in case of activity destroy
     */
    override fun onDestroy() {
        super.onDestroy()
        requestPermissionLauncher.unregister()
    }
}