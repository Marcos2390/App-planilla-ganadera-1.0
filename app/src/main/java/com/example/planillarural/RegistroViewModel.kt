package com.example.planillarural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroViewModel(private val db: AppDatabase) : ViewModel() {

    // ¡NUEVO! Inserta un nuevo animal y devuelve su ID.
    suspend fun insertarAnimal(animal: Animal): Long {
        return withContext(Dispatchers.IO) {
            db.animalDao().insertar(animal)
        }
    }

    // ¡NUEVO! Actualiza un animal existente.
    fun actualizarAnimal(animal: Animal) {
        viewModelScope.launch {
            db.animalDao().actualizar(animal)
        }
    }

    suspend fun obtenerAnimalPorId(animalId: Int): Animal? {
        return db.animalDao().obtenerPorId(animalId)
    }
}