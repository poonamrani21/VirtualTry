package com.infostride.virtualtryon.presentation.dashboard

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.databinding.ActivityApplyBinding
import com.infostride.virtualtryon.domain.model.CostumeDetails
import com.infostride.virtualtryon.domain.model.CostumeType
import com.infostride.virtualtryon.presentation.ui.DrawView
import com.infostride.virtualtryon.presentation.ui.Outfit
import com.infostride.virtualtryon.presentation.ui.PreviewCamera
import com.infostride.virtualtryon.util.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader


/****
 * Created by poonam Rani on 23 Jan 2023
 */
class ApplyCostumeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplyBinding
    private lateinit var activity: Activity
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
        binding = ActivityApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val list = listOf(1, 2, 5)
        val numbers = listOf(1, 2, 3)
        for (num in numbers) {
            println(num)
        }
        val words: List<String> = listOf("jump", "run", "skip")
        val oddNumbers = mutableListOf("1", "9", "15")
        activity = this
        activity = this
       /* if (savedInstanceState == null) {
            fragmentManager
                .beginTransaction()
                .replace(R.id.FrameContainer_fit_preview, PreviewCamera.newInstance())
                .commit()
        }*/
        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.FrameContainer_fit_preview, PreviewCamera())
            .commit()
        costumeList = ArrayList()
        intent?.also {
            genderType = intent.getStringExtra(Constant.GENDER_TYPE).toString()
            showCostumeList()
        }
    }

    override fun onResume()
    {
        super.onResume();
        if(OpenCVLoader.initDebug())
        {
            Log.i("Test Model -- ", "OpenCV initialize success");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.i("Test Model -- ", "OpenCV initialize failed")
        }
    } //End onResume



    /***
     * Show costume list as per selected gender
     */
    private fun showCostumeList() {
        val costumeTypeList = listOfCostumesType(genderType)
        categoryName = costumeTypeList[0].type
        listOfCostumes(costumeType, genderType)
         DrawView.currentOutfit = Outfit(costumeList[0].id, costumeList[0].category, costumeList[0].image)

    }

    private fun applyCostumeToModel() { showToast("Costume Applied") }

    /***
     * [updateCostumeList] will update
     */
    private fun updateCostumeList() {
        costumeList.clear()
        listOfCostumes(costumeType, genderType)
        showToast("List updated: Costume type is: $costumeType and costume list size is: ${costumeList.size}")
    }

    /***
     * [listOfCostumesType]
     */
    private fun listOfCostumesType(genderType: String?): List<CostumeType> {
        val costumeTypeList = ArrayList<CostumeType>()
        when (genderType) {
            Constant.men -> {
                AppLog.loge(true, kotlinFileName, "normalizeRating", categoryName, Exception())
                costumeTypeList.add(CostumeType(1, CostumeTypes.MEN_TROUSERS.dress))
                costumeTypeList.add(CostumeType(2, CostumeTypes.MEN_SHIRTS.dress))
                costumeTypeList.add(CostumeType(3, CostumeTypes.MEN_JEANS.dress))
            }
            Constant.women -> {
                costumeTypeList.add(CostumeType(1, CostumeTypes.WOMEN_TOPS.dress))
                costumeTypeList.add(CostumeType(2, CostumeTypes.WOMEN_LONG_WEAR.dress))
                costumeTypeList.add(CostumeType(3, CostumeTypes.WOMEN_TROUSERS.dress))
                costumeTypeList.add(CostumeType(4, CostumeTypes.WOMEN_SHORTS.dress))
            }
        }
        return costumeTypeList
    }

    /**
     * [listOfCostumes]
     */
    private fun listOfCostumes(costumeType: Int, genderType: String?): ArrayList<CostumeDetails> {
        when (genderType) {
            Constant.men -> {
                when (costumeType) {
                    Constant.men_shirts -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress1,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                    }
                    Constant.men_jeans -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress1,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress4,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress5,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                    }
                    Constant.men_trousers ->  {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress1,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress2,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress6,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                    }
                }
            }
            Constant.women -> {
                when (costumeType) {
                    Constant.women_top -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress1,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress4,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress6,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                    }
                    Constant.women_long_wears -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress6,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress4,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress1,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                    }
                    Constant.women_trousers -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress4,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress1,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress5,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress6,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                    }
                    Constant.women_shorts_n_skirts -> {
                        costumeList.add(CostumeDetails(1, costumeType, R.drawable.women_dress5,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress5)))
                        costumeList.add(CostumeDetails(2, costumeType, R.drawable.women_dress2,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress2)))
                        costumeList.add(CostumeDetails(3, costumeType, R.drawable.women_dress3,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress3)))
                        costumeList.add(CostumeDetails(4, costumeType, R.drawable.women_dress4,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress4)))
                        costumeList.add(CostumeDetails(5, costumeType, R.drawable.women_dress1,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(6, costumeType, R.drawable.women_dress6,
                            getCategoryName(categoryName!!),convertImageToByteArray(R.drawable.women_dress6)))
                    }
                }
            }
        }
        return costumeList
    }

}