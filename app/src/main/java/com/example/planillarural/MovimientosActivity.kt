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

class MovimientosActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var movimientoAdapter: MovimientoAdapter
    private var animalId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movimientos)

        // Obtenemos el ID del animal (si viene de una ficha específica)
        animalId = intent.getIntExtra("ANIMAL_ID", -1)

        movimientoDao = AppDatabase.getDatabase(applicationContext).movimientoDao()
        recyclerView = findViewById(R.id.recyclerViewMovimientos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fab: FloatingActionButton = findViewById(R.id.fabAgregarMovimiento)
        fab.setOnClickListener {
            val intent = Intent(this, AgregarMovimientoActivity::class.java)
            if (animalId != -1) intent.putExtra("ANIMAL_ID", animalId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarMovimientos()
    }

    private fun cargarMovimientos() {
        lifecycleScope.launch {
            // Si animalId es -1, cargamos TODOS los movimientos
            val lista = if (animalId == -1) {
                movimientoDao.obtenerTodos()
            } else {
                movimientoDao.obtenerPorAnimal(animalId)
            }
            
            movimientoAdapter = MovimientoAdapter(lista) { movimiento ->
                confirmarEliminacion(movimiento)
            }
            recyclerView.adapter = movimientoAdapter
        }
    }

    private fun confirmarEliminacion(movimiento: Movimiento) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar registro?")
            .setMessage("¿Estás seguro de que quieres borrar este movimiento?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    movimientoDao.eliminar(movimiento)
                    cargarMovimientos()
                    Toast.makeText(this@MovimientosActivity, "Movimiento eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}