package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SanidadAdapter(private val sanidadList: List<Sanidad>) : RecyclerView.Adapter<SanidadAdapter.SanidadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        SanidadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sanidad, parent, false)
        return SanidadViewHolder(view)
    }

    override fun onBindViewHolder(holder: SanidadViewHolder, position: Int) {
        val sanidad = sanidadList[position]
        holder.bind(sanidad)
    }

    override fun getItemCount() = sanidadList.size

    class SanidadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tratamientoTextView: TextView = itemView.findViewById(R.id.tvTratamiento)
        private val productoTextView: TextView = itemView.findViewById(R.id.tvProducto)
        private val fechaTextView: TextView = itemView.findViewById(R.id.tvFecha)

        fun bind(sanidad: Sanidad) {
            tratamientoTextView.text = sanidad.tratamiento
            productoTextView.text = sanidad.producto
            fechaTextView.text = sanidad.fecha
        }
    }
}