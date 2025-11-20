package com.example.planillarural

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnotacionAdapter(
    private val anotaciones: List<Anotacion>,
    private val onLongClick: (Anotacion) -> Unit
) : RecyclerView.Adapter<AnotacionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloAnotacion)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoAnotacion)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionAnotacion)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaAnotacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anotacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = anotaciones[position]

        holder.tvTitulo.text = item.titulo
        holder.tvDescripcion.text = item.descripcion
        holder.tvFecha.text = item.fecha

        // Lógica de visualización según el tipo (Ingreso/Gasto/Nota)
        if (item.tipo == "Nota") {
            holder.tvMonto.visibility = View.GONE
        } else {
            holder.tvMonto.visibility = View.VISIBLE
            val simbolo = if (item.moneda == "USD") "U\$S" else "$"
            
            if (item.tipo == "Ingreso") {
                holder.tvMonto.text = "+ $simbolo ${item.monto}"
                holder.tvMonto.setTextColor(Color.parseColor("#4CAF50")) // Verde
            } else {
                holder.tvMonto.text = "- $simbolo ${item.monto}"
                holder.tvMonto.setTextColor(Color.parseColor("#F44336")) // Rojo
            }
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount() = anotaciones.size
}