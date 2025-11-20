package com.example.planillarural

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegistrarSanidadOvinoActivity : AppCompatActivity() {

    private lateinit var sanidadDao: SanidadDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_sanidad_ovino)

        val database = AppDatabase.getDatabase(applicationContext)
        sanidadDao = database.sanidadDao()

        val etTratamiento: EditText = findViewById(R.id.etTratamiento)
        val etProducto: EditText = findViewById(R.id.etProducto)
        val etDosis: EditText = findViewById(R.id.etDosis)
        val etFecha: EditText = findViewById(R.id.etFechaSanidad)
        val btnGuardar: Button = findViewById(R.id.btnGuardarSanidadOvino)

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val tratamiento = etTratamiento.text.toString()
            val producto = etProducto.text.toString()
            val dosis = etDosis.text.toString()
            val fecha = etFecha.text.toString()

            if (tratamiento.isEmpty() || producto.isEmpty() || dosis.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val sanidad = Sanidad(
                    animalId = null, // Es un registro de lote, no de animal individual
                    fecha = fecha,
                    tratamiento = tratamiento,
                    producto = producto,
                    dosis = dosis,
                    especie = "Ovino" // ¡Importante!
                )
                sanidadDao.registrar(sanidad)

                runOnUiThread {
                    Toast.makeText(this@RegistrarSanidadOvinoActivity, "Sanidad de lote guardada", Toast.LENGTH_SHORT).show()
                    // Volver a la pantalla de Ovinos
                    val intent = Intent(this@RegistrarSanidadOvinoActivity, OvinosMainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
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