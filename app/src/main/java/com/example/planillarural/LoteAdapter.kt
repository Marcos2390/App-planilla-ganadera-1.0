package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LoteAdapter(
    private val lotes: List<LotePotrero>,
    private val onLoteClick: (LotePotrero) -> Unit
) : RecyclerView.Adapter<LoteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidadLote)
        val tvEspecie: TextView = view.findViewById(R.id.tvEspecieLote)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoriaLote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lote, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lote = lotes[position]
        holder.tvCantidad.text = lote.cantidad.toString()
        holder.tvEspecie.text = lote.especie
        holder.tvCategoria.text = lote.categoria

        holder.itemView.setOnClickListener { onLoteClick(lote) }
    }

    override fun getItemCount() = lotes.size
}