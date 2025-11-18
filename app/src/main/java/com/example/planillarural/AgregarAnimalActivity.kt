package com.example.planillarural

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
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

    // Referencias a las vistas para poder actualizarlas
    private lateinit var btnVerSanidad: Button
    private lateinit var btnVerMovimientos: Button

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
        val spinnerColor = findViewById<Spinner>(R.id.spinnerColorAnimal)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        
        // Inicializamos estas variables aquí para usarlas luego
        btnVerSanidad = findViewById(R.id.btnVerSanidad)
        btnVerMovimientos = findViewById(R.id.btnVerMovimientos)

        val codigosColores = resources.getStringArray(R.array.codigos_colores_animales)

        animalId = intent.getIntExtra("ANIMAL_ID", -1)
        nacimientoId = intent.getIntExtra("NACIMIENTO_ID", -1)

        if (nacimientoId != -1) {
            // MODO 3: Vengo de un nacimiento pendiente
            val categoriaPrecardaga = intent.getStringExtra("CATEGORIA_PRECARGADA")
            val fechaNacPrecardaga = intent.getStringExtra("FECHA_NAC_PRECARGADA")
            txtCategoria.setText(categoriaPrecardaga)
            txtEdad.setText(fechaNacPrecardaga)
            btnVerSanidad.visibility = View.GONE
            btnVerMovimientos.visibility = View.GONE
        } else if (animalId != -1) {
            // MODO 2: Editar existente
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
                    val colorIndex = codigosColores.indexOf(it.color)
                    if (colorIndex != -1) spinnerColor.setSelection(colorIndex)
                }
            }
        } else {
            // MODO 1: Nuevo
            txtCaravana.setText("")
            txtCategoria.setText("")
            txtRaza.setText("")
            txtEdad.setText("")
            txtInfoAdicional.setText("")
            spinnerColor.setSelection(0)
            btnVerSanidad.visibility = View.GONE
            btnVerMovimientos.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val nombre = txtCaravana.text.toString()
            val categoria = txtCategoria.text.toString()
            val colorSeleccionado = codigosColores[spinnerColor.selectedItemPosition]

            if (nombre.isEmpty() || categoria.isEmpty()) {
                Toast.makeText(this, "Caravana y Categoría son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (animalId == -1) {
                    // --- INSERTAR NUEVO ---
                    val animalNuevo = Animal(
                        nombre = nombre,
                        categoria = categoria,
                        raza = txtRaza.text.toString(),
                        fechaNac = txtEdad.text.toString(),
                        informacionAdicional = txtInfoAdicional.text.toString(),
                        color = colorSeleccionado
                    )
                    val nuevoId = registroViewModel.insertarAnimal(animalNuevo)
                    
                    // Registrar el movimiento inicial
                    if (nacimientoId == -1) {
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
                        // Actualizar nacimiento pendiente
                        val nacimiento = nacimientoPendienteDao.obtenerPorId(nacimientoId)
                        nacimiento?.let {
                            it.cantidadAsignada++
                            nacimientoPendienteDao.actualizar(it)
                        }
                    }

                } else {
                    // --- ACTUALIZAR EXISTENTE ---
                    val animalActualizado = Animal(
                        id = animalId,
                        nombre = nombre,
                        categoria = categoria,
                        raza = txtRaza.text.toString(),
                        fechaNac = txtEdad.text.toString(),
                        informacionAdicional = txtInfoAdicional.text.toString(),
                        color = colorSeleccionado
                    )
                    registroViewModel.actualizarAnimal(animalActualizado)
                }

                runOnUiThread {
                    Toast.makeText(this@AgregarAnimalActivity, "Animal guardado correctamente", Toast.LENGTH_SHORT).show()
                    // Volver a la pantalla principal
                    val intent = Intent(this@AgregarAnimalActivity, ListaAnimalesActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
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

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}