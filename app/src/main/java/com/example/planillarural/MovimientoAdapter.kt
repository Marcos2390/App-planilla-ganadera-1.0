package com.example.planillarural

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MovimientoAdapter(
    private val movimientos: List<Movimiento>,
    private val onMovimientoLongClickListener: (Movimiento) -> Unit // Para Eliminar
) : RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder>() {

    class MovimientoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tipoTextView: TextView = itemView.findViewById(R.id.tvTipoMovimiento)
        val fechaTextView: TextView = itemView.findViewById(R.id.tvFechaMovimiento)
        val motivoTextView: TextView = itemView.findViewById(R.id.tvMotivoMovimiento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimientoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movimiento, parent, false)
        return MovimientoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return movimientos.size
    }

    override fun onBindViewHolder(holder: MovimientoViewHolder, position: Int) {
        val movimientoActual = movimientos[position]
        holder.tipoTextView.text = "Tipo: ${movimientoActual.tipo} (${movimientoActual.cantidad} animales)"
        holder.fechaTextView.text = "Fecha: ${movimientoActual.fecha}"
        holder.motivoTextView.text = "Motivo: ${movimientoActual.motivo}"

        holder.itemView.setOnLongClickListener {
            onMovimientoLongClickListener(movimientoActual)
            true
        }
    }
}