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
    private lateinit var animalDao: AnimalDao
    private var especie: String = "Bovino"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_movimiento_v2)

        val database = AppDatabase.getDatabase(applicationContext)
        movimientoDao = database.movimientoDao()
        nacimientoPendienteDao = database.nacimientoPendienteDao()
        animalDao = database.animalDao()

        especie = intent.getStringExtra("ESPECIE") ?: "Bovino"

        val etTipo: EditText = findViewById(R.id.etTipoMovimientoV2) 
        val etCategoria: EditText = findViewById(R.id.etCategoriaMovimientoV2)
        val etFecha: EditText = findViewById(R.id.etFechaMovimientoV2)
        val etCantidad: EditText = findViewById(R.id.etCantidadV2)
        val etMotivo: EditText = findViewById(R.id.etMotivoV2)
        val etIdCaravana: EditText = findViewById(R.id.etIdCaravanaV2)
        val btnGuardar: Button = findViewById(R.id.btnGuardarMovimientoV2)

        val opcionesMovimiento = arrayOf("Nacimiento", "Compra", "Venta", "Muerte", "Entrada", "Salida", "Trabajo", "Otro")

        etTipo.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Selecciona el tipo de movimiento")
                .setItems(opcionesMovimiento) { _, which ->
                    val seleccion = opcionesMovimiento[which]
                    etTipo.setText(seleccion)

                    if (seleccion == "Nacimiento") {
                        // Obligamos a elegir género para que la estadística sea exacta
                        val generos = arrayOf("Ternero (Macho)", "Ternera (Hembra)")
                        AlertDialog.Builder(this@AgregarMovimientoActivity)
                            .setTitle("Selecciona el Género")
                            .setItems(generos) { _, gWhich ->
                                etCategoria.setText(if (gWhich == 0) "Ternero" else "Ternera")
                            }
                            .setCancelable(false)
                            .show()
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

            if (tipo.isEmpty() || categoria.isEmpty() || fecha.isEmpty() || cantidadStr.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad = cantidadStr.toInt()
            var motivoFinal = etMotivo.text.toString()
            if (caravana.isNotEmpty()) {
                motivoFinal = if (motivoFinal.isNotEmpty()) "$motivoFinal (ID: $caravana)" else "ID: $caravana"
            }

            lifecycleScope.launch {
                val movimiento = Movimiento(animalId = null, tipo = tipo, categoria = categoria, fecha = fecha, cantidad = cantidad, motivo = motivoFinal, especie = especie)
                movimientoDao.registrar(movimiento)

                if (tipo == "Nacimiento" && especie == "Bovino") {
                    val nacimientoPendiente = NacimientoPendiente(fecha = fecha, categoria = categoria, cantidadTotal = cantidad)
                    nacimientoPendienteDao.insertar(nacimientoPendiente)
                }

                if ((tipo == "Compra" || tipo == "Entrada") && caravana.isNotEmpty()) {
                    val listaCaravanas = caravana.split(",", " ", ";", "\n").filter { it.isNotBlank() }
                    for (idCaravana in listaCaravanas) {
                        val existentes = animalDao.buscarPorNombre(idCaravana)
                        val yaExiste = existentes.any { it.nombre.equals(idCaravana, ignoreCase = true) && it.status == "Activo" }
                        if (!yaExiste) {
                            val nuevoAnimal = Animal(nombre = idCaravana, categoria = categoria, raza = "A definir", fechaNac = fecha, status = "Activo", especie = especie, informacionAdicional = "Ingreso por $tipo")
                            animalDao.insertar(nuevoAnimal)
                        }
                    }
                }

                runOnUiThread {
                    Toast.makeText(this@AgregarMovimientoActivity, "Registro guardado", Toast.LENGTH_SHORT).show()
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