package com.example.planillarural

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class AgregarAnimalActivity : AppCompatActivity() {

    // 1. Declarar el ViewModel. Se inicializará en onCreate.
    private lateinit var registroViewModel: RegistroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_animal)

        // --- TODA LA INICIALIZACIÓN DEBE ESTAR AQUÍ, EN ONCREATE ---

        // 2. Inicializar el ViewModel UNA SOLA VEZ cuando se crea la pantalla.
        val database = AppDatabase.getDatabase(applicationContext)
        val viewModelFactory = RegistroViewModelFactory(database)
        registroViewModel = ViewModelProvider(this, viewModelFactory)[RegistroViewModel::class.java]

        // 3. Encontrar todas las vistas UNA SOLA VEZ.
        val txtCaravana = findViewById<EditText>(R.id.etNumeroCaravana)
        val txtCategoria = findViewById<EditText>(R.id.etCategoria)
        val txtRaza = findViewById<EditText>(R.id.etRaza)
        val txtEdad = findViewById<EditText>(R.id.etEdad)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        // 4. Configurar el listener del botón de guardar.
        btnGuardar.setOnClickListener {
            // Esta es la lógica que se ejecuta solo al hacer clic.
            val nombre = txtCaravana.text.toString()
            val categoria = txtCategoria.text.toString()
            val raza = txtRaza.text.toString()
            val fechaNac = txtEdad.text.toString()

            // Validar que los campos no estén vacíos
            if (nombre.isEmpty() || categoria.isEmpty() || raza.isEmpty() || fechaNac.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear el objeto Animal
            val nuevoAnimal = Animal(
                nombre = nombre,
                categoria = categoria,
                raza = raza,
                fechaNac = fechaNac
            )

            // Usar el ViewModel (que ya fue creado) para guardar el animal
            registroViewModel.registrarAnimal(nuevoAnimal)

            // Mostrar confirmación y cerrar la pantalla
            Toast.makeText(this, "Animal agregado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 5. Configurar el listener del botón de cancelar.
        btnCancelar.setOnClickListener {
            finish()
        }
    }
}
