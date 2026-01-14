package com.vasensio.bluetooth_list_recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(
    private val dataSet: MutableList<Dispositivo>,
    private val onItemClick: (Dispositivo) -> Unit
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvMac: TextView = view.findViewById(R.id.tvMac)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]

        viewHolder.tvTitulo.text = item.nombre
        viewHolder.tvMac.text = item.mac

        viewHolder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = dataSet.size
}