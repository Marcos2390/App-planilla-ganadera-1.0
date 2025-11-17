package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AgregarAnimalActivity : AppCompatActivity() {

    private lateinit var registroViewModel: RegistroViewModel
    private var animalId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_animal)

        val database = AppDatabase.getDatabase(applicationContext)
        val viewModelFactory = RegistroViewModelFactory(database)
        registroViewModel = ViewModelProvider(this, viewModelFactory)[RegistroViewModel::class.java]

        val txtCaravana = findViewById<EditText>(R.id.etNumeroCaravana)
        val txtCategoria = findViewById<EditText>(R.id.etCategoria)
        val txtRaza = findViewById<EditText>(R.id.etRaza)
        val txtEdad = findViewById<EditText>(R.id.etEdad)
        val txtInfoAdicional = findViewById<EditText>(R.id.etInformacionAdicional)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val btnVerSanidad = findViewById<Button>(R.id.btnVerSanidad)
        val btnVerMovimientos = findViewById<Button>(R.id.btnVerMovimientos)

        animalId = intent.getIntExtra("ANIMAL_ID", -1)

        if (animalId == -1) {
            btnVerSanidad.visibility = View.GONE
            btnVerMovimientos.visibility = View.GONE
        } else {
            btnVerSanidad.visibility = View.VISIBLE
            btnVerMovimientos.visibility = View.VISIBLE
            lifecycleScope.launch {
                val animal = registroViewModel.obtenerAnimalPorId(animalId)
                animal?.let {
                    txtCaravana.setText(it.nombre)
                    txtCategoria.setText(it.categoria)
                    txtRaza.setText(it.raza)
                    txtEdad.setText(it.fechaNac)
                    txtInfoAdicional.setText(it.informacionAdicional)
                }
            }
        }

        btnGuardar.setOnClickListener {
            val nombre = txtCaravana.text.toString()
            val categoria = txtCategoria.text.toString()
            val raza = txtRaza.text.toString()
            val fechaNac = txtEdad.text.toString()
            val infoAdicional = txtInfoAdicional.text.toString()

            if (nombre.isEmpty() || categoria.isEmpty() || raza.isEmpty() || fechaNac.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ¡CORRECCIÓN! Ahora nos aseguramos de incluir siempre la información adicional.
            val animal = Animal(
                id = if (animalId == -1) 0 else animalId,
                nombre = nombre,
                categoria = categoria,
                raza = raza,
                fechaNac = fechaNac,
                informacionAdicional = infoAdicional
            )

            registroViewModel.registrarAnimal(animal)

            Toast.makeText(this, "Animal guardado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnCancelar.setOnClickListener {
            finish()
        }

        btnVerSanidad.setOnClickListener {
            val intent = Intent(this, SanidadActivity::class.java)
            intent.putExtra("ANIMAL_ID", animalId)
            startActivity(intent)
        }

        btnVerMovimientos.setOnClickListener {
            val intent = Intent(this, MovimientosActivity::class.java)
            intent.putExtra("ANIMAL_ID", animalId)
            startActivity(intent)
        }
    }
}