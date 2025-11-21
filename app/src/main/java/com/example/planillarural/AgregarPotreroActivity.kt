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

class AgregarPotreroActivity : AppCompatActivity() {

    private lateinit var potreroDao: PotreroDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_potrero)

        val database = AppDatabase.getDatabase(applicationContext)
        potreroDao = database.potreroDao()

        val etNombre = findViewById<EditText>(R.id.etNombrePotrero)
        val etHectareas = findViewById<EditText>(R.id.etHectareas)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcionPotrero)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarPotrero)

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val nombre = etNombre.text.toString()
            val hectareasStr = etHectareas.text.toString()
            val descripcion = etDescripcion.text.toString()

            if (nombre.isEmpty() || hectareasStr.isEmpty()) {
                Toast.makeText(this, "Nombre y Hectáreas son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hectareas = hectareasStr.toDouble()

            lifecycleScope.launch {
                val nuevoPotrero = Potrero(
                    nombre = nombre,
                    hectareas = hectareas,
                    descripcion = descripcion
                )
                potreroDao.insertarPotrero(nuevoPotrero)

                runOnUiThread {
                    Toast.makeText(this@AgregarPotreroActivity, "Potrero guardado", Toast.LENGTH_SHORT).show()
                    finish()
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