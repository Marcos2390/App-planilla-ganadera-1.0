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
        // Referencias a TODOS los campos del nuevo diseño
        val nombreTextView: TextView = itemView.findViewById(R.id.tvNombreAnimal)
        val categoriaTextView: TextView = itemView.findViewById(R.id.tvCategoriaAnimal)
        val razaTextView: TextView = itemView.findViewById(R.id.tvRazaAnimal)
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
        
        // Asignar los datos a las vistas (FORMATO CORREGIDO)
        holder.nombreTextView.text = animalActual.nombre
        holder.categoriaTextView.text = "Categoría: ${animalActual.categoria}"
        holder.razaTextView.text = "Raza: ${animalActual.raza}"
        holder.edadTextView.text = "Nac: ${animalActual.fechaNac}"

        // Lógica para mostrar/ocultar la información adicional
        if (animalActual.informacionAdicional.isNullOrEmpty()) {
            holder.infoAdicionalTextView.visibility = View.GONE
        } else {
            holder.infoAdicionalTextView.visibility = View.VISIBLE
            holder.infoAdicionalTextView.text = "Obs: ${animalActual.informacionAdicional}"
        }

        // Dejamos el fondo naranja como base.
        // El color personalizado lo podemos quitar o mantener. Por ahora lo quito para mantener el diseño naranja.
        holder.itemView.setBackgroundResource(R.drawable.background_caravana_naranja)

        holder.itemView.setOnClickListener {
            onAnimalClickListener(animalActual)
        }

        holder.itemView.setOnLongClickListener {
            onAnimalLongClickListener(animalActual)
            true
        }
    }
}