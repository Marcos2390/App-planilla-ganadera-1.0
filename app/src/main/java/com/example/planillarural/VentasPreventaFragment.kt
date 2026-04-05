package com.example.planillarural

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VentasPreventaFragment : Fragment() {

    private lateinit var ventaDao: VentaPreventaDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAgregar: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ventas_preventa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ventaDao = AppDatabase.getDatabase(requireContext()).ventaPreventaDao()
        recyclerView = view.findViewById(R.id.rvVentasPreventa)
        fabAgregar = view.findViewById(R.id.fabAgregarVenta)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        fabAgregar.setOnClickListener {
            mostrarDialogoAgregar()
        }

        cargarVentas()
    }

    private fun cargarVentas() {
        lifecycleScope.launch {
            val ventas = ventaDao.obtenerTodas()
            recyclerView.adapter = VentasPreventaAdapter(ventas)
        }
    }

    private fun mostrarDialogoAgregar() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_agregar_preventa, null)
        builder.setView(dialogView)

        val etCaravana = dialogView.findViewById<EditText>(R.id.etCaravanaPreventa)
        val etRaza = dialogView.findViewById<EditText>(R.id.etRazaPreventa)
        val etSexo = dialogView.findViewById<EditText>(R.id.etSexoPreventa)
        val etKilos = dialogView.findViewById<EditText>(R.id.etKilosPreventa)
        val etPrecio = dialogView.findViewById<EditText>(R.id.etPrecioPreventa) // ¡NUEVO!

        builder.setTitle("Agregar a Preventa")
            .setPositiveButton("Guardar") { _, _ ->
                val caravana = etCaravana.text.toString()
                val raza = etRaza.text.toString()
                val sexo = etSexo.text.toString()
                val kilos = etKilos.text.toString().toDoubleOrNull() ?: 0.0
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0 // ¡NUEVO!

                if (caravana.isNotEmpty()) {
                    lifecycleScope.launch {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val venta = VentaPreventa(
                            caravana = caravana,
                            raza = raza,
                            sexo = sexo,
                            kilos = kilos,
                            precio = precio, // ¡NUEVO!
                            fecha = sdf.format(Date())
                        )
                        ventaDao.insertar(venta)
                        cargarVentas()
                    }
                } else {
                    Toast.makeText(context, "La caravana es obligatoria", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    inner class VentasPreventaAdapter(private val lista: List<VentaPreventa>) : RecyclerView.Adapter<VentasPreventaAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvCaravana: TextView = view.findViewById(R.id.tvCaravanaVenta)
            val tvRaza: TextView = view.findViewById(R.id.tvRazaVenta)
            val tvSexo: TextView = view.findViewById(R.id.tvSexoVenta)
            val tvKilos: TextView = view.findViewById(R.id.tvKilosVenta)
            val tvEstado: TextView = view.findViewById(R.id.tvEstadoVenta)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_venta_preventa, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = lista[position]
            holder.tvCaravana.text = "Caravana: ${item.caravana}"
            holder.tvRaza.text = "Raza: ${item.raza}"
            holder.tvSexo.text = "Sexo: ${item.sexo}"
            holder.tvKilos.text = "Peso: ${item.kilos} kg | Precio: $${item.precio}" // ¡MOSTRAR PRECIO!
            holder.tvEstado.text = item.estado
            
            holder.itemView.setOnLongClickListener {
                mostrarOpciones(item)
                true
            }
        }

        override fun getItemCount() = lista.size

        private fun mostrarOpciones(item: VentaPreventa) {
            val opciones = arrayOf("Marcar como Vendido", "Eliminar")
            AlertDialog.Builder(requireContext())
                .setTitle("Opciones: ${item.caravana}")
                .setItems(opciones) { _, which ->
                    lifecycleScope.launch {
                        when (which) {
                            0 -> ventaDao.actualizar(item.copy(estado = "Vendido"))
                            1 -> ventaDao.eliminar(item)
                        }
                        cargarVentas()
                    }
                }
                .show()
        }
    }
}
