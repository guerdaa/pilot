package com.tsellami.pilot.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.databinding.AirportLayoutBinding

class FavoriteAdapter(private val listener: OnItemListener) : ListAdapter<Airport, FavoriteAdapter.AirportViewHolder>(DiffComparator()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AirportViewHolder {
        val binding = AirportLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AirportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AirportViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class AirportViewHolder(private val binding: AirportLayoutBinding)
        : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION)
                    listener.onClick(getItem(position))
            }
        }

        fun bind(airport: Airport) {
            binding.airportIcao.text = airport.icao
            binding.airportName.text = airport.name
        }
    }

    class DiffComparator: DiffUtil.ItemCallback<Airport>() {
        override fun areItemsTheSame(oldItem: Airport, newItem: Airport): Boolean =
            oldItem.icao == newItem.icao


        override fun areContentsTheSame(oldItem: Airport, newItem: Airport): Boolean =
            oldItem == newItem

    }

    interface OnItemListener {
        fun onClick(airport: Airport)
    }

}