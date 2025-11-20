package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class OvinosMainActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao
    private lateinit var tvResumenOvinos: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ovinos_main)

        val database = AppDatabase.getDatabase(applicationContext)
        movimientoDao = database.movimientoDao()

        tvResumenOvinos = findViewById(R.id.tvResumenOvinos)
        
        val btnMovimiento = findViewById<Button>(R.id.btn_ovinos_movimiento)
        val btnSanidad = findViewById<Button>(R.id.btn_ovinos_sanidad)
        val btnHistorial = findViewById<Button>(R.id.btn_ovinos_historial)

        btnMovimiento.setOnClickListener {
            startActivity(Intent(this, AgregarMovimientoOvinoActivity::class.java))
        }

        btnSanidad.setOnClickListener {
            startActivity(Intent(this, RegistrarSanidadOvinoActivity::class.java))
        }

        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialOvinosActivity::class.java))
        }

        // Lógica de la Barra de Navegación ELIMINADA para que no de error

        cargarResumenStock()
    }

    override fun onResume() {
        super.onResume()
        cargarResumenStock()
        // Lógica de la Barra de Navegación ELIMINADA
    }

    private fun cargarResumenStock() {
        lifecycleScope.launch {
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
        }
    }
}