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

class AgregarMovimientoOvinoActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ¡USAMOS EL DISEÑO V2! (Limpio y sin errores)
        setContentView(R.layout.activity_agregar_movimiento_ovino_v2)

        val database = AppDatabase.getDatabase(applicationContext)
        movimientoDao = database.movimientoDao()

        // Referencias a los NUEVOS IDs (terminan en V2)
        val etTipo: EditText = findViewById(R.id.etTipoMovimientoOvinoV2)
        val etCategoria: EditText = findViewById(R.id.etCategoriaOvinoV2)
        val etFecha: EditText = findViewById(R.id.etFechaOvinoV2)
        val etCantidad: EditText = findViewById(R.id.etCantidadOvinoV2)
        val etMotivo: EditText = findViewById(R.id.etMotivoOvinoV2)
        val btnGuardar: Button = findViewById(R.id.btnGuardarOvinoV2)

        val opcionesMovimiento = arrayOf(
            "Nacimiento", "Compra", "Venta", "Muerte", 
            "Entrada", "Salida", "Trabajo", "Otro"
        )

        etTipo.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Selecciona el tipo de movimiento")
                .setItems(opcionesMovimiento) { _, which ->
                    etTipo.setText(opcionesMovimiento[which])
                }
                .show()
        }

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val tipo = etTipo.text.toString()
            val categoria = etCategoria.text.toString()
            val fecha = etFecha.text.toString()
            val cantidadStr = etCantidad.text.toString()

            if (tipo.isEmpty()) {
                Toast.makeText(this, "Selecciona un tipo de movimiento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoria.isEmpty() || fecha.isEmpty() || cantidadStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad = cantidadStr.toInt()

            lifecycleScope.launch {
                try {
                    val movimiento = Movimiento(
                        animalId = null,
                        tipo = tipo,
                        categoria = categoria,
                        fecha = fecha,
                        cantidad = cantidad,
                        motivo = etMotivo.text.toString(),
                        especie = "Ovino"
                    )
                    movimientoDao.registrar(movimiento)

                    runOnUiThread {
                        Toast.makeText(this@AgregarMovimientoOvinoActivity, "Movimiento de Ovino registrado", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@AgregarMovimientoOvinoActivity, OvinosMainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@AgregarMovimientoOvinoActivity, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
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