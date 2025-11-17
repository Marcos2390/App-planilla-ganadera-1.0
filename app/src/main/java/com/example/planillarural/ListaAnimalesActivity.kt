package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ListaAnimalesActivity : AppCompatActivity() {

    private lateinit var animalDao: AnimalDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var animalAdapter: AnimalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_animales)

        animalDao = AppDatabase.getDatabase(applicationContext).animalDao()

        recyclerView = findViewById(R.id.recyclerViewAnimales)
        val fabAgregarAnimal: FloatingActionButton = findViewById(R.id.fabAgregarAnimal)
        val fabAgregarMovimiento: FloatingActionButton = findViewById(R.id.fabAgregarMovimiento)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAgregarAnimal.setOnClickListener {
            val intent = Intent(this, AgregarAnimalActivity::class.java)
            startActivity(intent)
        }

        fabAgregarMovimiento.setOnClickListener {
            val intent = Intent(this, MovimientosActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarAnimales()
    }

    private fun cargarAnimales() {
        lifecycleScope.launch {
            val listaDeAnimales = animalDao.obtenerTodos()
            animalAdapter = AnimalAdapter(
                animales = listaDeAnimales,
                onAnimalClickListener = { animal ->
                    val intent = Intent(this@ListaAnimalesActivity, AgregarAnimalActivity::class.java)
                    intent.putExtra("ANIMAL_ID", animal.id)
                    startActivity(intent)
                },
                onAnimalLongClickListener = { animal ->
                    mostrarDialogoDeConfirmacion(animal)
                }
            )
            recyclerView.adapter = animalAdapter
        }
    }

    private fun mostrarDialogoDeConfirmacion(animal: Animal) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el animal con caravana ${animal.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarAnimal(animal)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarAnimal(animal: Animal) {
        lifecycleScope.launch {
            animalDao.eliminar(animal)
            cargarAnimales()
        }
    }
}