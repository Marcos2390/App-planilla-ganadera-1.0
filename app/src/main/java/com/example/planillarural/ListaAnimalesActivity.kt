package com.example.planillarural

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
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
    private lateinit var anotacionDao: AnotacionDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var animalAdapter: AnimalAdapter
    private lateinit var searchView: SearchView
    private lateinit var tvResumenCategorias: TextView
    private lateinit var btnNotificaciones: ImageButton

    // Botones de Acción Nuevos
    private lateinit var btnAgregarAnimal: TextView
    private lateinit var btnRegistrarMovimiento: TextView
    private lateinit var btnGestionOvinos: TextView
    private lateinit var btnNacimientos: TextView
    private lateinit var btnRegistrarSanidad: TextView
    private lateinit var btnGestionPotreros: TextView
    private lateinit var btnFinanzas: TextView
    private lateinit var btnExportar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_lista_animales)

        val database = AppDatabase.getDatabase(applicationContext)
        animalDao = database.animalDao()
        movimientoDao = database.movimientoDao()
        nacimientoPendienteDao = database.nacimientoPendienteDao()
        sanidadDao = database.sanidadDao()
        anotacionDao = database.anotacionDao()

        recyclerView = findViewById(R.id.recyclerViewAnimales)
        searchView = findViewById(R.id.searchViewAnimales)
        tvResumenCategorias = findViewById(R.id.tvResumenCategorias)
        btnNotificaciones = findViewById(R.id.btnNotificaciones)

        // Inicializar botones nuevos
        btnAgregarAnimal = findViewById(R.id.btnAgregarAnimal)
        btnRegistrarMovimiento = findViewById(R.id.btnRegistrarMovimiento)
        btnGestionOvinos = findViewById(R.id.btnGestionOvinos)
        btnNacimientos = findViewById(R.id.btnNacimientos)
        btnRegistrarSanidad = findViewById(R.id.btnRegistrarSanidad)
        btnGestionPotreros = findViewById(R.id.btnGestionPotreros)
        btnFinanzas = findViewById(R.id.btnFinanzas)
        btnExportar = findViewById(R.id.btnExportar)

        // USAR GRID LAYOUT (2 COLUMNAS)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        setupListeners()
        crearCanalDeNotificaciones()
    }

    private fun setupListeners() {
        // Listeners para los nuevos botones
        btnAgregarAnimal.setOnClickListener {
            startActivity(Intent(this, AgregarAnimalActivity::class.java))
        }

        btnRegistrarMovimiento.setOnClickListener {
            val intent = Intent(this, AgregarMovimientoActivity::class.java)
            intent.putExtra("ESPECIE", "Bovino")
            startActivity(intent)
        }

        btnGestionOvinos.setOnClickListener {
            startActivity(Intent(this, OvinosMainActivity::class.java))
        }

        btnNacimientos.setOnClickListener {
            startActivity(Intent(this, NacimientosPendientesActivity::class.java))
        }

        btnRegistrarSanidad.setOnClickListener {
             startActivity(Intent(this, AgregarSanidadGrupalActivity::class.java))
        }

        btnGestionPotreros.setOnClickListener {
            startActivity(Intent(this, PotrerosActivity::class.java))
        }

        btnFinanzas.setOnClickListener {
            startActivity(Intent(this, FinanzasNotasActivity::class.java))
        }

        btnExportar.setOnClickListener {
            mostrarDialogoExportar()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                cargarAnimales(newText.orEmpty())
                return true
            }
        })
    }

    private fun mostrarDialogoExportar() {
        val opciones = arrayOf("Documento (PDF)", "Hoja de Cálculo (CSV)")
        AlertDialog.Builder(this)
            .setTitle("¿En qué formato quieres el reporte?")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> exportarDatosPDF()
                    1 -> exportarDatosCSV()
                }
            }
            .show()
    }

    private fun exportarDatosPDF() {
        lifecycleScope.launch {
            val animales = animalDao.obtenerTodosConBajas()
            val movimientos = movimientoDao.obtenerTodos()
            val anotaciones = anotacionDao.obtenerTodas()
            
            val html = construirHtmlReporte(animales, movimientos, anotaciones)
            imprimirHTML(html)
        }
    }

    private fun construirHtmlReporte(animales: List<Animal>, movimientos: List<Movimiento>, anotaciones: List<Anotacion>): String {
        val css = """
            body { font-family: sans-serif; margin: 20px; }
            h1, h2 { color: #00796B; border-bottom: 2px solid #00796B; padding-bottom: 5px; }
            table { border-collapse: collapse; width: 100%; margin-bottom: 30px; }
            th, td { border: 1px solid #dddddd; text-align: left; padding: 8px; }
            th { background-color: #f2f2f2; }
            .firma { text-align: center; margin-top: 50px; font-style: italic; color: #757575; }
            .marca-agua { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-25deg); opacity: 0.1; font-size: 80px; font-weight: bold; color: #E0E0E0; z-index: -1; }
        """

        val sb = StringBuilder()
        sb.append("<html><head><style>$css</style></head><body>")
        sb.append("<div class='marca-agua'>Planilla Rural</div>")
        sb.append("<h1>Reporte General - Planilla Rural</h1>")

        sb.append("<h2>Tabla de Bovinos</h2>")
        sb.append("<table><tr><th>Caravana</th><th>Categoría</th><th>Raza</th><th>Estado</th><th>Fecha Nac.</th></tr>")
        animales.filter { it.status == "Activo" }.forEach { sb.append("<tr><td>${it.nombre}</td><td>${it.categoria}</td><td>${it.raza}</td><td>${it.status}</td><td>${it.fechaNac}</td></tr>") }
        sb.append("</table>")

        val ovinos = movimientos.filter { it.especie == "Ovino" }
        if (ovinos.isNotEmpty()) {
            sb.append("<h2>Tabla de Ovinos (Lotes)</h2>")
            sb.append("<table><tr><th>Movimiento</th><th>Categoría</th><th>Fecha</th><th>Cantidad</th><th>Motivo</th></tr>")
            ovinos.forEach { sb.append("<tr><td>${it.tipo}</td><td>${it.categoria}</td><td>${it.fecha}</td><td>${it.cantidad}</td><td>${it.motivo ?: ""}</td></tr>") }
            sb.append("</table>")
        }

        if (anotaciones.isNotEmpty()) {
            sb.append("<h2>Tabla de Finanzas y Notas</h2>")
            sb.append("<table><tr><th>Fecha</th><th>Título</th><th>Tipo</th><th>Monto</th><th>Moneda</th></tr>")
            anotaciones.forEach { 
                val monto = if(it.tipo == "Nota") "-" else it.monto.toString()
                val moneda = if(it.tipo == "Nota") "-" else it.moneda
                sb.append("<tr><td>${it.fecha}</td><td>${it.titulo}</td><td>${it.tipo}</td><td>$monto</td><td>$moneda</td></tr>") 
            }
            sb.append("</table>")
        }

        sb.append("<div class='firma'><p>Reporte generado con Planilla Rural</p><p>by @marcosdesign</p></div>")
        sb.append("</body></html>")
        return sb.toString()
    }

    private fun imprimirHTML(html: String) {
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "Reporte_Planilla_Rural"
                val printAdapter = view.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
    }

    private fun exportarDatosCSV() {
        lifecycleScope.launch {
            val animales = animalDao.obtenerTodosConBajas() 
            val movimientos = movimientoDao.obtenerTodos()
            val registrosSanidad = sanidadDao.obtenerTodos()
            val anotaciones = anotacionDao.obtenerTodas()
            val resumenCategorias = animalDao.contarPorCategoria()

            try {
                withContext(Dispatchers.IO) {
                    val fileName = "PlanillaRural_Reporte_${System.currentTimeMillis()}.csv"
                    val file = File(cacheDir, fileName)
                    val writer = FileWriter(file)

                    writer.append("=== TABLA DE BOVINOS (INDIVIDUAL) ===\n")
                    writer.append("Caravana,Categoria,Raza,Estado,Fecha Nac,Ultima Info\n")
                    val animalesActivos = animales.filter { it.status == "Activo" }
                    for (animal in animalesActivos) {
                        writer.append("${animal.nombre},${animal.categoria},${animal.raza},${animal.status},${animal.fechaNac},${animal.informacionAdicional ?: ""}\n")
                    }
                    writer.append("\n\n")

                    val movimientosOvinos = movimientos.filter { it.especie == "Ovino" }
                    if (movimientosOvinos.isNotEmpty()) {
                        writer.append("=== TABLA DE OVINOS (LOTES) ===\n")
                        writer.append("Movimiento,Categoria,Fecha,Cantidad,Motivo\n")
                        for (mov in movimientosOvinos) {
                            writer.append("${mov.tipo},${mov.categoria},${mov.fecha},${mov.cantidad},${mov.motivo ?: ""}\n")
                        }
                        writer.append("\n\n")
                    }

                    if (anotaciones.isNotEmpty()) {
                        writer.append("=== TABLA DE FINANZAS Y NOTAS ===\n")
                        writer.append("Fecha,Titulo,Tipo,Monto,Moneda,Detalle\n")
                        for (nota in anotaciones) {
                            val montoStr = if (nota.tipo == "Nota") "-" else nota.monto.toString()
                            val monedaStr = if (nota.tipo == "Nota") "-" else nota.moneda
                            writer.append("${nota.fecha},${nota.titulo},${nota.tipo},${montoStr},${monedaStr},${nota.descripcion}\n")
                        }
                        writer.append("\n\n")
                    }

                    writer.append("=== RESUMEN DE STOCK ACTUAL ===\n")
                    writer.append("Especie,Categoria,Cantidad\n")
                    for (cat in resumenCategorias) {
                        if (cat.count != 0) {
                            writer.append("Bovino,${cat.categoria},${cat.count}\n")
                        }
                    }
                    
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
                runOnUiThread { Toast.makeText(this@ListaAnimalesActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
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

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
        cargarAnimales(searchView.query.toString())
        verificarRecordatorios()
        verificarNacimientosPendientes()
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
            var totalOvinos = 0
            for (mov in movimientosOvinos) {
                if (mov.tipo == "Nacimiento" || mov.tipo == "Compra" || mov.tipo == "Entrada") totalOvinos += mov.cantidad
                if (mov.tipo == "Venta" || mov.tipo == "Muerte" || mov.tipo == "Salida") totalOvinos -= mov.cantidad
            }
            
            tvResumenCategorias.text = "Bovinos: $totalReal ($resumenCategorias) | Ovinos: $totalOvinos"

            animalAdapter = AnimalAdapter(listaDeAnimales, this@ListaAnimalesActivity::onAnimalClick, this@ListaAnimalesActivity::onAnimalLongClick)
            recyclerView.adapter = animalAdapter
        }
    }

    private fun verificarRecordatorios() {
        lifecycleScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hoy = Date()
            val fechaHoy = sdf.format(hoy)
            val pendientesHoy = sanidadDao.buscarPendientesPorFecha(fechaHoy)

            if (pendientesHoy.isNotEmpty()) {
                btnNotificaciones.setImageResource(R.drawable.ic_notification_bell)
                lanzarNotificacion(1, "Tienes ${pendientesHoy.size} tareas para HOY.")
            } else {
                btnNotificaciones.setImageResource(R.drawable.ic_notification_bell_disabled)
            }
            
            btnNotificaciones.setOnClickListener {
                mostrarDialogoRecordatorios(pendientesHoy)
            }
        }
    }

    private fun verificarNacimientosPendientes() {
        lifecycleScope.launch {
            val pendientes = nacimientoPendienteDao.obtenerTodosPendientes()
            if (pendientes.isNotEmpty()) {
                btnNacimientos.text = "Nacimientos\n(${pendientes.size})"
            } else {
                btnNacimientos.text = "Nacimientos"
            }
        }
    }

    private fun mostrarDialogoRecordatorios(hoy: List<Sanidad>) {
        val mensaje = if (hoy.isEmpty()) "No hay tareas para hoy." else "Tareas de hoy:\n" + hoy.joinToString("\n") { "- ${it.tratamiento}" }
        AlertDialog.Builder(this).setTitle("Recordatorios").setMessage(mensaje).setPositiveButton("Ok", null).show()
    }

    private fun lanzarNotificacion(id: Int, mensaje: String) {
        val builder = NotificationCompat.Builder(this, "SANIDAD_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Planilla Rural")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    private fun onAnimalClick(animal: Animal) {
        val intent = Intent(this, AgregarAnimalActivity::class.java)
        intent.putExtra("ANIMAL_ID", animal.id)
        startActivity(intent)
    }

    private fun onAnimalLongClick(animal: Animal) {
        val opciones = arrayOf("Registrar Venta", "Registrar Muerte", "Eliminar Registro")
        AlertDialog.Builder(this)
            .setTitle(animal.nombre)
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
                motivo = motivo,
                especie = "Bovino"
            )
            movimientoDao.registrar(movimiento)
            val animalActualizado = animal.copy(status = if (motivo == "Venta") "Vendido" else "Muerto")
            animalDao.actualizar(animalActualizado)
            cargarAnimales(searchView.query.toString())
            Toast.makeText(this@ListaAnimalesActivity, "Baja registrada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarEliminacion(animal: Animal) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar?")
            .setMessage("Se borrará todo el historial.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    animalDao.eliminar(animal)
                    cargarAnimales(searchView.query.toString())
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun escribirAnimalEnCSV(writer: FileWriter, animal: Animal, movimientosPorAnimal: Map<Int, List<Movimiento>>, sanidadPorAnimal: Map<Int, List<Sanidad>>) {
        // Método legacy conservado por compatibilidad si se requiere
    }
}