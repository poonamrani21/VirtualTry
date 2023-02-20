package com.infostride.virtualtryon.presentation.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.databinding.ActivityMainBinding
import com.infostride.virtualtryon.domain.model.CostumeDetails
import com.infostride.virtualtryon.domain.model.CostumeType
import com.infostride.virtualtryon.presentation.dashboard.adapter.CostumeListAdapter
import com.infostride.virtualtryon.presentation.dashboard.adapter.CostumeTypeAdapter
import com.infostride.virtualtryon.presentation.ui.DrawView
import com.infostride.virtualtryon.presentation.ui.Outfit
import com.infostride.virtualtryon.presentation.ui.PreviewCamera
import com.infostride.virtualtryon.util.*
import com.infostride.virtualtryon.util.AppLog.loge
import com.infostride.virtualtryon.util.Constant.men
import com.infostride.virtualtryon.util.Constant.men_jeans
import com.infostride.virtualtryon.util.Constant.men_shirts
import com.infostride.virtualtryon.util.Constant.men_trousers
import com.infostride.virtualtryon.util.Constant.women
import com.infostride.virtualtryon.util.Constant.women_long_wears
import com.infostride.virtualtryon.util.Constant.women_shorts_n_skirts
import com.infostride.virtualtryon.util.Constant.women_top
import com.infostride.virtualtryon.util.Constant.women_trousers
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

/****
 * Created by poonam Rani on 23 Jan 2023
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var costumeListAdapter: CostumeListAdapter
    private lateinit var costumeTypeAdapter: CostumeTypeAdapter
    private lateinit var costumeList: ArrayList<CostumeDetails>
    private var costumeType = 1//for type
    private lateinit var genderType: String
    private  var categoryName:String?=null

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                INCOMPATIBLE_MANAGER_VERSION -> return
                INIT_FAILED -> return
                INSTALL_CANCELED -> return
                MARKET_ERROR -> return
                else -> super.onManagerConnected(status)
            }
        }
    } //End mLoaderCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Replace CameraPreview fragment
        savedInstanceState ?: supportFragmentManager.beginTransaction().replace(R.id.FrameContainer_fit_preview, PreviewCamera()).commit()
        costumeList = ArrayList()
        intent?.also {
            genderType = intent.getStringExtra(Constant.GENDER_TYPE).toString()
            showCostumeList()
        }
        showHideViewOverCameraView()
        setOnClickListeners()
    }

    override fun onResume() {
            super.onResume()
            if(OpenCVLoader.initDebug())
            {
                Log.i("Test Model -- ", "OpenCV initialize success")
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
            }
            else Log.i("Test Model -- ", "OpenCV initialize failed")
        } //End onResume

    @SuppressLint("ClickableViewAccessibility")
    private fun showHideViewOverCameraView() {
        binding.outsideDetector.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {MotionEvent.ACTION_DOWN -> {
                if(binding.rvCostume.visibility == View.VISIBLE)
                {
                    showToast("Full Camera view will visible here")
                    binding.rvCostume.visibility = View.INVISIBLE
                    binding.llSelectOutfit.visibility = View.INVISIBLE
                } else {
                    showToast("Full Camera  view hidden")
                    binding.llSelectOutfit.visibility = View.VISIBLE
                    binding.rvCostume.visibility = View.VISIBLE
                } }
            }
            true
        }
    }

    /***
     * [setOnClickListeners] for on click events
     */
    private fun setOnClickListeners(){
        binding.btnUploadOutfit.setOnClickListener {
        showToast("Open Gallery here to select outfit")
    }
    }

    /***
     * Show costume list as per selected gender
     */
    private fun showCostumeList() {
        val costumeTypeList = listOfCostumesType(genderType)
        categoryName = costumeTypeList[0].type
        listOfCostumes(costumeType, genderType)
        costumeTypeAdapter = CostumeTypeAdapter(costumeTypeList) {
            if (costumeType != it.id) {
                costumeType = it.id
                categoryName = it.type
                updateCostumeList()
            }
        }
        binding.rvCostumeType.adapter = costumeTypeAdapter
        DrawView.currentOutfit = Outfit(costumeList[0].id, costumeList[0].category, costumeList[0].image)
        costumeListAdapter = CostumeListAdapter(costumeList) {
            DrawView.currentOutfit = Outfit(id = it.id, category = it.category, image = it.image)
            applyCostumeToModel()
        }
        binding.rvCostume.adapter = costumeListAdapter
    }

    private fun applyCostumeToModel() { showToast("Costume Applied") }

    /***
     * [updateCostumeList] will update
     */
    private fun updateCostumeList() {
        costumeList.clear()
        listOfCostumes(costumeType, genderType)
        costumeListAdapter.notifyDataSetChanged()
        showToast("List updated: Costume type is: $costumeType and costume list size is: ${costumeList.size}")
    }

    /***
     * [listOfCostumesType] will get types of costume as per [genderType] -> 1 for [women] and 2 -> for [men]
     */
    private fun listOfCostumesType(genderType: String?): List<CostumeType> {
        val costumeTypeList = ArrayList<CostumeType>()
        when (genderType) {
            men -> {
                loge(true, kotlinFileName, "normalizeRating", categoryName, Exception())
                costumeTypeList.add(CostumeType(1, CostumeTypes.MEN_TROUSERS.dress))
                costumeTypeList.add(CostumeType(2, CostumeTypes.MEN_SHIRTS.dress))
                costumeTypeList.add(CostumeType(3, CostumeTypes.MEN_JEANS.dress))
            }
            women -> {
                costumeTypeList.add(CostumeType(1, CostumeTypes.WOMEN_TOPS.dress))
                costumeTypeList.add(CostumeType(2, CostumeTypes.WOMEN_LONG_WEAR.dress))
                costumeTypeList.add(CostumeType(3, CostumeTypes.WOMEN_TROUSERS.dress))
                costumeTypeList.add(CostumeType(4, CostumeTypes.WOMEN_SHORTS.dress))
            }
        }
        return costumeTypeList
    }

    /**
     * [listOfCostumes] will get list of costume as per [costumeType] and [genderType]
     * [costumeType] -> will get types from [CostumeTypes]
     * [genderType] -> 1 for [women] and 2 -> for [men]
     */
    private fun listOfCostumes(costumeType: Int, genderType: String?): ArrayList<CostumeDetails> {
        when (genderType) {
            men -> {
                when (costumeType) {
                    men_shirts -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress1,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                    }
                    men_jeans -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress1,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress4,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress5,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                    }
                    men_trousers ->  {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress1,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress2,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress6,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                    }
                }
            }
            women -> {
                when (costumeType) {
                    women_top -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress1,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress4,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress6,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                    }
                    women_long_wears -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress6,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress4,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress1,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                    }
                    women_trousers -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress4,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress1,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress6,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                    }
                    women_shorts_n_skirts -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress5,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress4,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress1,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress6,getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                    }
                }
            }
        }
        return costumeList
    }

}