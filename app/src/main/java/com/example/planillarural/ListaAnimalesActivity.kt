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

    // ... (declaraciones de DAOs y vistas se mantienen igual)
    private lateinit var animalDao: AnimalDao
    private lateinit var movimientoDao: MovimientoDao
    private lateinit var nacimientoPendienteDao: NacimientoPendienteDao
    private lateinit var sanidadDao: SanidadDao
    private lateinit var anotacionDao: AnotacionDao
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
        anotacionDao = database.anotacionDao()

        recyclerView = findViewById(R.id.recyclerViewAnimales)
        searchView = findViewById(R.id.searchViewAnimales)
        tvResumenCategorias = findViewById(R.id.tvResumenCategorias)
        btnAcciones = findViewById(R.id.btnAcciones)
        btnNotificaciones = findViewById(R.id.btnNotificaciones)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupListeners()
        crearCanalDeNotificaciones()
    }

    // ... (onResume, cargarAnimales, onAnimalClick, etc. se mantienen igual)

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.actions_menu, popup.menu)

        // ... (lógica de nacimientos pendientes igual)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                // ... (otros casos del menú igual)

                R.id.action_exportar_datos -> {
                    mostrarDialogoExportar()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    // ¡NUEVO! Diálogo para elegir formato
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

    // --- LÓGICA DE EXPORTACIÓN ---

    private fun exportarDatosCSV() {
        // ... (El código de exportar a CSV se mantiene igual que antes)
    }

    // ¡NUEVO! Lógica para exportar a PDF
    private fun exportarDatosPDF() {
        lifecycleScope.launch {
            // 1. Recolectar datos
            val animales = animalDao.obtenerTodosConBajas()
            val movimientos = movimientoDao.obtenerTodos()
            val anotaciones = anotacionDao.obtenerTodas()

            // 2. Construir el HTML
            val html = construirHtmlReporte(animales, movimientos, anotaciones)

            // 3. Imprimir el HTML a PDF
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

        // --- Tabla Bovinos ---
        sb.append("<h2>Tabla de Bovinos</h2>")
        sb.append("<table><tr><th>Caravana</th><th>Categoría</th><th>Raza</th><th>Estado</th><th>Fecha Nac.</th></tr>")
        animales.forEach { sb.append("<tr><td>${it.nombre}</td><td>${it.categoria}</td><td>${it.raza}</td><td>${it.status}</td><td>${it.fechaNac}</td></tr>") }
        sb.append("</table>")

        // --- Tabla Ovinos ---
        val ovinos = movimientos.filter { it.especie == "Ovino" }
        if (ovinos.isNotEmpty()) {
            sb.append("<h2>Tabla de Ovinos (Lotes)</h2>")
            sb.append("<table><tr><th>Movimiento</th><th>Categoría</th><th>Fecha</th><th>Cantidad</th><th>Motivo</th></tr>")
            ovinos.forEach { sb.append("<tr><td>${it.tipo}</td><td>${it.categoria}</td><td>${it.fecha}</td><td>${it.cantidad}</td><td>${it.motivo ?: ""}</td></tr>") }
            sb.append("</table>")
        }

        // --- Tabla Finanzas ---
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
        webView.webViewClient = object : android.webkit.WebViewClient() {
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

    // ... (El resto de la clase, como cargarAnimales, onAnimalClick, etc., se mantiene sin cambios)
    
}
