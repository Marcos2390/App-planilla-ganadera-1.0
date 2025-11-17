package com.example.planillarural

import android.content.Intent
import android.os.Bundle
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

        animalId = intent.getIntExtra("ANIMAL_ID", -1)
        if (animalId == -1) {
            // Si no hay ID, no podemos mostrar nada, así que cerramos.
            finish()
            return
        }

        sanidadDao = AppDatabase.getDatabase(applicationContext).sanidadDao()
        recyclerView = findViewById(R.id.recyclerViewSanidad)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fab: FloatingActionButton = findViewById(R.id.fabAgregarSanidad)
        fab.setOnClickListener {
            val intent = Intent(this, AgregarSanidadActivity::class.java)
            intent.putExtra("ANIMAL_ID", animalId) // Pasamos el ID a la siguiente pantalla
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (animalId != -1) {
            cargarRegistrosDeSanidad()
        }
    }

    private fun cargarRegistrosDeSanidad() {
        lifecycleScope.launch {
            val listaDeSanidad = sanidadDao.obtenerPorAnimal(animalId)
            sanidadAdapter = SanidadAdapter(listaDeSanidad)
            recyclerView.adapter = sanidadAdapter
        }
    }
}