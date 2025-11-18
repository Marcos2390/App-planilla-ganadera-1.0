package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListaAnimalesActivity : AppCompatActivity() {

    private lateinit var animalDao: AnimalDao
    private lateinit var movimientoDao: MovimientoDao
    private lateinit var nacimientoPendienteDao: NacimientoPendienteDao
    private lateinit var sanidadDao: SanidadDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var animalAdapter: AnimalAdapter
    private lateinit var searchView: SearchView
    private lateinit var tvResumenCategorias: TextView
    private lateinit var btnAcciones: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_lista_animales)

        val database = AppDatabase.getDatabase(applicationContext)
        animalDao = database.animalDao()
        movimientoDao = database.movimientoDao()
        nacimientoPendienteDao = database.nacimientoPendienteDao()
        sanidadDao = database.sanidadDao()

        recyclerView = findViewById(R.id.recyclerViewAnimales)
        searchView = findViewById(R.id.searchViewAnimales)
        tvResumenCategorias = findViewById(R.id.tvResumenCategorias)
        btnAcciones = findViewById(R.id.btnAcciones)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupListeners()
    }

    private fun setupListeners() {
        btnAcciones.setOnClickListener { view ->
            showPopupMenu(view)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                cargarAnimales(newText.orEmpty())
                return true
            }
        })
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.actions_menu, popup.menu)

        lifecycleScope.launch {
            val pendientes = nacimientoPendienteDao.obtenerTodosPendientes()
            val nacimientosMenuItem = popup.menu.findItem(R.id.action_nacimientos_pendientes)
            nacimientosMenuItem.isVisible = pendientes.isNotEmpty()
            if (pendientes.isNotEmpty()) {
                nacimientosMenuItem.title = "Nacimientos por Identificar (${pendientes.size})"
            }
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_agregar_animal -> {
                    startActivity(Intent(this, AgregarAnimalActivity::class.java))
                    true
                }
                R.id.action_registrar_movimiento -> {
                    startActivity(Intent(this, AgregarMovimientoActivity::class.java))
                    true
                }
                R.id.action_registrar_sanidad -> {
                    startActivity(Intent(this, AgregarSanidadGrupalActivity::class.java))
                    true
                }
                R.id.action_nacimientos_pendientes -> {
                    startActivity(Intent(this, NacimientosPendientesActivity::class.java))
                    true
                }
                R.id.action_exportar_datos -> {
                    exportarDatosCSV()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun exportarDatosCSV() {
        lifecycleScope.launch {
            // 1. Obtener todos los datos necesarios
            val animales = animalDao.obtenerTodos()
            val movimientos = movimientoDao.obtenerTodos()
            val registrosSanidad = sanidadDao.obtenerTodos()
            val nacimientosPendientes = nacimientoPendienteDao.obtenerTodosPendientes()
            val resumenCategorias = animalDao.contarPorCategoria()

            if (animales.isEmpty() && movimientos.isEmpty() && registrosSanidad.isEmpty()) {
                Toast.makeText(this@ListaAnimalesActivity, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                withContext(Dispatchers.IO) {
                    val fileName = "PlanillaRural_Reporte_${System.currentTimeMillis()}.csv"
                    val file = File(cacheDir, fileName)
                    val writer = FileWriter(file)

                    // --- PARTE 1: DETALLE INDIVIDUAL (Animal + sus movimientos + su sanidad) ---
                    writer.append("REPORTE DETALLADO POR ANIMAL\n")
                    writer.append("Caravana,Categoria,Raza,Fecha Nac,TIPO DE DATO,Detalle/Motivo/Tratamiento,Fecha Evento,Cantidad/Dosis,Info Adicional\n")

                    // Agrupar datos por ID de animal para acceso rápido
                    val movimientosPorAnimal = movimientos.filter { it.animalId != null }.groupBy { it.animalId!! }
                    val sanidadPorAnimal = registrosSanidad.filter { it.animalId != null }.groupBy { it.animalId!! }

                    for (animal in animales) {
                        // 1.1 Fila del Animal
                        writer.append("${animal.nombre},${animal.categoria},${animal.raza},${animal.fechaNac},[ANIMAL],Registro,,,${animal.informacionAdicional ?: ""}\n")

                        // 1.2 Sus Movimientos
                        val susMovimientos = movimientosPorAnimal[animal.id]
                        susMovimientos?.forEach { mov ->
                            writer.append(",,,,-> Movimiento,${mov.tipo} (${mov.motivo}),${mov.fecha},${mov.cantidad},\n")
                        }

                        // 1.3 Su Sanidad
                        val suSanidad = sanidadPorAnimal[animal.id]
                        suSanidad?.forEach { san ->
                            writer.append(",,,,-> Sanidad,${san.tratamiento} (${san.producto}),${san.fecha},${san.dosis},Prox: ${san.fechaProximaDosis ?: ""}\n")
                        }
                        
                        writer.append("\n") // Separador entre animales
                    }

                    writer.append("\n")
                    writer.append("--------------------------------------------------\n")
                    writer.append("\n")

                    // --- PARTE 2: MOVIMIENTOS GLOBALES (Sin animal asignado, ej: ventas generales) ---
                    val movimientosGlobales = movimientos.filter { it.animalId == null }
                    if (movimientosGlobales.isNotEmpty()) {
                        writer.append("MOVIMIENTOS GLOBALES / SIN ID ASIGNADO\n")
                        writer.append("Tipo,Categoria,Motivo,Fecha,Cantidad\n")
                        for (mov in movimientosGlobales) {
                            writer.append("${mov.tipo},${mov.categoria},${mov.motivo},${mov.fecha},${mov.cantidad}\n")
                        }
                        writer.append("\n")
                    }

                    // --- PARTE 3: NACIMIENTOS PENDIENTES DE IDENTIFICAR ---
                    if (nacimientosPendientes.isNotEmpty()) {
                        writer.append("NACIMIENTOS PENDIENTES DE IDENTIFICAR\n")
                        writer.append("Fecha,Categoria,Cantidad Total,Ya Asignados,Pendientes\n")
                        for (nac in nacimientosPendientes) {
                            val pendientesCount = nac.cantidadTotal - nac.cantidadAsignada
                            writer.append("${nac.fecha},${nac.categoria},${nac.cantidadTotal},${nac.cantidadAsignada},${pendientesCount}\n")
                        }
                        writer.append("\n")
                    }

                    writer.append("--------------------------------------------------\n")
                    writer.append("\n")

                    // --- PARTE 4: RESUMEN FINAL DE STOCK ---
                    writer.append("RESUMEN TOTAL DE STOCK\n")
                    writer.append("Categoria,Cantidad\n")
                    
                    var granTotal = 0
                    for (cat in resumenCategorias) {
                        if (cat.count != 0) { // Solo mostrar categorías con animales
                            writer.append("${cat.categoria},${cat.count}\n")
                            granTotal += cat.count
                        }
                    }
                    writer.append("TOTAL GENERAL,${granTotal}\n")

                    writer.flush()
                    writer.close()

                    // Compartir
                    val uri = FileProvider.getUriForFile(
                        this@ListaAnimalesActivity,
                        "${packageName}.provider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/csv"
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    startActivity(Intent.createChooser(intent, "Exportar reporte con..."))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@ListaAnimalesActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
        cargarAnimales(searchView.query.toString())
    }

    private fun cargarAnimales(query: String = "") {
        lifecycleScope.launch {
            val listaDeAnimales = if (query.isEmpty()) {
                animalDao.obtenerTodos()
            } else {
                animalDao.buscarPorNombre(query)
            }

            val conteoPorCategoria = animalDao.contarPorCategoria()
            val totalReal = conteoPorCategoria.sumOf { it.count }
            val resumenCategorias = conteoPorCategoria.filter { it.count > 0 }.joinToString(" | ") { "${it.categoria}: ${it.count}" }
            tvResumenCategorias.text = "Total: $totalReal | $resumenCategorias"

            val pendientes = nacimientoPendienteDao.obtenerTodosPendientes()
            // Solo ocultamos la visibilidad del menú contextual dinámicamente, no hay botón en pantalla que ocultar aquí
            // ya que usamos el menú general 'Acciones'.

            animalAdapter = AnimalAdapter(
                animales = listaDeAnimales,
                onAnimalClickListener = { animal ->
                    val intent = Intent(this@ListaAnimalesActivity, AgregarAnimalActivity::class.java)
                    intent.putExtra("ANIMAL_ID", animal.id)
                    startActivity(intent)
                },
                onAnimalLongClickListener = { animal ->
                    mostrarDialogoDeConfirmacion(animal)
                }
            )
            recyclerView.adapter = animalAdapter
        }
    }

    private fun mostrarDialogoDeConfirmacion(animal: Animal) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el animal con caravana ${animal.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarAnimal(animal)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarAnimal(animal: Animal) {
        lifecycleScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaActual = sdf.format(Date())
            val movimiento = Movimiento(
                animalId = animal.id,
                tipo = "Muerte",
                categoria = animal.categoria,
                fecha = fechaActual,
                cantidad = 1,
                motivo = "Eliminado desde la lista"
            )
            movimientoDao.registrar(movimiento)
            animalDao.eliminar(animal)
            cargarAnimales(searchView.query.toString())
        }
    }
}