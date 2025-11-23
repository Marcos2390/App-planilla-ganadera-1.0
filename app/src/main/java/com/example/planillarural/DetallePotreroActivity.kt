package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton // CORREGIDO: Volvemos a usar Extended
import kotlinx.coroutines.launch

class DetallePotreroActivity : AppCompatActivity() {

    private lateinit var potreroDao: PotreroDao
    private lateinit var animalDao: AnimalDao
    
    private var potreroId: Int = -1
    private var potreroActual: Potrero? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_potrero)
        
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            potreroDao = database.potreroDao()
            animalDao = database.animalDao()

            potreroId = intent.getIntExtra("POTRERO_ID", -1)
            if (potreroId == -1) {
                Toast.makeText(this, "Error: ID de potrero no encontrado", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // CORREGIDO: Usamos ExtendedFloatingActionButton porque el XML ahora los tiene
            val fabLote = findViewById<ExtendedFloatingActionButton>(R.id.fabAgregarLote)
            val fabBovino = findViewById<ExtendedFloatingActionButton>(R.id.fabAsignarBovino)

            fabLote.setOnClickListener { mostrarDialogoNuevoLote() }
            fabBovino.setOnClickListener {
                val intent = Intent(this, SeleccionarAnimalesActivity::class.java)
                intent.putExtra("MODO_MOVER_POTRERO", true)
                intent.putExtra("POTRERO_DESTINO_ID", potreroId)
                startActivity(intent)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error CRÍTICO al iniciar: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (potreroId != -1) cargarDatosCompletos()
    }

    private fun cargarDatosCompletos() {
        lifecycleScope.launch {
            try {
                // Cargar datos del potrero
                potreroActual = potreroDao.obtenerPorId(potreroId)
                if (potreroActual == null) {
                    Toast.makeText(this@DetallePotreroActivity, "Error: No se pudo cargar el potrero.", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }

                val tvTitulo = findViewById<TextView>(R.id.tvTituloDetallePotrero)
                val tvSubtitulo = findViewById<TextView>(R.id.tvSubtituloHectareas)
                tvTitulo.text = potreroActual!!.nombre
                tvSubtitulo.text = "${potreroActual!!.hectareas} Ha - ${potreroActual!!.descripcion}"

                // Cargar listas
                cargarListas()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DetallePotreroActivity, "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarListas() {
        lifecycleScope.launch {
            // Cargar Bovinos
            val animalesEnPotrero = animalDao.obtenerTodosActivos().filter { it.potreroId == potreroId }
            val rvBovinos = findViewById<RecyclerView>(R.id.rvBovinosEnPotrero)
            rvBovinos.layoutManager = LinearLayoutManager(this@DetallePotreroActivity)
            rvBovinos.adapter = AnimalEnPotreroAdapter(animalesEnPotrero) { animal ->
                mostrarDialogoSacarBovino(animal)
            }

            // Cargar Lotes
            val lotes = potreroDao.obtenerLotesDePotrero(potreroId)
            val rvLotes = findViewById<RecyclerView>(R.id.rvLotesEnPotrero)
            rvLotes.layoutManager = LinearLayoutManager(this@DetallePotreroActivity)
            rvLotes.adapter = LoteAdapter(lotes) { lote ->
                confirmarBorrarLote(lote)
            }
        }
    }
    
    private fun mostrarDialogoNuevoLote() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nuevo Lote Manual")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputEspecie = EditText(this)
        inputEspecie.hint = "Especie (Ej: Ovinos)"
        layout.addView(inputEspecie)

        val inputCategoria = EditText(this)
        inputCategoria.hint = "Categoría (Ej: Ovejas)"
        layout.addView(inputCategoria)

        val inputCantidad = EditText(this)
        inputCantidad.hint = "Cantidad (Ej: 50)"
        inputCantidad.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(inputCantidad)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val especie = inputEspecie.text.toString()
            val categoria = inputCategoria.text.toString()
            val cantidadStr = inputCantidad.text.toString()

            if (especie.isNotEmpty() && cantidadStr.isNotEmpty()) {
                lifecycleScope.launch {
                    val lote = LotePotrero(
                        potreroId = potreroId,
                        especie = especie,
                        categoria = categoria,
                        cantidad = cantidadStr.toInt()
                    )
                    potreroDao.insertarLote(lote)
                    cargarListas()
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
    
    private fun confirmarBorrarLote(lote: LotePotrero) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Lote")
            .setMessage("¿Borrar este lote de ${lote.cantidad} ${lote.especie}?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    potreroDao.eliminarLote(lote)
                    cargarListas()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoSacarBovino(animal: Animal) {
        AlertDialog.Builder(this)
            .setTitle("Sacar del Potrero")
            .setMessage("¿Quitar a ${animal.nombre} de este potrero? (Volverá a estar 'Sin Asignar')")
            .setPositiveButton("Sacar") { _, _ ->
                lifecycleScope.launch {
                    val animalActualizado = animal.copy(potreroId = null)
                    animalDao.actualizar(animalActualizado)
                    cargarListas()
                    Toast.makeText(this@DetallePotreroActivity, "Animal retirado del potrero", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}