package com.example.planillarural

import android.os.Bundle
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

        sanidadDao = AppDatabase.getDatabase(applicationContext).sanidadDao()
        animalId = intent.getIntExtra("ANIMAL_ID", -1)

        val etTratamiento: EditText = findViewById(R.id.etTratamiento)
        val etProducto: EditText = findViewById(R.id.etProducto)
        val etDosis: EditText = findViewById(R.id.etDosis)
        val etFecha: EditText = findViewById(R.id.etFechaSanidad)
        val btnGuardar: Button = findViewById(R.id.btnGuardarSanidad)
        val btnCancelar: Button = findViewById(R.id.btnCancelarSanidad)

        btnGuardar.setOnClickListener {
            val tratamiento = etTratamiento.text.toString()
            val producto = etProducto.text.toString()
            val dosis = etDosis.text.toString()
            val fecha = etFecha.text.toString()

            if (tratamiento.isEmpty() || producto.isEmpty() || dosis.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (animalId == -1) {
                Toast.makeText(this, "Error: ID de animal no encontrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoRegistro = Sanidad(
                animalId = animalId,
                tratamiento = tratamiento,
                producto = producto,
                dosis = dosis,
                fecha = fecha
            )

            lifecycleScope.launch {
                sanidadDao.registrar(nuevoRegistro)
            }

            Toast.makeText(this, "Registro de sanidad guardado", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}