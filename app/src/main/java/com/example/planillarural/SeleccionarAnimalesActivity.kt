package com.example.planillarural

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class SeleccionarAnimalesActivity : AppCompatActivity() {

    private lateinit var animalDao: AnimalDao
    private lateinit var sanidadDao: SanidadDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnimalSelectableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_animales)

        val database = AppDatabase.getDatabase(applicationContext)
        animalDao = database.animalDao()
        sanidadDao = database.sanidadDao()

        recyclerView = findViewById(R.id.recyclerViewSelectAnimales)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnGuardar: Button = findViewById(R.id.btnGuardarSanidadGrupal)

        // Cargar todos los animales para la selección
        lifecycleScope.launch {
            val animales = animalDao.obtenerTodos()
            adapter = AnimalSelectableAdapter(animales)
            recyclerView.adapter = adapter
        }

        btnGuardar.setOnClickListener {
            val seleccionados = adapter.getAnimalesSeleccionados()
            if (seleccionados.isEmpty()) {
                Toast.makeText(this, "No has seleccionado ningún animal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener los datos del tratamiento de la pantalla anterior
            val tratamiento = intent.getStringExtra("TRATAMIENTO") ?: ""
            val producto = intent.getStringExtra("PRODUCTO") ?: ""
            val dosis = intent.getStringExtra("DOSIS") ?: ""
            val fecha = intent.getStringExtra("FECHA") ?: ""

            lifecycleScope.launch {
                for (animal in seleccionados) {
                    val registroSanidad = Sanidad(
                        animalId = animal.id,
                        fecha = fecha,
                        tratamiento = tratamiento,
                        producto = producto,
                        dosis = dosis
                    )
                    sanidadDao.registrar(registroSanidad)
                }

                runOnUiThread {
                    Toast.makeText(this@SeleccionarAnimalesActivity, "Sanidad registrada para ${seleccionados.size} animales", Toast.LENGTH_LONG).show()
                    finishAffinity() // Cierra esta pantalla y la anterior para volver a la principal
                }
            }
        }
    }
}