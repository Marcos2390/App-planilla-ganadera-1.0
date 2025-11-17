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

class MovimientosActivity : AppCompatActivity() {

    private lateinit var movimientoDao: MovimientoDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var movimientoAdapter: MovimientoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movimientos)

        movimientoDao = AppDatabase.getDatabase(applicationContext).movimientoDao()
        recyclerView = findViewById(R.id.recyclerViewMovimientos)
        val fabAgregarMovimiento: FloatingActionButton = findViewById(R.id.fabAgregarMovimiento)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAgregarMovimiento.setOnClickListener {
            val intent = Intent(this, AgregarMovimientoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarMovimientos()
    }

    private fun cargarMovimientos() {
        lifecycleScope.launch {
            val listaDeMovimientos = movimientoDao.obtenerTodos()
            movimientoAdapter = MovimientoAdapter(listaDeMovimientos) { movimiento ->
                mostrarDialogoDeConfirmacion(movimiento)
            }
            recyclerView.adapter = movimientoAdapter
        }
    }

    private fun mostrarDialogoDeConfirmacion(movimiento: Movimiento) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar este movimiento?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarMovimiento(movimiento)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarMovimiento(movimiento: Movimiento) {
        lifecycleScope.launch {
            movimientoDao.eliminar(movimiento)
            cargarMovimientos()
        }
    }
}