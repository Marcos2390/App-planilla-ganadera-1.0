package com.example.planillarural

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ResumenAnimalesAdapter(
    private val animales: List<Animal>
) : RecyclerView.Adapter<ResumenAnimalesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCaravana: TextView = itemView.findViewById(R.id.tvCaravanaResumen)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaResumen)
        val tvRaza: TextView = itemView.findViewById(R.id.tvRazaResumen)
        // El contenedor principal de la tarjeta para cambiarle el color
        val cardContainer: View = itemView 
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resumen_animal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = animales[position]
        holder.tvCaravana.text = animal.nombre
        holder.tvCategoria.text = animal.categoria
        holder.tvRaza.text = animal.raza

        // Lógica de colores
        val categoria = animal.categoria.lowercase()
        val color = when {
            categoria.endsWith("o") -> Color.parseColor("#E3F2FD") // Azul claro
            categoria.endsWith("a") -> Color.parseColor("#FCE4EC") // Rosa claro
            else -> Color.WHITE
        }
        
        // Asumimos que el root view del item es un CardView o un Layout al que se le puede poner color
        (holder.cardContainer as? androidx.cardview.widget.CardView)?.setCardBackgroundColor(color) ?: holder.cardContainer.setBackgroundColor(color)

    }

    override fun getItemCount() = animales.size
}