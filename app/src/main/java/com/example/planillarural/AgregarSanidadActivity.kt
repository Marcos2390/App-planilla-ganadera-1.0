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

class AgregarSanidadActivity : AppCompatActivity() {

    private lateinit var sanidadDao: SanidadDao
    private var animalId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_sanidad)

        val database = AppDatabase.getDatabase(applicationContext)
        sanidadDao = database.sanidadDao()

        animalId = intent.getIntExtra("ANIMAL_ID", -1)

        val etTratamiento: EditText = findViewById(R.id.etTratamiento)
        val etProducto: EditText = findViewById(R.id.etProducto)
        val etDosis: EditText = findViewById(R.id.etDosis)
        val etFecha: EditText = findViewById(R.id.etFechaSanidad)
        val etProximaDosis: EditText = findViewById(R.id.etFechaProximaDosis)
        val btnGuardar: Button = findViewById(R.id.btnGuardarSanidad)
        val btnCancelar: Button = findViewById(R.id.btnCancelarSanidad)

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val tratamiento = etTratamiento.text.toString()
            val producto = etProducto.text.toString()
            val dosis = etDosis.text.toString()
            val fecha = etFecha.text.toString()
            val proximaDosis = etProximaDosis.text.toString()

            if (tratamiento.isEmpty() || producto.isEmpty() || dosis.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val sanidad = Sanidad(
                    animalId = if (animalId != -1) animalId else null,
                    fecha = fecha,
                    tratamiento = tratamiento,
                    producto = producto,
                    dosis = dosis,
                    fechaProximaDosis = if (proximaDosis.isNotEmpty()) proximaDosis else null,
                    especie = "Bovino"
                )
                sanidadDao.registrar(sanidad)

                runOnUiThread {
                    Toast.makeText(this@AgregarSanidadActivity, "Sanidad registrada", Toast.LENGTH_SHORT).show()
                    finish()
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