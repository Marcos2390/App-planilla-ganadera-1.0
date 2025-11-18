package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnimalSelectableAdapter(
    private val animales: List<Animal>
) : RecyclerView.Adapter<AnimalSelectableAdapter.ViewHolder>() {

    private val seleccionados = mutableSetOf<Animal>()

    fun getAnimalesSeleccionados(): List<Animal> {
        return seleccionados.toList()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.tvNombreAnimal)
        val categoriaTextView: TextView = itemView.findViewById(R.id.tvCategoriaAnimal)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkboxAnimal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_animal_selectable, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = animales.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = animales[position]
        holder.nombreTextView.text = "Caravana: ${animal.nombre}"
        holder.categoriaTextView.text = "Categoría: ${animal.categoria}"

        holder.checkBox.isChecked = seleccionados.contains(animal)

        holder.itemView.setOnClickListener {
            holder.checkBox.toggle()
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                seleccionados.add(animal)
            } else {
                seleccionados.remove(animal)
            }
        }
    }
}