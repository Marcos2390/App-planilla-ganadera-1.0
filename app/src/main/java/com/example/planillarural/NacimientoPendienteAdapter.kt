package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NacimientoPendienteAdapter(
    private val nacimientos: List<NacimientoPendiente>,
    private val onAsignarClickListener: (NacimientoPendiente) -> Unit
) : RecyclerView.Adapter<NacimientoPendienteAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val infoTextView: TextView = itemView.findViewById(R.id.tvNacimientoInfo)
        val progresoTextView: TextView = itemView.findViewById(R.id.tvNacimientoProgreso)
        val asignarButton: Button = itemView.findViewById(R.id.btnAsignarIds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nacimiento_pendiente, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = nacimientos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nacimiento = nacimientos[position]

        holder.infoTextView.text = "${nacimiento.cantidadTotal} ${nacimiento.categoria}s (${nacimiento.fecha})"
        holder.progresoTextView.text = "Asignados: ${nacimiento.cantidadAsignada} de ${nacimiento.cantidadTotal}"

        holder.asignarButton.setOnClickListener {
            onAsignarClickListener(nacimiento)
        }
    }
}