// Contenido CORRECTO para MainActivity.kt
package com.example.planillarural

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var registroViewModel: RegistroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_animal)

        val database = PlanillaDB.getDatabase(applicationContext)
        val viewModelFactory = RegistroViewModelFactory(database)
        registroViewModel = ViewModelProvider(this, viewModelFactory)[RegistroViewModel::class.java]

        val txtCaravana = findViewById<EditText>(R.id.etNumeroCaravana)
        val txtCategoria = findViewById<EditText>(R.id.etCategoria)
        val txtRaza = findViewById<EditText>(R.id.etRaza)
        val txtEdad = findViewById<EditText>(R.id.etEdad)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        btnGuardar.setOnClickListener {
            val nombre = txtCaravana.text.toString()
            val categoria = txtCategoria.text.toString()
            val raza = txtRaza.text.toString()
            val fechaNac = txtEdad.text.toString()

            if (nombre.isEmpty() || categoria.isEmpty() || raza.isEmpty() || fechaNac.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoAnimal = Animal(
                nombre = nombre,
                categoria = categoria,
                raza = raza,
                fechaNac = fechaNac
            )

            registroViewModel.registrarAnimal(nuevoAnimal)

            Toast.makeText(this, "Animal agregado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}