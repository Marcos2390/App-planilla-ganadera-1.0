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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Conecta este código con su diseño XML
        setContentView(R.layout.activity_agregar_movimiento)

        // 2. Obtiene acceso a la base de datos a través del DAO de movimientos
        movimientoDao = AppDatabase.getDatabase(applicationContext).movimientoDao()

        // 3. Conecta los componentes del XML con variables de Kotlin
        val etTipo: EditText = findViewById(R.id.etTipoMovimiento)
        val etFecha: EditText = findViewById(R.id.etFechaMovimiento)
        val etCantidad: EditText = findViewById(R.id.etCantidad)
        val etMotivo: EditText = findViewById(R.id.etMotivo)
        val btnGuardar: Button = findViewById(R.id.btnGuardarMovimiento)
        val btnCancelar: Button = findViewById(R.id.btnCancelarMovimiento)

        // 4. Configura el listener del botón de guardar
        btnGuardar.setOnClickListener {
            // Se obtienen los datos de los campos de texto
            val tipo = etTipo.text.toString()
            val fecha = etFecha.text.toString()
            val cantidadStr = etCantidad.text.toString()
            val motivo = etMotivo.text.toString()

            // Validar que los campos no estén vacíos
            if (tipo.isEmpty() || fecha.isEmpty() || cantidadStr.isEmpty() || motivo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear el objeto Movimiento
            val nuevoMovimiento = Movimiento(
                tipo = tipo,
                fecha = fecha,
                cantidad = cantidadStr.toInt(), // Convertimos el texto a número
                motivo = motivo
            )

            // Usamos una Coroutine para guardar en la base de datos
            lifecycleScope.launch {
                movimientoDao.registrar(nuevoMovimiento)
            }

            // Mostrar confirmación y cerrar la pantalla
            Toast.makeText(this, "Movimiento agregado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 5. Configura el listener del botón de cancelar
        btnCancelar.setOnClickListener {
            finish()
        }
    }
}

