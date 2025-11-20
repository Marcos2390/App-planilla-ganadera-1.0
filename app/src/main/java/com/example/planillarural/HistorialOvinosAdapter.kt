package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistorialOvinosAdapter(
    private val eventos: List<EventoHistorial>
) : RecyclerView.Adapter<HistorialOvinosAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.tvHistorialTitulo)
        val detalle: TextView = itemView.findViewById(R.id.tvHistorialDetalle)
        val fecha: TextView = itemView.findViewById(R.id.tvHistorialFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_ovino, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evento = eventos[position]
        holder.titulo.text = evento.titulo
        holder.detalle.text = evento.detalle
        holder.fecha.text = evento.fecha
    }

    override fun getItemCount() = eventos.size
}