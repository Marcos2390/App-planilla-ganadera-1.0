package com.example.planillarural

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListaAnimalesActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var animalDao: AnimalDao
    private lateinit var nacimientoPendienteDao: NacimientoPendienteDao

    val pickCsvLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { importarLecturas(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_animales)

        val db = AppDatabase.getDatabase(this)
        animalDao = db.animalDao()
        nacimientoPendienteDao = db.nacimientoPendienteDao()
        
        crearCanalDeNotificaciones()

        viewPager = findViewById(R.id.viewPagerPrincipal)
        tabLayout = findViewById(R.id.tabLayoutIndicator)
        
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Inicio"
                1 -> "Stock"
                2 -> "Historial"
                3 -> "Ventas"
                4 -> "Estadísticas"
                else -> null
            }
        }.attach()

        configurarBotonesPrincipales()
    }

    override fun onResume() {
        super.onResume()
        actualizarBadgeNacimientos()
    }

    private fun configurarBotonesPrincipales() {
        findViewById<View>(R.id.btnAgregarAnimal).setOnClickListener { startActivity(Intent(this, AgregarAnimalActivity::class.java)) }
        findViewById<View>(R.id.btnRegistrarMovimiento).setOnClickListener { startActivity(Intent(this, AgregarMovimientoActivity::class.java)) }
        findViewById<View>(R.id.btnSanidadPanel).setOnClickListener { startActivity(Intent(this, SanidadActivity::class.java)) }
        findViewById<View>(R.id.btnGestionOvinos).setOnClickListener { startActivity(Intent(this, OvinosMainActivity::class.java)) }
        findViewById<View>(R.id.btnGestionPotreros).setOnClickListener { startActivity(Intent(this, PotrerosActivity::class.java)) }
        findViewById<View>(R.id.btnFinanzas).setOnClickListener { startActivity(Intent(this, FinanzasNotasActivity::class.java)) }
        findViewById<View>(R.id.btnNacimientos).setOnClickListener { startActivity(Intent(this, NacimientosPendientesActivity::class.java)) }
    }

    private fun actualizarBadgeNacimientos() {
        lifecycleScope.launch {
            val pendientes = nacimientoPendienteDao.obtenerTodosPendientes()
            val badge = findViewById<TextView>(R.id.badgeNacimientos)
            if (badge != null) {
                badge.visibility = if (pendientes.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    fun mostrarDialogoImportar() {
        val opciones = arrayOf("Seleccionar archivo (CSV o Texto)", "BORRAR TODO EL STOCK (Limpiar errores)")
        AlertDialog.Builder(this)
            .setTitle("Importar Datos")
            .setItems(opciones) { _, which ->
                if (which == 0) pickCsvLauncher.launch("text/*") else confirmarBorradoTotal()
            }
            .show()
    }

    private fun confirmarBorradoTotal() {
        AlertDialog.Builder(this)
            .setTitle("¡ATENCIÓN!")
            .setMessage("Esto borrará ABSOLUTAMENTE TODO (Animales, Historial, Sanidad, Ventas, etc.). ¿Estás seguro?")
            .setPositiveButton("BORRAR TODO") { _, _ ->
                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(this@ListaAnimalesActivity)
                    withContext(Dispatchers.IO) {
                        db.clearAllTables()
                    }
                    Toast.makeText(this@ListaAnimalesActivity, "Base de datos reseteada", Toast.LENGTH_SHORT).show()
                    actualizarBadgeNacimientos()
                    finish()
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun importarLecturas(uri: Uri) {
        lifecycleScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    val inputStream = contentResolver.openInputStream(uri)
                    val reader = inputStream?.bufferedReader()
                    val lineas = reader?.readLines() ?: emptyList()
                    var c = 0
                    if (lineas.isNotEmpty() && lineas[0].any { it.code < 32 && it.code != 10 && it.code != 13 && it.code != 9 }) return@withContext -1
                    lineas.forEach { line ->
                        if (line.trim().isEmpty()) return@forEach
                        val data = line.split(",")
                        val caravana = data[0].trim()
                        if (caravana.isNotEmpty() && caravana.length < 30) {
                            val animal = Animal(
                                nombre = caravana,
                                categoria = if (data.size > 1) data[1].trim() else "Importado",
                                raza = if (data.size > 2) data[2].trim() else "N/A",
                                fechaNac = "",
                                status = "Activo"
                            )
                            animalDao.insertar(animal)
                            c++
                        }
                    }
                    c
                }
                if (res == -1) Toast.makeText(this@ListaAnimalesActivity, "Error: El archivo no es de texto", Toast.LENGTH_LONG).show()
                else Toast.makeText(this@ListaAnimalesActivity, "Se cargaron $res animales", Toast.LENGTH_LONG).show()
            } catch (e: Exception) { Toast.makeText(this@ListaAnimalesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    fun mostrarDialogoExportar() {
        val opciones = arrayOf("Reporte General", "Reporte de Ventas")
        AlertDialog.Builder(this).setTitle("Exportar").setItems(opciones) { _, w ->
            if (w == 0) exportarGeneral() else exportarVentas()
        }.show()
    }

    private fun exportarGeneral() {
        lifecycleScope.launch {
            val activos = animalDao.obtenerTodosActivos()
            val sanidadDao = AppDatabase.getDatabase(this@ListaAnimalesActivity).sanidadDao()
            
            val builder = StringBuilder()
            builder.append("<html><head><style>")
            builder.append("body { font-family: Arial, sans-serif; position: relative; margin: 0; padding: 20px; }")
            builder.append(".watermark { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); font-size: 80px; color: rgba(200, 200, 200, 0.2); z-index: -1; white-space: nowrap; pointer-events: none; }")
            builder.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
            builder.append("th, td { border: 1px solid #ddd; padding: 10px; text-align: left; font-size: 12px; }")
            builder.append("th { background-color: #00796B; color: white; }")
            builder.append("tr:nth-child(even) { background-color: #f9f9f9; }")
            builder.append("h1 { color: #00796B; text-align: center; }")
            builder.append("</style></head><body>")
            builder.append("<div class='watermark'>PLANILLA RURAL<br>@marcosdesign</div>")
            builder.append("<h1>Reporte General de Ganado</h1>")
            builder.append("<p>Generado el: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}</p>")
            builder.append("<table>")
            builder.append("<tr><th>ID/Caravana</th><th>Género/Cat.</th><th>Raza</th><th>Última Sanidad</th><th>Observaciones</th></tr>")
            
            for (animal in activos) {
                val registrosSanidad = sanidadDao.obtenerPorAnimal(animal.id)
                val ultimaSanidad = registrosSanidad.firstOrNull()
                val sanidadTexto = if (ultimaSanidad != null) "${ultimaSanidad.fecha} - ${ultimaSanidad.tratamiento}" else "Sin registros"
                builder.append("<tr><td><b>${animal.nombre}</b></td><td>${animal.categoria}</td><td>${animal.raza}</td><td>$sanidadTexto</td><td>${animal.informacionAdicional ?: "-"}</td></tr>")
            }
            
            builder.append("</table></body></html>")
            imprimir(builder.toString(), "General")
        }
    }

    private fun exportarVentas() {
        lifecycleScope.launch {
            val ventas = AppDatabase.getDatabase(this@ListaAnimalesActivity).ventaPreventaDao().obtenerTodas().filter { it.estado == "Vendido" }
            
            val builder = StringBuilder()
            builder.append("<html><head><style>")
            builder.append("body { font-family: Arial, sans-serif; position: relative; margin: 0; padding: 20px; }")
            builder.append(".watermark { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); font-size: 80px; color: rgba(200, 200, 200, 0.2); z-index: -1; white-space: nowrap; pointer-events: none; }")
            builder.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
            builder.append("th, td { border: 1px solid #ddd; padding: 10px; text-align: left; font-size: 12px; }")
            builder.append("th { background-color: #1976D2; color: white; }")
            builder.append(".total { font-size: 16px; font-weight: bold; color: #1976D2; margin-top: 20px; text-align: right; }")
            builder.append("h1 { color: #1976D2; text-align: center; }")
            builder.append("</style></head><body>")
            builder.append("<div class='watermark'>VENTAS REGISTRADAS<br>PLANILLA RURAL</div>")
            builder.append("<h1>Reporte de Ventas</h1>")
            builder.append("<table>")
            builder.append("<tr><th>ID/Caravana</th><th>Género</th><th>Raza</th><th>Kilage</th><th>Precio (USD)</th></tr>")
            
            var totalDolares = 0.0
            for (venta in ventas) {
                totalDolares += venta.precio
                builder.append("<tr><td><b>${venta.caravana}</b></td><td>${venta.sexo}</td><td>${venta.raza}</td><td>${venta.kilos} kg</td><td>$ ${venta.precio}</td></tr>")
            }
            
            builder.append("</table>")
            builder.append("<div class='total'>TOTAL VENTAS: USD $ ${String.format("%.2f", totalDolares)}</div>")
            builder.append("</body></html>")
            
            imprimir(builder.toString(), "Ventas")
        }
    }

    private fun imprimir(html: String, name: String) {
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(v: WebView, u: String) {
                val pm = getSystemService(Context.PRINT_SERVICE) as PrintManager
                pm.print(name, v.createPrintDocumentAdapter(name), PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
    }

    private fun crearCanalDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val canal = NotificationChannel("SANIDAD_CHANNEL", "Notificaciones", NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(canal)
        }
    }
}

class MainPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 5
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListaAnimalesFragment()      
            1 -> ResumenGlobalFragment()      
            2 -> ActividadesFragment() 
            3 -> VentasPreventaFragment()
            else -> EstadisticasFragment()
        }
    }
}

class EstadisticasFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View? =
        inflater.inflate(R.layout.fragment_estadisticas, container, false)

    override fun onResume() { super.onResume(); actualizar() }

    private fun actualizar() {
        if (!isAdded) return
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val activos = db.animalDao().obtenerTodosActivos()

            view?.let { v ->
                val machos = activos.filter { it.categoria.contains("Ternero", true) || it.categoria.contains("Macho", true) }.size
                val hembras = activos.filter { it.categoria.contains("Ternera", true) || it.categoria.contains("Hembra", true) }.size
                
                v.findViewById<TextView>(R.id.tvNacimientosMachos).text = machos.toString()
                v.findViewById<TextView>(R.id.tvNacimientosHembras).text = hembras.toString()

                val container = v.findViewById<LinearLayout>(R.id.containerPorcentajes)
                container.removeAllViews()
                if (activos.isNotEmpty()) {
                    activos.groupingBy { it.categoria }.eachCount().forEach { (cat, count) ->
                        val tv = TextView(requireContext())
                        val porcentaje = (count * 100.0 / activos.size)
                        tv.text = "$cat: $count (${String.format("%.1f", porcentaje)}%)"
                        tv.setPadding(0, 8, 0, 8)
                        container.addView(tv)
                    }
                }
            }
        }
    }
}

class ListaAnimalesFragment : Fragment() {
    private lateinit var animalDao: AnimalDao
    private lateinit var rv: RecyclerView
    private lateinit var tvResumen: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View? =
        inflater.inflate(R.layout.fragment_lista_animales, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animalDao = AppDatabase.getDatabase(requireContext()).animalDao()
        rv = view.findViewById(R.id.recyclerViewAnimales)
        tvResumen = view.findViewById(R.id.tvResumenCategorias)

        view.findViewById<View>(R.id.btnSNIG).setOnClickListener { 
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.snig.gub.uy"))) 
        }
        view.findViewById<View>(R.id.btnImportarFragment).setOnClickListener { 
            (activity as? ListaAnimalesActivity)?.mostrarDialogoImportar() 
        }
        view.findViewById<View>(R.id.btnExportarFragment).setOnClickListener { 
            (activity as? ListaAnimalesActivity)?.mostrarDialogoExportar() 
        }

        view.findViewById<SearchView>(R.id.searchViewAnimales).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean = false
            override fun onQueryTextChange(q: String?): Boolean { cargar(q ?: ""); return true }
        })

        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        cargar()
    }

    override fun onResume() { super.onResume(); cargar() }

    private fun cargar(q: String = "") {
        if (!isAdded) return
        lifecycleScope.launch {
            val lista = if (q.isEmpty()) animalDao.obtenerTodosActivos() else animalDao.buscarPorNombre(q)
            tvResumen.text = "Total Activos: ${lista.size}"
            rv.adapter = AnimalAdapter(lista, { a -> onAnimalClick(a) }, { a -> onAnimalLongClick(a) })
        }
    }

    private fun onAnimalClick(a: Animal) { startActivity(Intent(requireContext(), AgregarAnimalActivity::class.java).putExtra("ANIMAL_ID", a.id)) }

    private fun onAnimalLongClick(a: Animal) {
        AlertDialog.Builder(requireContext()).setTitle(a.nombre).setItems(arrayOf("Venta", "Muerte", "Salida", "Eliminar")) { _, w ->
            when(w) { 
                0 -> registrarBaja(a, "Venta")
                1 -> registrarBaja(a, "Muerte")
                2 -> registrarBaja(a, "Salida")
                3 -> confirmarEliminar(a)
            }
        }.show()
    }

    private fun registrarBaja(a: Animal, t: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val mov = Movimiento(animalId = a.id, tipo = t, categoria = a.categoria, fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()), cantidad = 1, motivo = t, especie = "Bovino")
            db.movimientoDao().registrar(mov)
            animalDao.actualizar(a.copy(status = if (t == "Venta") "Vendido" else t))
            cargar()
        }
    }

    private fun confirmarEliminar(a: Animal) {
        AlertDialog.Builder(requireContext()).setTitle("Eliminar").setMessage("¿Borrar historial?").setPositiveButton("Sí") { _, _ ->
            lifecycleScope.launch { animalDao.eliminar(a); cargar() }
        }.show()
    }
}

class ResumenGlobalFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View? =
        inflater.inflate(R.layout.fragment_resumen_global, container, false)

    override fun onResume() {
        super.onResume()
        if (!isAdded) return
        lifecycleScope.launch {
            val lista = AppDatabase.getDatabase(requireContext()).animalDao().obtenerTodosActivos()
            view?.findViewById<TextView>(R.id.tvTotalAnimales)?.text = "Total: ${lista.size}"
            view?.findViewById<RecyclerView>(R.id.recyclerViewResumenAnimales)?.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = ResumenAnimalesAdapter(lista)
            }
        }
    }
}

class ActividadesFragment : Fragment() {
    private var todas = mutableListOf<ActividadReciente>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View? =
        inflater.inflate(R.layout.fragment_actividades_historial, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TabLayout>(R.id.tabLayoutFiltro).addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(t: TabLayout.Tab?) { filtrar(t?.position ?: 0) }
            override fun onTabUnselected(t: TabLayout.Tab?) {}
            override fun onTabReselected(t: TabLayout.Tab?) {}
        })
        cargar()
    }

    private fun cargar() {
        if (!isAdded) return
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val movs = db.movimientoDao().obtenerTodos()
            val animals = db.animalDao().obtenerTodosConBajas().associateBy { it.id }
            todas.clear()
            movs.forEach { m ->
                val a = animals[m.animalId]
                val f = when(m.tipo) { 
                    "Nacimiento" -> 2
                    "Muerte", "Venta", "Salida" -> 3 
                    else -> 0 
                }
                todas.add(ActividadReciente(m.fecha, "${m.tipo}: ${a?.nombre ?: "Lote"}", "Motivo: ${m.motivo}", f))
            }
            filtrar(0)
        }
    }

    private fun filtrar(p: Int) {
        if (!isAdded) return
        view?.findViewById<RecyclerView>(R.id.recyclerViewActividades)?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ActividadesAdapter(todas.filter { it.tipoFiltro == p })
        }
    }
}
