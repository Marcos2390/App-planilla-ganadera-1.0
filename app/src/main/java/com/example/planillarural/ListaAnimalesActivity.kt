package com.example.planillarural

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
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
import java.util.Calendar
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
    private lateinit var btnNotificaciones: ImageButton

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
        btnNotificaciones = findViewById(R.id.btnNotificaciones)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupListeners()
        crearCanalDeNotificaciones()
    }

    private fun crearCanalDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Recordatorios Sanidad"
            val descripcion = "Avisos sobre próximas vacunas y dosis"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val canal = NotificationChannel("SANIDAD_CHANNEL", nombre, importancia).apply {
                description = descripcion
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }

    private fun verificarRecordatorios() {
        lifecycleScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            val hoy = Date()
            val fechaHoy = sdf.format(hoy)

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val fechaManana = sdf.format(calendar.time)

            val pendientesHoy = sanidadDao.buscarPendientesPorFecha(fechaHoy)
            val pendientesManana = sanidadDao.buscarPendientesPorFecha(fechaManana)

            val hayPendientes = pendientesHoy.isNotEmpty() || pendientesManana.isNotEmpty()

            if (hayPendientes) {
                btnNotificaciones.setImageResource(R.drawable.ic_notification_bell)
                if (pendientesHoy.isNotEmpty()) {
                    lanzarNotificacion(1, "¡Atención! Tienes ${pendientesHoy.size} tareas de sanidad para HOY.")
                }
            } else {
                btnNotificaciones.setImageResource(R.drawable.ic_notification_bell_disabled)
            }

            btnNotificaciones.setOnClickListener {
                mostrarDialogoRecordatorios(pendientesHoy, pendientesManana)
            }
        }
    }

    private fun mostrarDialogoRecordatorios(hoy: List<Sanidad>, manana: List<Sanidad>) {
        val mensaje = StringBuilder()

        if (hoy.isEmpty() && manana.isEmpty()) {
            mensaje.append("No tienes recordatorios de sanidad pendientes para hoy ni mañana.")
        } else {
            if (hoy.isNotEmpty()) {
                mensaje.append("PARA HOY (${hoy.size}):\n")
                hoy.forEach { mensaje.append("- ${it.tratamiento} (${it.producto})\n") }
                mensaje.append("\n")
            }
            if (manana.isNotEmpty()) {
                mensaje.append("PARA MAÑANA (${manana.size}):\n")
                manana.forEach { mensaje.append("- ${it.tratamiento} (${it.producto})\n") }
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Recordatorios de Sanidad")
            .setMessage(mensaje.toString())
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun lanzarNotificacion(id: Int, mensaje: String) {
        val builder = NotificationCompat.Builder(this, "SANIDAD_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio Sanitario")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
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
                    val intent = Intent(this, AgregarMovimientoActivity::class.java)
                    intent.putExtra("ESPECIE", "Bovino")
                    startActivity(intent)
                    true
                }
                R.id.action_registrar_ovino -> {
                    startActivity(Intent(this, OvinosMainActivity::class.java))
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
                R.id.action_finanzas_notas -> {
                    startActivity(Intent(this, FinanzasNotasActivity::class.java))
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
            val animales = animalDao.obtenerTodosConBajas() 
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

                    writer.append("REPORTE DETALLADO POR ANIMAL (BOVINOS)\n")
                    writer.append("Caravana,Categoria,Raza,Estado,Fecha Nac,TIPO DE DATO,Detalle,Fecha Evento,Cantidad,Info Adicional\n")

                    val movimientosPorAnimal = movimientos.filter { it.animalId != null }.groupBy { it.animalId!! }
                    val sanidadPorAnimal = registrosSanidad.filter { it.animalId != null }.groupBy { it.animalId!! }

                    val animalesActivos = animales.filter { it.status == "Activo" }
                    val animalesVendidos = animales.filter { it.status == "Vendido" }
                    val animalesMuertos = animales.filter { it.status == "Muerto" }

                    writer.append("\n--- ANIMALES ACTIVOS ---\n")
                    for (animal in animalesActivos) {
                        escribirAnimalEnCSV(writer, animal, movimientosPorAnimal, sanidadPorAnimal)
                    }

                    if (animalesVendidos.isNotEmpty()) {
                        writer.append("\n--- ANIMALES VENDIDOS ---\n")
                        for (animal in animalesVendidos) {
                            escribirAnimalEnCSV(writer, animal, movimientosPorAnimal, sanidadPorAnimal)
                        }
                    }

                    if (animalesMuertos.isNotEmpty()) {
                        writer.append("\n--- ANIMALES MUERTOS ---\n")
                        for (animal in animalesMuertos) {
                            escribirAnimalEnCSV(writer, animal, movimientosPorAnimal, sanidadPorAnimal)
                        }
                    }

                    writer.append("\n--------------------------------------------------\n\n")

                    val movimientosOvinos = movimientos.filter { it.especie == "Ovino" }
                    if (movimientosOvinos.isNotEmpty()) {
                        writer.append("REGISTRO DE OVINOS (POR LOTE)\n")
                        writer.append("Tipo/Trabajo,Categoria,Fecha,Cantidad\n")
                        for (mov in movimientosOvinos) {
                            writer.append("${mov.tipo},${mov.categoria},${mov.fecha},${mov.cantidad}\n")
                        }
                        writer.append("\n")
                    }

                    val movimientosBovinosGlobales = movimientos.filter { it.animalId == null && it.especie == "Bovino" }
                    if (movimientosBovinosGlobales.isNotEmpty()) {
                        writer.append("MOVIMIENTOS GLOBALES BOVINOS (Sin ID)\n")
                        writer.append("Tipo,Categoria,Motivo,Fecha,Cantidad\n")
                        for (mov in movimientosBovinosGlobales) {
                            writer.append("${mov.tipo},${mov.categoria},${mov.motivo},${mov.fecha},${mov.cantidad}\n")
                        }
                        writer.append("\n")
                    }

                    if (nacimientosPendientes.isNotEmpty()) {
                        writer.append("NACIMIENTOS PENDIENTES DE IDENTIFICAR (BOVINOS)\n")
                        writer.append("Fecha,Categoria,Cantidad Total,Ya Asignados,Pendientes\n")
                        for (nac in nacimientosPendientes) {
                            val pendientesCount = nac.cantidadTotal - nac.cantidadAsignada
                            writer.append("${nac.fecha},${nac.categoria},${nac.cantidadTotal},${nac.cantidadAsignada},${pendientesCount}\n")
                        }
                        writer.append("\n")
                    }

                    writer.append("--------------------------------------------------\n\n")
                    writer.append("RESUMEN FINAL DE STOCK (SOLO ACTIVOS)\n")
                    writer.append("Especie,Categoria,Cantidad\n")
                    
                    for (cat in resumenCategorias) {
                        if (cat.count != 0) {
                            writer.append("Bovino,${cat.categoria},${cat.count}\n")
                        }
                    }
                    
                    val totalOvinos = movimientosOvinos.sumOf { it.cantidad }
                    writer.append("Ovino,TOTAL,${totalOvinos}\n")

                    writer.flush()
                    writer.close()

                    val uri = FileProvider.getUriForFile(this@ListaAnimalesActivity, "${packageName}.provider", file)
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/csv"
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(intent, "Exportar reporte con..."))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@ListaAnimalesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun escribirAnimalEnCSV(
        writer: FileWriter,
        animal: Animal,
        movimientosPorAnimal: Map<Int, List<Movimiento>>,
        sanidadPorAnimal: Map<Int, List<Sanidad>>
    ) {
        writer.append("${animal.nombre},${animal.categoria},${animal.raza},${animal.status},${animal.fechaNac},[ANIMAL],Registro,,,${animal.informacionAdicional ?: ""}\n")
        movimientosPorAnimal[animal.id]?.forEach { mov ->
            writer.append(",,,,,-> Movimiento,${mov.tipo} (${mov.motivo}),${mov.fecha},${mov.cantidad},\n")
        }
        sanidadPorAnimal[animal.id]?.forEach { san ->
            writer.append(",,,,,-> Sanidad,${san.tratamiento} (${san.producto}),${san.fecha},${san.dosis},Prox: ${san.fechaProximaDosis ?: ""}\n")
        }
        writer.append("\n")
    }

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
        cargarAnimales(searchView.query.toString())
        verificarRecordatorios()
    }

    private fun cargarAnimales(query: String = "") {
        lifecycleScope.launch {
            val listaDeAnimales = if (query.isEmpty()) {
                animalDao.obtenerTodosActivos()
            } else {
                animalDao.buscarPorNombre(query)
            }

            val conteoPorCategoria = animalDao.contarPorCategoria()
            val totalReal = conteoPorCategoria.sumOf { it.count }
            val resumenCategorias = conteoPorCategoria.filter { it.count > 0 }.joinToString(" | ") { "${it.categoria}: ${it.count}" }
            
            val movimientos = movimientoDao.obtenerTodos()
            val movimientosOvinos = movimientos.filter { it.especie == "Ovino" }
            val totalOvinos = movimientosOvinos.sumOf { it.cantidad }
            
            tvResumenCategorias.text = "Bovinos: $totalReal ($resumenCategorias) | Ovinos: $totalOvinos"

            val pendientes = nacimientoPendienteDao.obtenerTodosPendientes()
            
            animalAdapter = AnimalAdapter(listaDeAnimales, this@ListaAnimalesActivity::onAnimalClick, this@ListaAnimalesActivity::onAnimalLongClick)
            recyclerView.adapter = animalAdapter
        }
    }

    private fun onAnimalClick(animal: Animal) {
        val intent = Intent(this, AgregarAnimalActivity::class.java)
        intent.putExtra("ANIMAL_ID", animal.id)
        startActivity(intent)
    }

    private fun onAnimalLongClick(animal: Animal) {
        val opciones = arrayOf("Registrar Venta", "Registrar Muerte", "Eliminar Registro (Error)")
        AlertDialog.Builder(this)
            .setTitle("Acciones para ${animal.nombre}")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> registrarBaja(animal, "Venta")
                    1 -> registrarBaja(animal, "Muerte")
                    2 -> confirmarEliminacion(animal)
                }
            }
            .show()
    }

    private fun registrarBaja(animal: Animal, motivo: String) {
        lifecycleScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaActual = sdf.format(Date())
            val movimiento = Movimiento(
                animalId = animal.id,
                tipo = if (motivo == "Venta") "Venta" else "Muerte",
                categoria = animal.categoria,
                fecha = fechaActual,
                cantidad = 1,
                motivo = "Baja por $motivo (Desde lista)",
                especie = "Bovino"
            )
            movimientoDao.registrar(movimiento)

            val animalActualizado = animal.copy(status = if (motivo == "Venta") "Vendido" else "Muerto")
            animalDao.actualizar(animalActualizado)

            Toast.makeText(this@ListaAnimalesActivity, "Animal registrado como $motivo", Toast.LENGTH_SHORT).show()
            cargarAnimales(searchView.query.toString())
        }
    }

    private fun confirmarEliminacion(animal: Animal) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar definitivamente?")
            .setMessage("Esto borrará al animal y todo su historial. Úsalo solo si fue un error de carga.\n\nSi el animal se vendió o murió, usa las otras opciones.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    animalDao.eliminar(animal)
                    cargarAnimales(searchView.query.toString())
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}