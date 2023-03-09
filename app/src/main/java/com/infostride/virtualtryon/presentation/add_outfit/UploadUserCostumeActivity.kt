package com.infostride.virtualtryon.presentation.add_outfit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.databinding.ActivityUploadUserOutfitBinding
import com.infostride.virtualtryon.domain.model.Outfit
import com.infostride.virtualtryon.presentation.ui.DrawView
import com.infostride.virtualtryon.util.*
import java.io.ByteArrayOutputStream
import java.io.IOException

/***
 * Created by poonam on 23 Jan 2023
 */
class UploadUserCostumeActivity : AppCompatActivity() {

    private lateinit var binding:ActivityUploadUserOutfitBinding

    /***
     * [selectedBmp] will use to store user selected image bitmap
     */
    private var selectedBmp: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding initialization
        binding = ActivityUploadUserOutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //set seekbar values
        setSeekbarValues()
        // set click listeners on views
        onClickListeners()

    }
    private fun setSeekbarValues() {
        binding.sensitivityBarAddOutfit.max = 155
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.sensitivityBarAddOutfit.min = 0
        }
    }

    private fun onClickListeners() {
        binding.buttonSearchGalleryAddOutfit.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncher.launch(intent)
        }
        binding.buttonInsertAddOutfit.setOnClickListener{
            val popupMenu = PopupMenu(this,  binding.buttonInsertAddOutfit)
            popupMenu.menuInflater.inflate(R.menu.menu_outfit_categories, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                var category = ""
                val title = item.title.toString()
                if (title == "Tops" || title =="Shirts") {
                    category = "top"
                }
                if (title == "Long Wears") {
                    category = "long_wears"
                }
                if (title == "Trousers") {
                    category = "trousers"
                }
                if (title == "Shorts") {
                    category = "shorts_n_skirts"
                }
                if ((binding.imViewAddOutfit.drawable as BitmapDrawable).bitmap.height>10&&(binding.imViewAddOutfit.drawable as BitmapDrawable).bitmap.width>1){
                    val bmp = (binding.imViewAddOutfit.drawable as BitmapDrawable).bitmap
                    val stream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val imgByte = stream.toByteArray()
                    DrawView.currentOutfit = Outfit( category = category, image = imgByte)
                    showToast(getString(R.string.costume_applied))
                    finish()
                }else showToast(getString(R.string.unable_apply_outfit))
                false
            } //end setOnMenuItemClickListener
            popupMenu.gravity = Gravity.CENTER
            popupMenu.show()
            popupMenu.setOnDismissListener { binding.sensitivityBarAddOutfit.makeVisible() }
            binding.sensitivityBarAddOutfit.makeGone()
        }
        binding.sensitivityBarAddOutfit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Log.d("onProgressChanged: ", progress.toString())
                val processor = ImageProcessor()
                val processedBmp: Bitmap = processor.extractOutfit(selectedBmp!!, progress)
                binding.imViewAddOutfit.setImageBitmap(processedBmp)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                Log.d(kotlinFileName, "onStartTrackingTouch: ")}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Log.d(kotlinFileName, "onStopTrackingTouch: ")}
        })

    }

    /****
     * [resultLauncher] will launch Gallery Activity  here user will get user pick image from gallery
     */
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if ( (result.resultCode == Activity.RESULT_OK)) {

            val selectedImage = result.data?.data
            if (result.data==null) return@registerForActivityResult
            else
            {
            try {
               val bitmap :Bitmap? = when {
                    Build.VERSION.SDK_INT < 28 -> { MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage) }
                    else -> {
                        val source = ImageDecoder.createSource(contentResolver, selectedImage!!)
                        ImageDecoder.decodeBitmap(source)
                    }
                }
                selectedBmp = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
                binding.imViewAddOutfit.setImageBitmap(bitmap)
                binding.buttonSearchGalleryAddOutfit.makeGone()
                binding.buttonInsertAddOutfit.makeVisible()
                binding.sensitivityBarAddOutfit.makeVisible()
                binding.sensitivityBarAddOutfit.makeVisible()
            } catch (e: IOException) {
                Log.d(kotlinFileName, "IO exception $e")
            }}
        }
    }
}
