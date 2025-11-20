package com.example.planillarural

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AgregarAnotacionActivity : AppCompatActivity() {

    private lateinit var anotacionDao: AnotacionDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_anotacion)

        val database = AppDatabase.getDatabase(applicationContext)
        anotacionDao = database.anotacionDao()

        val etTitulo: EditText = findViewById(R.id.etTituloAnotacion)
        val etDescripcion: EditText = findViewById(R.id.etDescripcionAnotacion)
        val rgTipo: RadioGroup = findViewById(R.id.rgTipoAnotacion)
        val layoutMonto: LinearLayout = findViewById(R.id.layoutMonto)
        val etMonto: EditText = findViewById(R.id.etMontoAnotacion)
        val spinnerMoneda: Spinner = findViewById(R.id.spinnerMoneda)
        val etFecha: EditText = findViewById(R.id.etFechaAnotacion)
        val btnGuardar: Button = findViewById(R.id.btnGuardarAnotacion)

        // Mostrar/Ocultar campo de monto según el tipo
        rgTipo.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbNota) {
                layoutMonto.visibility = View.GONE
            } else {
                layoutMonto.visibility = View.VISIBLE
            }
        }

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val titulo = etTitulo.text.toString()
            val descripcion = etDescripcion.text.toString()
            val fecha = etFecha.text.toString()
            val selectedId = rgTipo.checkedRadioButtonId

            if (titulo.isEmpty() || fecha.isEmpty() || selectedId == -1) {
                Toast.makeText(this, "Completa Título, Tipo y Fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var tipo = "Nota"
            var monto = 0.0
            var moneda = "ARS"

            when (selectedId) {
                R.id.rbIngreso -> tipo = "Ingreso"
                R.id.rbGasto -> tipo = "Gasto"
                R.id.rbNota -> tipo = "Nota"
            }

            if (tipo != "Nota") {
                val montoStr = etMonto.text.toString()
                if (montoStr.isEmpty()) {
                    Toast.makeText(this, "Ingresa el monto", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                monto = montoStr.toDouble()
                val monedaSeleccionada = spinnerMoneda.selectedItem.toString()
                moneda = if (monedaSeleccionada.contains("Dólares")) "USD" else "ARS"
            }

            lifecycleScope.launch {
                val nuevaAnotacion = Anotacion(
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = fecha,
                    tipo = tipo,
                    monto = monto,
                    moneda = moneda
                )
                anotacionDao.insertar(nuevaAnotacion)

                runOnUiThread {
                    Toast.makeText(this@AgregarAnotacionActivity, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                    finish() // Volver a la lista
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