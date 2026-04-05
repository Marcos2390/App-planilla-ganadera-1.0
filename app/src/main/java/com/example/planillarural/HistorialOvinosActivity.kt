package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class HistorialOvinosActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao
    private lateinit var sanidadDao: SanidadDao
    private lateinit var recyclerView: RecyclerView
    private var listaCompleta: List<EventoHistorial> = emptyList() // Guardamos la lista

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_ovinos)

        val database = AppDatabase.getDatabase(applicationContext)
        movimientoDao = database.movimientoDao()
        sanidadDao = database.sanidadDao()

        recyclerView = findViewById(R.id.recyclerViewHistorialOvinos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fabExportar: FloatingActionButton = findViewById(R.id.fabExportarHistorialOvinos)
        fabExportar.setOnClickListener {
            exportarHistorialOvinos()
        }

        cargarHistorial()
    }

    private fun cargarHistorial() {
        lifecycleScope.launch {
            val movimientos = movimientoDao.obtenerTodos().filter { it.especie == "Ovino" }
            val sanidad = sanidadDao.obtenerTodos().filter { it.especie == "Ovino" }

            val eventos = mutableListOf<EventoHistorial>()
            movimientos.forEach { 
                eventos.add(EventoHistorial(
                    id = it.id, 
                    tipoObjeto = "Movimiento", 
                    fecha = it.fecha, 
                    titulo = "${it.tipo}: ${it.cantidad} ${it.categoria}", 
                    detalle = "Motivo: ${it.motivo}"
                ))
            }
            sanidad.forEach {
                eventos.add(EventoHistorial(
                    id = it.id, 
                    tipoObjeto = "Sanidad", 
                    fecha = it.fecha, 
                    titulo = "Sanidad: ${it.tratamiento}", 
                    detalle = "Producto: ${it.producto} (${it.dosis}) x${it.cantidad}"
                ))
            }

            listaCompleta = eventos.sortedByDescending { it.fecha }

            recyclerView.adapter = HistorialOvinosAdapter(listaCompleta, this@HistorialOvinosActivity::onItemClick)
        }
    }

    private fun onItemClick(evento: EventoHistorial) {
        val opciones = arrayOf("Eliminar Registro")
        AlertDialog.Builder(this)
            .setTitle(evento.titulo)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> confirmarEliminacion(evento)
                }
            }
            .show()
    }

    private fun confirmarEliminacion(evento: EventoHistorial) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar?")
            .setMessage("Esta acción es irreversible.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    if (evento.tipoObjeto == "Movimiento") {
                        // Buscar el objeto real para eliminarlo (necesitamos un método en DAO o buscarlo)
                        // Para simplificar, asumimos que el DAO tiene método eliminar por ID o instanciamos uno dummy con ID
                        val movimientoDummy = Movimiento(
                            id = evento.id,
                            animalId = 0, tipo = "", categoria = "", fecha = "", cantidad = 0, especie = "Ovino", motivo = "Eliminado"
                        )
                        movimientoDao.eliminar(movimientoDummy)
                    } else {
                        val sanidadDummy = Sanidad(
                            id = evento.id,
                            animalId = 0, fecha = "", tratamiento = "", producto = "", dosis = "", especie = "Ovino"
                        )
                        sanidadDao.eliminar(sanidadDummy)
                    }
                    cargarHistorial()
                    Toast.makeText(this@HistorialOvinosActivity, "Registro eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun exportarHistorialOvinos() {
        if (listaCompleta.isEmpty()) {
            Toast.makeText(this, "No hay historial para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val fileName = "Historial_Ovinos_${System.currentTimeMillis()}.csv"
                    val file = File(cacheDir, fileName)
                    val writer = FileWriter(file)

                    // Encabezado
                    writer.append("Fecha,Tipo de Evento,Detalle\n")

                    // Datos
                    for (evento in listaCompleta) {
                        writer.append("${evento.fecha},${evento.titulo},${evento.detalle.replace('\n', ' ')}\n")
                    }

                    writer.flush()
                    writer.close()

                    // Compartir
                    val uri = FileProvider.getUriForFile(this@HistorialOvinosActivity, "${packageName}.provider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Exportar historial con..."))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@HistorialOvinosActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

// Clase de datos actualizada con ID y Tipo para poder identificar qué borrar
data class EventoHistorial(
    val id: Int, 
    val tipoObjeto: String, // "Movimiento" o "Sanidad"
    val fecha: String, 
    val titulo: String, 
    val detalle: String
)