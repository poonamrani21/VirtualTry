package com.infostride.virtualtryon.presentation.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.infostride.virtualtryon.databinding.CostumerOptionItemLayoutBinding
import com.infostride.virtualtryon.domain.model.CostumeType


/****
 * Created by poonam Rani on 23 Jan 2023
 */
class CostumeTypeAdapter(private val listOfCostumeType: List<CostumeType>,private val onItemClick:(CostumeType) -> Unit):RecyclerView.Adapter<CostumeTypeAdapter.CostumerTypeViewHolder>() {

     inner class  CostumerTypeViewHolder(val binding: CostumerOptionItemLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CostumerTypeViewHolder {
       return  CostumerTypeViewHolder(CostumerOptionItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int { return  listOfCostumeType.size }

    override fun onBindViewHolder(holder: CostumerTypeViewHolder, position: Int) {
        //Set data over views
       with( holder.binding){
           with(listOfCostumeType[position]){
               tvCostumeOption.text=type
               tvCostumeOption.setOnClickListener {  onItemClick(this)}
           }
       }
    }
}
