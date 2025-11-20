package com.example.planillarural

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegistrarTrabajoOvinoActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_trabajo_ovino)

        val database = AppDatabase.getDatabase(applicationContext)
        movimientoDao = database.movimientoDao()

        val etNombreTrabajo: EditText = findViewById(R.id.etNombreTrabajo)
        val etFechaTrabajo: EditText = findViewById(R.id.etFechaTrabajo)
        val btnGuardar: Button = findViewById(R.id.btnGuardarTrabajo)

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val nombreTrabajo = etNombreTrabajo.text.toString()
            val fecha = etFechaTrabajo.text.toString()

            if (nombreTrabajo.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Creamos un movimiento con cantidad 0, ya que un trabajo no afecta el stock
                val trabajo = Movimiento(
                    animalId = null,
                    tipo = "Trabajo", // Tipo general
                    categoria = "", // No aplica a un trabajo general
                    fecha = fecha,
                    cantidad = 0, 
                    motivo = nombreTrabajo, // El nombre del trabajo va como motivo
                    especie = "Ovino"
                )
                movimientoDao.registrar(trabajo)

                runOnUiThread {
                    Toast.makeText(this@RegistrarTrabajoOvinoActivity, "Trabajo registrado", Toast.LENGTH_SHORT).show()
                    etNombreTrabajo.setText("")
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