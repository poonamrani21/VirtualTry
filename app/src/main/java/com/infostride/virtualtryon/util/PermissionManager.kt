package com.infostride.virtualtryon.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// TODO: Need to update this file
class PermissionManager(activityContext:Activity) : AppCompatActivity() {

    private val REQUEST_CODE = 999
    private var activityContext: Activity? = null
    private var allPermissionsGranted = false
    private val permissionList = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    init { this.activityContext = activityContext }

    private fun checkPermissions(): Boolean {
        for (permissionObj in permissionList) {
            if (ContextCompat.checkSelfPermission(activityContext!!, permissionObj) !== PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false
                return false
            } else {
                allPermissionsGranted = true
            }
        } //end for loop
        return allPermissionsGranted
    } //end checkPermissions


    fun requestPerms() {
        if (checkPermissions()) return else {
            ActivityCompat.requestPermissions(activityContext!!, permissionList, REQUEST_CODE)
        }
    } //end requestPerms

}//end class
