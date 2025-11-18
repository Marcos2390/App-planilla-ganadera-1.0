package com.example.planillarural

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgregarAnimalActivity : AppCompatActivity() {

    private lateinit var registroViewModel: RegistroViewModel
    private lateinit var movimientoDao: MovimientoDao
    private lateinit var nacimientoPendienteDao: NacimientoPendienteDao
    private var animalId: Int = -1
    private var nacimientoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_animal)

        val database = AppDatabase.getDatabase(applicationContext)
        val viewModelFactory = RegistroViewModelFactory(database)
        registroViewModel = ViewModelProvider(this, viewModelFactory)[RegistroViewModel::class.java]
        movimientoDao = database.movimientoDao()
        nacimientoPendienteDao = database.nacimientoPendienteDao()

        val txtCaravana = findViewById<EditText>(R.id.etNumeroCaravana)
        val txtCategoria = findViewById<EditText>(R.id.etCategoria)
        val txtRaza = findViewById<EditText>(R.id.etRaza)
        val txtEdad = findViewById<EditText>(R.id.etEdad)
        val txtInfoAdicional = findViewById<EditText>(R.id.etInformacionAdicional)
        val spinnerColor = findViewById<Spinner>(R.id.spinnerColorAnimal) // ¡NUEVO!
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val btnVerSanidad = findViewById<Button>(R.id.btnVerSanidad)
        val btnVerMovimientos = findViewById<Button>(R.id.btnVerMovimientos)

        // Obtener los códigos de color desde resources
        val codigosColores = resources.getStringArray(R.array.codigos_colores_animales)

        animalId = intent.getIntExtra("ANIMAL_ID", -1)
        nacimientoId = intent.getIntExtra("NACIMIENTO_ID", -1)

        if (nacimientoId != -1) {
            // MODO 3: Vengo de la pantalla de nacimientos pendientes
            val categoriaPrecardaga = intent.getStringExtra("CATEGORIA_PRECARGADA")
            val fechaNacPrecardaga = intent.getStringExtra("FECHA_NAC_PRECARGADA")
            txtCategoria.setText(categoriaPrecardaga)
            txtEdad.setText(fechaNacPrecardaga)
            btnVerSanidad.visibility = View.GONE
            btnVerMovimientos.visibility = View.GONE
        } else if (animalId != -1) {
            // MODO 2: Vengo a editar un animal existente
            btnVerSanidad.visibility = View.VISIBLE
            btnVerMovimientos.visibility = View.VISIBLE
            lifecycleScope.launch {
                val animal = registroViewModel.obtenerAnimalPorId(animalId)
                animal?.let {
                    txtCaravana.setText(it.nombre)
                    txtCategoria.setText(it.categoria)
                    txtRaza.setText(it.raza)
                    txtEdad.setText(it.fechaNac)
                    txtInfoAdicional.setText(it.informacionAdicional)
                    
                    // Seleccionar el color correcto en el Spinner
                    val colorIndex = codigosColores.indexOf(it.color)
                    if (colorIndex != -1) {
                        spinnerColor.setSelection(colorIndex)
                    }
                }
            }
        } else {
            // MODO 1: Creación de un animal nuevo desde cero
            txtCaravana.setText("")
            txtCategoria.setText("")
            txtRaza.setText("")
            txtEdad.setText("")
            txtInfoAdicional.setText("")
            spinnerColor.setSelection(0) // Color por defecto (Blanco)
            btnVerSanidad.visibility = View.GONE
            btnVerMovimientos.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            val nombre = txtCaravana.text.toString()
            val categoria = txtCategoria.text.toString()
            val colorSeleccionado = codigosColores[spinnerColor.selectedItemPosition] // ¡OBTENER COLOR!

            if (nombre.isEmpty() || categoria.isEmpty()) {
                Toast.makeText(this, "Caravana y Categoría son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (animalId == -1) {
                    // --- CREAR ANIMAL NUEVO ---
                    val animalNuevo = Animal(
                        nombre = nombre,
                        categoria = categoria,
                        raza = txtRaza.text.toString(),
                        fechaNac = txtEdad.text.toString(),
                        informacionAdicional = txtInfoAdicional.text.toString(),
                        color = colorSeleccionado // ¡GUARDAR COLOR!
                    )
                    val nuevoId = registroViewModel.insertarAnimal(animalNuevo)

                    if (nacimientoId == -1) {
                        // Si es un registro manual (no un nacimiento), crear movimiento.
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val fechaActual = sdf.format(Date())
                        val movimiento = Movimiento(
                            animalId = nuevoId.toInt(),
                            tipo = "Registro inicial",
                            categoria = categoria,
                            fecha = fechaActual,
                            cantidad = 1,
                            motivo = "Registro manual"
                        )
                        movimientoDao.registrar(movimiento)
                    } else {
                        // Si venía de un nacimiento, actualizar el contador.
                        val nacimiento = nacimientoPendienteDao.obtenerPorId(nacimientoId)
                        nacimiento?.let {
                            it.cantidadAsignada++
                            nacimientoPendienteDao.actualizar(it)
                        }
                    }
                } else {
                    // --- ACTUALIZAR ANIMAL EXISTENTE ---
                    val animalActualizado = Animal(
                        id = animalId,
                        nombre = nombre,
                        categoria = categoria,
                        raza = txtRaza.text.toString(),
                        fechaNac = txtEdad.text.toString(),
                        informacionAdicional = txtInfoAdicional.text.toString(),
                        color = colorSeleccionado // ¡ACTUALIZAR COLOR!
                    )
                    registroViewModel.actualizarAnimal(animalActualizado)
                }

                runOnUiThread {
                    Toast.makeText(this@AgregarAnimalActivity, "Animal guardado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCancelar.setOnClickListener {
            finish()
        }

        btnVerSanidad.setOnClickListener {
            if (animalId != -1) {
                val intent = Intent(this, SanidadActivity::class.java)
                intent.putExtra("ANIMAL_ID", animalId)
                startActivity(intent)
            }
        }

        btnVerMovimientos.setOnClickListener {
            if (animalId != -1) {
                val intent = Intent(this, MovimientosActivity::class.java)
                intent.putExtra("ANIMAL_ID", animalId)
                startActivity(intent)
            }
        }
    }
}