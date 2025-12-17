package com.example.planillarural

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AgregarMovimientoActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao
    private lateinit var nacimientoPendienteDao: NacimientoPendienteDao
    private lateinit var animalDao: AnimalDao // AGREGADO
    private var especie: String = "Bovino"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_movimiento_v2)

        val database = AppDatabase.getDatabase(applicationContext)
        movimientoDao = database.movimientoDao()
        nacimientoPendienteDao = database.nacimientoPendienteDao()
        animalDao = database.animalDao() // INICIALIZADO

        especie = intent.getStringExtra("ESPECIE") ?: "Bovino"

        val etTipo: EditText = findViewById(R.id.etTipoMovimientoV2) 
        val etCategoria: EditText = findViewById(R.id.etCategoriaMovimientoV2)
        val etFecha: EditText = findViewById(R.id.etFechaMovimientoV2)
        val etCantidad: EditText = findViewById(R.id.etCantidadV2)
        val etMotivo: EditText = findViewById(R.id.etMotivoV2)
        val etIdCaravana: EditText = findViewById(R.id.etIdCaravanaV2)
        val btnGuardar: Button = findViewById(R.id.btnGuardarMovimientoV2)

        val opcionesMovimiento = arrayOf(
            "Nacimiento", "Compra", "Venta", "Muerte", 
            "Entrada", "Salida", "Trabajo", "Otro"
        )

        etTipo.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Selecciona el tipo de movimiento")
                .setItems(opcionesMovimiento) { _, which ->
                    val seleccion = opcionesMovimiento[which]
                    etTipo.setText(seleccion)

                    // Si es Nacimiento, autocompletar y bloquear categoría
                    if (seleccion == "Nacimiento") {
                        etCategoria.setText("Ternero/a")
                        etCategoria.isEnabled = false
                    } else {
                        etCategoria.isEnabled = true
                        etCategoria.setText("")
                    }
                }
                .show()
        }

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val tipo = etTipo.text.toString()
            val categoria = etCategoria.text.toString()
            val fecha = etFecha.text.toString()
            val cantidadStr = etCantidad.text.toString()
            val caravana = etIdCaravana.text.toString()

            if (tipo.isEmpty()) {
                Toast.makeText(this, "Selecciona un tipo de movimiento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoria.isEmpty() || fecha.isEmpty() || cantidadStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad = cantidadStr.toInt()

            // Lógica para agregar la caravana al motivo si existe
            var motivoFinal = etMotivo.text.toString()
            if (caravana.isNotEmpty()) {
                motivoFinal = if (motivoFinal.isNotEmpty()) {
                    "$motivoFinal (ID: $caravana)"
                } else {
                    "ID: $caravana"
                }
            }

            lifecycleScope.launch {
                val movimiento = Movimiento(
                    animalId = null,
                    tipo = tipo,
                    categoria = categoria,
                    fecha = fecha,
                    cantidad = cantidad,
                    motivo = motivoFinal,
                    especie = especie
                )
                movimientoDao.registrar(movimiento)

                if (tipo == "Nacimiento" && especie == "Bovino") {
                    val nacimientoPendiente = NacimientoPendiente(fecha = fecha, categoria = categoria, cantidadTotal = cantidad)
                    nacimientoPendienteDao.insertar(nacimientoPendiente)
                }

                // LÓGICA NUEVA: Crear animales si es Compra/Entrada y hay caravana
                if ((tipo == "Compra" || tipo == "Entrada") && caravana.isNotEmpty()) {
                    val listaCaravanas = caravana.split(",", " ", ";", "\n").filter { it.isNotBlank() }
                    for (idCaravana in listaCaravanas) {
                        // Verificar duplicados simples en memoria
                        val existentes = animalDao.buscarPorNombre(idCaravana)
                        val yaExiste = existentes.any { it.nombre.equals(idCaravana, ignoreCase = true) && it.status == "Activo" }
                        
                        if (!yaExiste) {
                            val nuevoAnimal = Animal(
                                nombre = idCaravana,
                                categoria = categoria,
                                raza = "A definir", // Marcador para que el usuario edite
                                fechaNac = fecha,   // Usamos fecha de ingreso como referencia
                                status = "Activo",
                                especie = especie,
                                informacionAdicional = "Ingreso por $tipo"
                            )
                            animalDao.insertar(nuevoAnimal)
                        }
                    }
                }

                runOnUiThread {
                    Toast.makeText(this@AgregarMovimientoActivity, "Movimiento ($especie) registrado", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AgregarMovimientoActivity, ListaAnimalesActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}