package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class OvinosMainActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao
    private lateinit var tvResumenOvinos: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_ovinos_main)

            val database = AppDatabase.getDatabase(applicationContext)
            movimientoDao = database.movimientoDao()

            tvResumenOvinos = findViewById(R.id.tvResumenOvinos)
            
            val btnMovimiento = findViewById<Button>(R.id.btn_ovinos_movimiento)
            val btnSanidad = findViewById<Button>(R.id.btn_ovinos_sanidad)
            val btnHistorial = findViewById<Button>(R.id.btn_ovinos_historial)

            btnMovimiento.setOnClickListener {
                try {
                    startActivity(Intent(this, AgregarMovimientoOvinoActivity::class.java))
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al abrir Movimiento: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            btnSanidad.setOnClickListener {
                try {
                    startActivity(Intent(this, RegistrarSanidadOvinoActivity::class.java))
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al abrir Sanidad: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            btnHistorial.setOnClickListener {
                try {
                    startActivity(Intent(this, HistorialOvinosActivity::class.java))
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al abrir Historial: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            // Intentamos cargar el resumen, pero si falla, NO cerramos la app
            cargarResumenStockSeguro()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error crítico en Ovinos: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // Volver atrás suavemente en lugar de crashear
        }
    }

    override fun onResume() {
        super.onResume()
        cargarResumenStockSeguro()
    }

    private fun cargarResumenStockSeguro() {
        lifecycleScope.launch {
            try {
                // Verificamos si la tabla movimientos existe y tiene la columna especie
                val movimientos = movimientoDao.obtenerTodos().filter { it.especie == "Ovino" }
                
                var total = 0
                val categorias = mutableMapOf<String, Int>()

                for (mov in movimientos) {
                    val tipo = mov.tipo
                    val esEntrada = tipo.equals("Nacimiento", true) || tipo.equals("Compra", true) || tipo.equals("Entrada", true)
                    val esSalida = tipo.equals("Venta", true) || tipo.equals("Muerte", true) || tipo.equals("Salida", true)
                    
                    if (esEntrada) {
                        total += mov.cantidad
                        categorias[mov.categoria] = (categorias[mov.categoria] ?: 0) + mov.cantidad
                    } else if (esSalida) {
                        total -= mov.cantidad
                        categorias[mov.categoria] = (categorias[mov.categoria] ?: 0) - mov.cantidad
                    }
                }

                val resumenCategorias = categorias.entries.filter { it.value > 0 }.joinToString("\n") { "${it.key}: ${it.value}" }
                tvResumenOvinos.text = if (total == 0) "Sin Stock Registrado" else "Total Ovinos: $total\n\n$resumenCategorias"
            } catch (e: Exception) {
                e.printStackTrace()
                tvResumenOvinos.text = "Error al cargar stock (Base de Datos). Puedes seguir operando."
            }
        }
    }
}