package com.example.planillarural

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
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
        val etProximaDosis: EditText = findViewById(R.id.etFechaProximaDosis)
        val btnSiguiente: Button = findViewById(R.id.btnSeleccionarAnimales)

        btnSiguiente.setOnClickListener {
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

            val intent = Intent(this, SeleccionarAnimalesActivity::class.java)
            intent.putExtra("TRATAMIENTO", tratamiento)
            intent.putExtra("PRODUCTO", producto)
            intent.putExtra("DOSIS", dosis)
            intent.putExtra("FECHA", fecha)
            intent.putExtra("FECHA_PROXIMA", proximaDosis)
            startActivity(intent)
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