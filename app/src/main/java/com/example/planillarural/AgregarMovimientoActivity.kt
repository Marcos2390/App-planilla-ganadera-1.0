package com.example.planillarural

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AgregarMovimientoActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao
    private var animalId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_movimiento)

        animalId = intent.getIntExtra("ANIMAL_ID", -1)
        if (animalId == -1) {
            Toast.makeText(this, "Error: ID de animal no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        movimientoDao = AppDatabase.getDatabase(applicationContext).movimientoDao()

        val etTipo: EditText = findViewById(R.id.etTipoMovimiento)
        val etFecha: EditText = findViewById(R.id.etFechaMovimiento)
        val etCantidad: EditText = findViewById(R.id.etCantidad)
        val etMotivo: EditText = findViewById(R.id.etMotivo)
        val btnGuardar: Button = findViewById(R.id.btnGuardarMovimiento)
        val btnCancelar: Button = findViewById(R.id.btnCancelarMovimiento)

        btnGuardar.setOnClickListener {
            val tipo = etTipo.text.toString()
            val fecha = etFecha.text.toString()
            val cantidadStr = etCantidad.text.toString()
            val motivo = etMotivo.text.toString()

            if (tipo.isEmpty() || fecha.isEmpty() || cantidadStr.isEmpty() || motivo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoMovimiento = Movimiento(
                animalId = animalId, // ¡AÑADIDO!
                tipo = tipo,
                fecha = fecha,
                cantidad = cantidadStr.toInt(),
                motivo = motivo
            )

            lifecycleScope.launch {
                movimientoDao.registrar(nuevoMovimiento)
            }

            Toast.makeText(this, "Movimiento agregado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}