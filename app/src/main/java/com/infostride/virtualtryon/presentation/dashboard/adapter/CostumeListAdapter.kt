package com.infostride.virtualtryon.presentation.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.databinding.CostumerItemLayoutBinding
import com.infostride.virtualtryon.domain.model.CostumeDetails

class CostumeListAdapter(private val costumeList:ArrayList<CostumeDetails>,private val onItemClick:(CostumeDetails) -> Unit):
    RecyclerView.Adapter<CostumeListAdapter.CostumeViewListViewHolder>() {

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
            ivCostume.load(image) {
                crossfade(true)
                placeholder(R.mipmap.ic_launcher)
            }
            ivCostume.setOnClickListener { onItemClick(this) }
        }
       }
    }

 }

