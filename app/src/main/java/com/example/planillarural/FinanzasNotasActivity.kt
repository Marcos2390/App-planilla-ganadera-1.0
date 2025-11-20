package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class FinanzasNotasActivity : AppCompatActivity() {

    private lateinit var anotacionDao: AnotacionDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvIngresosArs: TextView
    private lateinit var tvIngresosUsd: TextView
    private lateinit var tvGastosArs: TextView
    private lateinit var tvGastosUsd: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finanzas_notas)

        val database = AppDatabase.getDatabase(applicationContext)
        anotacionDao = database.anotacionDao()

        recyclerView = findViewById(R.id.recyclerViewNotas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        tvIngresosArs = findViewById(R.id.tvTotalIngresosArs)
        tvIngresosUsd = findViewById(R.id.tvTotalIngresosUsd)
        tvGastosArs = findViewById(R.id.tvTotalGastosArs)
        tvGastosUsd = findViewById(R.id.tvTotalGastosUsd)

        val fabAgregar = findViewById<FloatingActionButton>(R.id.fabAgregarNota)
        fabAgregar.setOnClickListener {
            startActivity(Intent(this, AgregarAnotacionActivity::class.java))
        }

        // Eliminada la lógica de la barra de navegación

        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            val notas = anotacionDao.obtenerTodas()
            recyclerView.adapter = AnotacionAdapter(notas) { nota ->
                mostrarDialogoBorrar(nota)
            }

            // Calcular totales
            val ingresosArs = anotacionDao.totalIngresos("ARS") ?: 0.0
            val ingresosUsd = anotacionDao.totalIngresos("USD") ?: 0.0
            val gastosArs = anotacionDao.totalGastos("ARS") ?: 0.0
            val gastosUsd = anotacionDao.totalGastos("USD") ?: 0.0

            tvIngresosArs.text = "$ ${ingresosArs}"
            tvIngresosUsd.text = "U\$S ${ingresosUsd}"
            tvGastosArs.text = "$ ${gastosArs}"
            tvGastosUsd.text = "U\$S ${gastosUsd}"
        }
    }

    private fun mostrarDialogoBorrar(nota: Anotacion) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Anotación")
            .setMessage("¿Estás seguro de borrar '${nota.titulo}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    anotacionDao.eliminar(nota)
                    cargarDatos()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}