package com.example.planillarural

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
        val infoAdicionalTextView: TextView = itemView.findViewById(R.id.tvInformacionAdicionalAnimal) // ¡NUEVO!
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

        // Lógica para mostrar/ocultar la información adicional (¡NUEVO!)
        if (animalActual.informacionAdicional.isNullOrEmpty()) {
            holder.infoAdicionalTextView.visibility = View.GONE
        } else {
            holder.infoAdicionalTextView.visibility = View.VISIBLE
            holder.infoAdicionalTextView.text = animalActual.informacionAdicional
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