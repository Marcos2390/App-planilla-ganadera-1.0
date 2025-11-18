package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class NacimientosPendientesActivity : AppCompatActivity() {

    private lateinit var nacimientoDao: NacimientoPendienteDao
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nacimientos_pendientes)

        nacimientoDao = AppDatabase.getDatabase(applicationContext).nacimientoPendienteDao()
        recyclerView = findViewById(R.id.recyclerViewNacimientosPendientes)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        cargarNacimientosPendientes()
    }

    private fun cargarNacimientosPendientes() {
        lifecycleScope.launch {
            val pendientes = nacimientoDao.obtenerTodosPendientes()
            recyclerView.adapter = NacimientoPendienteAdapter(pendientes) { nacimiento ->
                // Al hacer clic, abrimos la pantalla de agregar animal
                val intent = Intent(this@NacimientosPendientesActivity, AgregarAnimalActivity::class.java)
                // Le pasamos los datos del nacimiento para que los use
                intent.putExtra("NACIMIENTO_ID", nacimiento.id)
                intent.putExtra("CATEGORIA_PRECARGADA", nacimiento.categoria)
                intent.putExtra("FECHA_NAC_PRECARGADA", nacimiento.fecha)
                startActivity(intent)
            }
        }
    }
}