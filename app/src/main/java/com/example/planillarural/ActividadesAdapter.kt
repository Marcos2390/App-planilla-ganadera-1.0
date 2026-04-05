package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActividadesAdapter(
    private val actividades: List<ActividadReciente>
) : RecyclerView.Adapter<ActividadesAdapter.ViewHolder>() {

    // Conjunto para rastrear qué posiciones están expandidas
    private val posicionesExpandidas = mutableSetOf<Int>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIcono: TextView = itemView.findViewById(R.id.tvIconoDespliegue)
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloActividad)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFechaActividad)
        val tvDetalle: TextView = itemView.findViewById(R.id.tvDetalleActividad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad_desplegable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val actividad = actividades[position]
        holder.tvTitulo.text = actividad.titulo
        holder.tvFecha.text = actividad.fecha
        holder.tvDetalle.text = actividad.detalle

        val estaExpandido = posicionesExpandidas.contains(position)
        holder.tvDetalle.visibility = if (estaExpandido) View.VISIBLE else View.GONE
        holder.tvIcono.text = if (estaExpandido) "-" else "+"

        holder.itemView.setOnClickListener {
            if (estaExpandido) {
                posicionesExpandidas.remove(position)
            } else {
                posicionesExpandidas.add(position)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = actividades.size
}

data class ActividadReciente(
    val fecha: String,
    val titulo: String,
    val detalle: String,
    val tipoFiltro: Int // 0=Ingreso, 1=Sanidad, 2=Nacido, 3=Muerte
)