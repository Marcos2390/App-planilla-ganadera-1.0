package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PotreroAdapter(
    private val potreros: List<Potrero>,
    private val onPotreroClick: (Potrero) -> Unit,
    private val onLongClick: (Potrero) -> Unit
) : RecyclerView.Adapter<PotreroAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombrePotrero)
        val tvHectareas: TextView = view.findViewById(R.id.tvHectareas)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionPotrero)
        val tvResumen: TextView = view.findViewById(R.id.tvResumenAnimales)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_potrero, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val potrero = potreros[position]

        holder.tvNombre.text = potrero.nombre
        holder.tvHectareas.text = "${potrero.hectareas} Ha"
        holder.tvDescripcion.text = potrero.descripcion.ifEmpty { "Sin descripción" }
        
        // En Fase 1, mostramos un texto genérico. En Fase 2, calcularemos el total real.
        holder.tvResumen.text = "Toca para ver animales"

        holder.itemView.setOnClickListener { onPotreroClick(potrero) }
        holder.itemView.setOnLongClickListener { 
            onLongClick(potrero)
            true 
        }
    }

    override fun getItemCount() = potreros.size
}