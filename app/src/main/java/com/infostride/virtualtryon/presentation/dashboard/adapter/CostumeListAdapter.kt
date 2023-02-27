package com.infostride.virtualtryon.presentation.dashboard.adapter

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.databinding.CostumerItemLayoutBinding
import com.infostride.virtualtryon.domain.model.CostumeDetails
import com.infostride.virtualtryon.util.ImageProcessor

/****
 * Created by poonam Rani on 23 Jan 2023
 */
class CostumeListAdapter(private val costumeList:ArrayList<CostumeDetails>,private val onItemClick:(CostumeDetails,Bitmap) -> Unit): RecyclerView.Adapter<CostumeListAdapter.CostumeViewListViewHolder>() {

    inner class CostumeViewListViewHolder(val binding: CostumerItemLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CostumeViewListViewHolder {
        return  CostumeViewListViewHolder(CostumerItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }

    override fun getItemCount(): Int {
      return   costumeList.size
    }

    override fun onBindViewHolder(holder: CostumeViewListViewHolder, position: Int) {
       with(holder.binding){
        with(costumeList[position]){
            val processor = ImageProcessor()
            val processedBmp = processor.extractOutfit(image, 15)
            ivCostume.load(image) {
                crossfade(true)
                placeholder(R.mipmap.ic_launcher)
            }
            ivCostume.setOnClickListener { onItemClick(this,processedBmp) }
        }
       }
    }

 }

