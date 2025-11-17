package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ListaAnimalesActivity : AppCompatActivity() {

    private lateinit var animalDao: AnimalDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var animalAdapter: AnimalAdapter
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_lista_animales)

        animalDao = AppDatabase.getDatabase(applicationContext).animalDao()

        recyclerView = findViewById(R.id.recyclerViewAnimales)
        searchView = findViewById(R.id.searchViewAnimales) // ¡NUEVO!
        val fabAgregarAnimal: FloatingActionButton = findViewById(R.id.fabAgregarAnimal)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAgregarAnimal.setOnClickListener {
            val intent = Intent(this, AgregarAnimalActivity::class.java)
            startActivity(intent)
        }

        // Configuración del listener para la búsqueda (¡NUEVO!)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                cargarAnimales(newText.orEmpty())
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cargarAnimales(searchView.query.toString())
    }

    // La función ahora acepta un texto de búsqueda (¡MODIFICADO!)
    private fun cargarAnimales(query: String = "") {
        lifecycleScope.launch {
            val listaDeAnimales = if (query.isEmpty()) {
                animalDao.obtenerTodos()
            } else {
                animalDao.buscarPorNombre(query)
            }

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
            cargarAnimales(searchView.query.toString()) // Recargamos con el filtro actual
        }
    }
}
