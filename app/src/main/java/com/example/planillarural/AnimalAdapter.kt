package com.example.planillarural

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnimalAdapter(
    private val animales: List<Animal>,
    private val onAnimalClickListener: (Animal) -> Unit,      // Para Editar
    private val onAnimalLongClickListener: (Animal) -> Unit   // Para Eliminar
) : RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder>() {

    class AnimalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.tvNombreAnimal)
        val razaTextView: TextView = itemView.findViewById(R.id.tvRazaAnimal)
        val categoriaTextView: TextView = itemView.findViewById(R.id.tvCategoriaAnimal)
        val edadTextView: TextView = itemView.findViewById(R.id.tvEdadAnimal)
        val infoAdicionalTextView: TextView = itemView.findViewById(R.id.tvInformacionAdicionalAnimal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_animal, parent, false)
        return AnimalViewHolder(view)
    }

    override fun getItemCount(): Int {
        return animales.size
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val animalActual = animales[position]
        holder.nombreTextView.text = "Caravana: ${animalActual.nombre}"
        holder.razaTextView.text = "Raza: ${animalActual.raza}"
        holder.categoriaTextView.text = "Categoría: ${animalActual.categoria}"
        holder.edadTextView.text = "Nacimiento: ${animalActual.fechaNac}"

        if (animalActual.informacionAdicional.isNullOrEmpty()) {
            holder.infoAdicionalTextView.visibility = View.GONE
        } else {
            holder.infoAdicionalTextView.visibility = View.VISIBLE
            holder.infoAdicionalTextView.text = animalActual.informacionAdicional
        }

        // ¡NUEVO! Aplicar el color de fondo
        try {
            if (!animalActual.color.isNullOrEmpty()) {
                // Si el color tiene un código válido, lo aplicamos
                holder.itemView.setBackgroundColor(Color.parseColor(animalActual.color))
            } else {
                // Si no tiene color, volvemos al blanco (o al color por defecto del drawable)
                holder.itemView.setBackgroundColor(Color.WHITE)
            }
        } catch (e: IllegalArgumentException) {
            // Si el código de color es inválido, usamos blanco por seguridad
            holder.itemView.setBackgroundColor(Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            onAnimalClickListener(animalActual)
        }

        holder.itemView.setOnLongClickListener {
            onAnimalLongClickListener(animalActual)
            true
        }
    }
}