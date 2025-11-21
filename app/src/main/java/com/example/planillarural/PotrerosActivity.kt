package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class PotrerosActivity : AppCompatActivity() {

    private lateinit var potreroDao: PotreroDao
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_potreros_main)

        val database = AppDatabase.getDatabase(applicationContext)
        potreroDao = database.potreroDao()

        recyclerView = findViewById(R.id.recyclerViewPotreros)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fabAgregar = findViewById<FloatingActionButton>(R.id.fabAgregarPotrero)
        fabAgregar.setOnClickListener {
            startActivity(Intent(this, AgregarPotreroActivity::class.java))
        }

        cargarPotreros()
    }

    override fun onResume() {
        super.onResume()
        cargarPotreros()
    }

    private fun cargarPotreros() {
        lifecycleScope.launch {
            val lista = potreroDao.obtenerTodos()
            recyclerView.adapter = PotreroAdapter(lista, 
                onPotreroClick = { potrero ->
                    // FASE 2: Aquí abriremos el detalle del potrero para ver animales
                    Toast.makeText(this@PotrerosActivity, "Detalle de ${potrero.nombre} (Próximamente)", Toast.LENGTH_SHORT).show()
                },
                onLongClick = { potrero ->
                    confirmarEliminacion(potrero)
                }
            )
        }
    }

    private fun confirmarEliminacion(potrero: Potrero) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Potrero")
            .setMessage("¿Borrar '${potrero.nombre}'? Se eliminará el registro de los animales ASIGNADOS a este lugar (no los animales en sí, solo su ubicación).")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    potreroDao.eliminarPotrero(potrero.id)
                    cargarPotreros()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}