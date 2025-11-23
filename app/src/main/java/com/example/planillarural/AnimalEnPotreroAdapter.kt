package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnimalEnPotreroAdapter(
    private val animales: List<Animal>,
    private val onLongClick: (Animal) -> Unit
) : RecyclerView.Adapter<AnimalEnPotreroAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCaravana: TextView = view.findViewById(R.id.tvCaravanaSimple)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoriaSimple)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_animal_en_potrero, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = animales[position]
        holder.tvCaravana.text = animal.nombre
        holder.tvCategoria.text = animal.categoria

        holder.itemView.setOnLongClickListener {
            onLongClick(animal)
            true
        }
    }

    override fun getItemCount() = animales.size
}