package com.tsellami.pilot.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tsellami.pilot.databinding.MetarDataLayoutBinding

class MetarDataAdapter(private val metarData: List<String>): RecyclerView.Adapter<MetarDataAdapter.MetarDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetarDataAdapter.MetarDataViewHolder {
        val view = MetarDataLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MetarDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: MetarDataAdapter.MetarDataViewHolder, position: Int) {
        holder.bind(metarData[position])
    }

    override fun getItemCount(): Int = metarData.size

    inner class MetarDataViewHolder(private val binding: MetarDataLayoutBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            val data = item.split(":", limit = 2)
            binding.title.text = data[0]
            binding.data.text = data[1]
        }
    }
}