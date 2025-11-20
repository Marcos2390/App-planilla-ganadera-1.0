package com.example.planillarural

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegistroOvinosActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao
    private lateinit var sanidadDao: SanidadDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_ovinos)

        val database = AppDatabase.getDatabase(applicationContext)
        movimientoDao = database.movimientoDao()
        sanidadDao = database.sanidadDao()

        // Referencias a los campos
        val spinnerTipo: Spinner = findViewById(R.id.spinnerTipoOvino)
        val etTrabajoDetalle: EditText = findViewById(R.id.etTrabajoDetalle)
        val etFechaTrabajo: EditText = findViewById(R.id.etFechaTrabajo)
        val etCategoria: EditText = findViewById(R.id.etCategoriaOvino)
        val etCantidad: EditText = findViewById(R.id.etCantidadOvino)
        val etProducto: EditText = findViewById(R.id.etProductoOvino)
        val etDosis: EditText = findViewById(R.id.etDosisOvino)
        
        val btnGuardar: Button = findViewById(R.id.btnGuardarOvino)
        val btnCancelar: Button = findViewById(R.id.btnCancelarOvino)

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val tipoMovimiento = spinnerTipo.selectedItem.toString()
            val detalleTrabajo = etTrabajoDetalle.text.toString()
            val fecha = etFechaTrabajo.text.toString()
            val categoria = etCategoria.text.toString()
            val cantidadStr = etCantidad.text.toString()
            val producto = etProducto.text.toString()
            val dosis = etDosis.text.toString()

            // Si se selecciona "Otro", el detalle es obligatorio
            if (tipoMovimiento.equals("Otro", ignoreCase = true) && detalleTrabajo.isEmpty()) {
                Toast.makeText(this, "Especifica el detalle del trabajo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Campos básicos siempre obligatorios
            if (fecha.isEmpty() || categoria.isEmpty() || cantidadStr.isEmpty()) {
                Toast.makeText(this, "Completa Fecha, Categoría y Cantidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad = cantidadStr.toInt()
            // Si es un trabajo, no afecta al stock (cantidad 0), si es movimiento, sí.
            val cantidadStock = if(tipoMovimiento.equals("Otro", ignoreCase = true)) 0 else cantidad
            val tipoFinal = if(tipoMovimiento.equals("Otro", ignoreCase = true)) "Trabajo" else tipoMovimiento

            lifecycleScope.launch {
                // 1. Registrar el Movimiento (Stock y/o Trabajo)
                val movimiento = Movimiento(
                    animalId = null,
                    tipo = tipoFinal,
                    categoria = categoria,
                    fecha = fecha,
                    cantidad = cantidadStock,
                    motivo = detalleTrabajo,
                    especie = "Ovino"
                )
                movimientoDao.registrar(movimiento)

                // 2. Registrar Sanidad (si se completó)
                if (producto.isNotEmpty()) {
                    val sanidad = Sanidad(
                        animalId = null,
                        fecha = fecha,
                        tratamiento = tipoFinal, // O `detalleTrabajo` si prefieres
                        producto = producto,
                        dosis = dosis,
                        especie = "Ovino",
                        cantidad = cantidad // Aquí guardamos a cuántos se aplicó
                    )
                    sanidadDao.registrar(sanidad)
                }

                runOnUiThread {
                    Toast.makeText(this@RegistroOvinosActivity, "Registro Ovino Guardado", Toast.LENGTH_SHORT).show()
                    etTrabajoDetalle.setText("")
                    etCategoria.setText("")
                    etCantidad.setText("")
                    etProducto.setText("")
                    etDosis.setText("")
                }
            }
        }

        btnCancelar.setOnClickListener {
            finish()
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