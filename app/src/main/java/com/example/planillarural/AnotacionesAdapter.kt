package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class AnotacionesAdapter(
    private val anotaciones: List<Anotacion>,
    private val onClick: (Anotacion) -> Unit
) : RecyclerView.Adapter<AnotacionesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloAnotacion)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionAnotacion)
        val tvMonto: TextView = itemView.findViewById(R.id.tvMontoAnotacion)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFechaAnotacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anotacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val anotacion = anotaciones[position]
        
        holder.tvTitulo.text = anotacion.titulo
        holder.tvDescripcion.text = anotacion.descripcion
        holder.tvFecha.text = anotacion.fecha

        // Manejo del monto y colores
        if (anotacion.tipo == "Nota") {
            holder.tvMonto.visibility = View.GONE
        } else {
            holder.tvMonto.visibility = View.VISIBLE
            val simbolo = if (anotacion.tipo == "Ingreso") "+" else "-"
            val color = if (anotacion.tipo == "Ingreso") "#4CAF50" else "#D32F2F" // Verde o Rojo
            
            val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
            formatoMoneda.maximumFractionDigits = 0
            val montoFormateado = formatoMoneda.format(anotacion.monto).replace("$", "")
            
            holder.tvMonto.text = "$simbolo $$montoFormateado ${anotacion.moneda}"
            holder.tvMonto.setTextColor(android.graphics.Color.parseColor(color))
        }

        holder.itemView.setOnClickListener { onClick(anotacion) }
    }

    override fun getItemCount() = anotaciones.size
}