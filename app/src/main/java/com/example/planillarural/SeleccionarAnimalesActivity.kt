package com.example.planillarural

import android.content.Intent
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

    // Variables para el modo "Mover a Potrero"
    private var modoMoverPotrero: Boolean = false
    private var potreroDestinoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_animales)

        val database = AppDatabase.getDatabase(applicationContext)
        animalDao = database.animalDao()
        sanidadDao = database.sanidadDao()

        // Detectar si venimos en modo "Mover a Potrero"
        modoMoverPotrero = intent.getBooleanExtra("MODO_MOVER_POTRERO", false)
        potreroDestinoId = intent.getIntExtra("POTRERO_DESTINO_ID", -1)

        recyclerView = findViewById(R.id.recyclerViewSelectAnimales)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnGuardar: Button = findViewById(R.id.btnGuardarSanidadGrupal)
        
        // Cambiar texto del botón según el modo
        if (modoMoverPotrero) {
            btnGuardar.text = "Mover al Potrero"
        }

        lifecycleScope.launch {
            val animales = animalDao.obtenerTodosActivos()
            adapter = AnimalSelectableAdapter(animales)
            recyclerView.adapter = adapter
        }

        btnGuardar.setOnClickListener {
            val seleccionados = adapter.getAnimalesSeleccionados()
            if (seleccionados.isEmpty()) {
                Toast.makeText(this, "No has seleccionado ningún animal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (modoMoverPotrero) {
                    // --- LÓGICA NUEVA: MOVER A POTRERO ---
                    for (animal in seleccionados) {
                        val animalActualizado = animal.copy(potreroId = potreroDestinoId)
                        animalDao.actualizar(animalActualizado)
                    }
                    
                    runOnUiThread {
                        Toast.makeText(this@SeleccionarAnimalesActivity, "${seleccionados.size} animales movidos al potrero", Toast.LENGTH_LONG).show()
                        finish() // Volver al detalle del potrero
                    }

                } else {
                    // --- LÓGICA ORIGINAL: REGISTRAR SANIDAD ---
                    val tratamiento = intent.getStringExtra("TRATAMIENTO") ?: ""
                    val producto = intent.getStringExtra("PRODUCTO") ?: ""
                    val dosis = intent.getStringExtra("DOSIS") ?: ""
                    val fecha = intent.getStringExtra("FECHA") ?: ""
                    val fechaProxima = intent.getStringExtra("FECHA_PROXIMA") ?: ""

                    for (animal in seleccionados) {
                        val registroSanidad = Sanidad(
                            animalId = animal.id,
                            fecha = fecha,
                            tratamiento = tratamiento,
                            producto = producto,
                            dosis = dosis,
                            fechaProximaDosis = if (fechaProxima.isNotEmpty()) fechaProxima else null,
                            especie = "Bovino"
                        )
                        sanidadDao.registrar(registroSanidad)
                    }

                    runOnUiThread {
                        Toast.makeText(this@SeleccionarAnimalesActivity, "Sanidad registrada para ${seleccionados.size} animales", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@SeleccionarAnimalesActivity, ListaAnimalesActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish() 
                    }
                }
            }
        }
    }
}