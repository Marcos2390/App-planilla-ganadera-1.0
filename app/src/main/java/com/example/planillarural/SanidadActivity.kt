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

class SanidadActivity : AppCompatActivity() {

    private lateinit var sanidadDao: SanidadDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var sanidadAdapter: SanidadAdapter
    private var animalId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sanidad)

        // Intentamos obtener el ID del animal, pero si no viene (-1), mostramos TODO el historial
        animalId = intent.getIntExtra("ANIMAL_ID", -1)

        sanidadDao = AppDatabase.getDatabase(applicationContext).sanidadDao()
        recyclerView = findViewById(R.id.recyclerViewSanidad)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fab: FloatingActionButton = findViewById(R.id.fabAgregarSanidad)
        fab.setOnClickListener {
            // Si no hay animalId, abrimos la versión grupal
            if (animalId == -1) {
                startActivity(Intent(this, AgregarSanidadGrupalActivity::class.java))
            } else {
                val intent = Intent(this, AgregarSanidadActivity::class.java)
                intent.putExtra("ANIMAL_ID", animalId)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarRegistrosDeSanidad()
    }

    private fun cargarRegistrosDeSanidad() {
        lifecycleScope.launch {
            val listaDeSanidad = if (animalId == -1) {
                sanidadDao.obtenerTodos()
            } else {
                sanidadDao.obtenerPorAnimal(animalId)
            }
            
            sanidadAdapter = SanidadAdapter(listaDeSanidad) { sanidad ->
                confirmarEliminacionSanidad(sanidad)
            }
            recyclerView.adapter = sanidadAdapter
        }
    }

    private fun confirmarEliminacionSanidad(sanidad: Sanidad) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar registro?")
            .setMessage("¿Estás seguro de que quieres borrar este tratamiento?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    sanidadDao.eliminar(sanidad)
                    cargarRegistrosDeSanidad()
                    Toast.makeText(this@SanidadActivity, "Registro eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}