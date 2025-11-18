package com.example.planillarural

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AgregarMovimientoActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao
    private lateinit var nacimientoPendienteDao: NacimientoPendienteDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_movimiento)

        val database = AppDatabase.getDatabase(applicationContext)
        movimientoDao = database.movimientoDao()
        nacimientoPendienteDao = database.nacimientoPendienteDao()

        val spinnerTipo: Spinner = findViewById(R.id.spinnerTipoMovimiento)
        val etCategoria: EditText = findViewById(R.id.etCategoriaMovimiento)
        val etFecha: EditText = findViewById(R.id.etFechaMovimiento)
        val etCantidad: EditText = findViewById(R.id.etCantidad)
        val etMotivo: EditText = findViewById(R.id.etMotivo)
        val btnGuardar: Button = findViewById(R.id.btnGuardarMovimiento)
        val btnCancelar: Button = findViewById(R.id.btnCancelarMovimiento)

        btnGuardar.setOnClickListener {
            val tipo = spinnerTipo.selectedItem.toString()
            val categoria = etCategoria.text.toString()
            val fecha = etFecha.text.toString()
            val cantidadStr = etCantidad.text.toString()

            if (categoria.isEmpty() || fecha.isEmpty() || cantidadStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad = cantidadStr.toInt()

            lifecycleScope.launch {
                val movimiento = Movimiento(
                    animalId = null,
                    tipo = tipo,
                    categoria = categoria,
                    fecha = fecha,
                    cantidad = cantidad,
                    motivo = etMotivo.text.toString()
                )
                movimientoDao.registrar(movimiento)

                if (tipo == "Nacimiento") {
                    val nacimientoPendiente = NacimientoPendiente(fecha = fecha, categoria = categoria, cantidadTotal = cantidad)
                    nacimientoPendienteDao.insertar(nacimientoPendiente)
                }

                runOnUiThread {
                    Toast.makeText(this@AgregarMovimientoActivity, "Movimiento registrado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}