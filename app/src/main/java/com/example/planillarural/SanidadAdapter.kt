package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SanidadAdapter(
    private val sanidadList: List<Sanidad>,
    private val onLongClick: (Sanidad) -> Unit // ¡NUEVO! Callback para click largo
) : RecyclerView.Adapter<SanidadAdapter.SanidadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        SanidadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sanidad, parent, false)
        return SanidadViewHolder(view)
    }

    override fun onBindViewHolder(holder: SanidadViewHolder, position: Int) {
        val sanidad = sanidadList[position]
        holder.bind(sanidad, onLongClick)
    }

    override fun getItemCount() = sanidadList.size

    class SanidadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tratamientoTextView: TextView = itemView.findViewById(R.id.tvTratamiento)
        private val productoTextView: TextView = itemView.findViewById(R.id.tvProducto)
        private val fechaTextView: TextView = itemView.findViewById(R.id.tvFecha)
        private val fechaProximaDosisTextView: TextView = itemView.findViewById(R.id.tvFechaProximaDosis)

        fun bind(sanidad: Sanidad, onLongClick: (Sanidad) -> Unit) {
            tratamientoTextView.text = sanidad.tratamiento
            productoTextView.text = "Producto: ${sanidad.producto}"
            fechaTextView.text = "Fecha: ${sanidad.fecha}"

            if (sanidad.fechaProximaDosis.isNullOrEmpty()) {
                fechaProximaDosisTextView.visibility = View.GONE
            } else {
                fechaProximaDosisTextView.visibility = View.VISIBLE
                fechaProximaDosisTextView.text = "Próxima Dosis: ${sanidad.fechaProximaDosis}"
            }

            // Asignar el listener de click largo a toda la tarjeta
            itemView.setOnLongClickListener {
                onLongClick(sanidad)
                true
            }
        }
    }
}