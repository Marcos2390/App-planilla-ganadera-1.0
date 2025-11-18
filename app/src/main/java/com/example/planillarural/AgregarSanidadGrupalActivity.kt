package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AgregarSanidadGrupalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_sanidad_grupal)

        val etTratamiento: EditText = findViewById(R.id.etTratamiento)
        val etProducto: EditText = findViewById(R.id.etProducto)
        val etDosis: EditText = findViewById(R.id.etDosis)
        val etFecha: EditText = findViewById(R.id.etFechaSanidad)
        val etFechaProximaDosis: EditText = findViewById(R.id.etFechaProximaDosis) // ¡NUEVO!
        val btnSeleccionar: Button = findViewById(R.id.btnSeleccionarAnimales)

        btnSeleccionar.setOnClickListener {
            val tratamiento = etTratamiento.text.toString()
            val producto = etProducto.text.toString()
            val dosis = etDosis.text.toString()
            val fecha = etFecha.text.toString()
            val fechaProxima = etFechaProximaDosis.text.toString() // ¡NUEVO!

            if (tratamiento.isEmpty() || producto.isEmpty() || dosis.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SeleccionarAnimalesActivity::class.java).apply {
                putExtra("TRATAMIENTO", tratamiento)
                putExtra("PRODUCTO", producto)
                putExtra("DOSIS", dosis)
                putExtra("FECHA", fecha)
                putExtra("FECHA_PROXIMA", fechaProxima) // ¡NUEVO!
            }
            startActivity(intent)
        }
    }
}