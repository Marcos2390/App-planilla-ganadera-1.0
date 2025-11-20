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
import androidx.appcompat.app.AlertDialog
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
    private lateinit var animalDao: AnimalDao
    private lateinit var nacimientoPendienteDao: NacimientoPendienteDao 
    private var animalId: Int = -1
    private var nacimientoId: Int = -1
    private var animalActual: Animal? = null

    private lateinit var btnVerSanidad: Button
    private lateinit var btnVerMovimientos: Button
    private lateinit var btnDarDeBaja: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_animal)

        val database = AppDatabase.getDatabase(applicationContext)
        val viewModelFactory = RegistroViewModelFactory(database)
        registroViewModel = ViewModelProvider(this, viewModelFactory)[RegistroViewModel::class.java]
        movimientoDao = database.movimientoDao()
        animalDao = database.animalDao()
        nacimientoPendienteDao = database.nacimientoPendienteDao()

        val txtCaravana = findViewById<EditText>(R.id.etNumeroCaravana)
        val txtCategoria = findViewById<EditText>(R.id.etCategoria)
        val txtRaza = findViewById<EditText>(R.id.etRaza)
        val txtEdad = findViewById<EditText>(R.id.etEdad)
        val txtInfoAdicional = findViewById<EditText>(R.id.etInformacionAdicional)
        // Eliminada referencia a spinnerColorAnimal ya que se quitó del diseño
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        
        btnVerSanidad = findViewById(R.id.btnVerSanidad)
        btnVerMovimientos = findViewById(R.id.btnVerMovimientos)
        btnDarDeBaja = findViewById(R.id.btnDarDeBaja)

        // Eliminado array de colores

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
            btnDarDeBaja.visibility = View.GONE
        } else if (animalId != -1) {
            // MODO 2: Editar existente
            btnVerSanidad.visibility = View.VISIBLE
            btnVerMovimientos.visibility = View.VISIBLE
            btnDarDeBaja.visibility = View.VISIBLE
            lifecycleScope.launch {
                animalActual = registroViewModel.obtenerAnimalPorId(animalId)
                animalActual?.let {
                    txtCaravana.setText(it.nombre)
                    txtCategoria.setText(it.categoria)
                    txtRaza.setText(it.raza)
                    txtEdad.setText(it.fechaNac)
                    txtInfoAdicional.setText(it.informacionAdicional)
                    // Eliminada lógica de setSelection de color
                }
            }
        } else {
            // MODO 1: Nuevo
            btnVerSanidad.visibility = View.GONE
            btnVerMovimientos.visibility = View.GONE
            btnDarDeBaja.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            ocultarTeclado()

            val nombre = txtCaravana.text.toString()
            val categoria = txtCategoria.text.toString()
            // Eliminada lectura de colorSeleccionado

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
                        color = null, // Ya no usamos color personalizado
                        status = "Activo" 
                    )
                    val nuevoId = registroViewModel.insertarAnimal(animalNuevo)
                    
                    // Actualizamos el ID para que la próxima vez sea una edición
                    animalId = nuevoId.toInt()

                    // Registrar el movimiento inicial
                    if (nacimientoId == -1) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val fechaActual = sdf.format(Date())
                        val movimiento = Movimiento(
                            animalId = animalId,
                            tipo = "Registro inicial",
                            categoria = categoria,
                            fecha = fechaActual,
                            cantidad = 1,
                            motivo = "Registro manual",
                            especie = "Bovino"
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

                    // Mostrar los botones de historial porque ya existe el animal
                    runOnUiThread {
                        btnVerSanidad.visibility = View.VISIBLE
                        btnVerMovimientos.visibility = View.VISIBLE
                        btnDarDeBaja.visibility = View.VISIBLE
                        Toast.makeText(this@AgregarAnimalActivity, "Animal guardado", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // --- ACTUALIZAR EXISTENTE ---
                    val statusActual = animalActual?.status ?: "Activo"
                    
                    val animalActualizado = Animal(
                        id = animalId,
                        nombre = nombre,
                        categoria = categoria,
                        raza = txtRaza.text.toString(),
                        fechaNac = txtEdad.text.toString(),
                        informacionAdicional = txtInfoAdicional.text.toString(),
                        color = null, // Sin color
                        status = statusActual
                    )
                    registroViewModel.actualizarAnimal(animalActualizado)
                    
                    runOnUiThread {
                        Toast.makeText(this@AgregarAnimalActivity, "Cambios guardados", Toast.LENGTH_SHORT).show()
                    }
                }
                
                // Volver a la pantalla principal
                val intent = Intent(this@AgregarAnimalActivity, ListaAnimalesActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

        btnDarDeBaja.setOnClickListener {
            mostrarDialogoBaja()
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

    private fun mostrarDialogoBaja() {
        val animal = animalActual ?: return

        AlertDialog.Builder(this)
            .setTitle("Confirmar Baja de Animal")
            .setMessage("¿Estás seguro de que quieres dar de baja la caravana ${animal.nombre}?")
            .setPositiveButton("Venta") { _, _ ->
                registrarBajaAnimal("Vendido")
            }
            .setNegativeButton("Muerte") { _, _ ->
                registrarBajaAnimal("Muerto")
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun registrarBajaAnimal(nuevoEstado: String) {
        val animal = animalActual ?: return

        lifecycleScope.launch {
            // 1. Crear el movimiento de Venta o Muerte
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaActual = sdf.format(Date())
            val movimiento = Movimiento(
                animalId = animal.id,
                tipo = if (nuevoEstado == "Vendido") "Venta" else "Muerte",
                categoria = animal.categoria,
                fecha = fechaActual,
                cantidad = 1,
                motivo = "Baja desde ficha de animal",
                especie = "Bovino"
            )
            movimientoDao.registrar(movimiento)

            // 2. Actualizar el estado del animal
            val animalActualizado = animal.copy(status = nuevoEstado)
            animalDao.actualizar(animalActualizado)

            runOnUiThread {
                Toast.makeText(this@AgregarAnimalActivity, "Animal dado de baja como $nuevoEstado", Toast.LENGTH_SHORT).show()
                
                // Volver a la lista
                val intent = Intent(this@AgregarAnimalActivity, ListaAnimalesActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
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