package com.tecsup.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tecsup.eventplanner.data.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EventViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()
    
    init {
        loadEvents()
    }
    
    private fun loadEvents() {
        val userId = auth.currentUser?.uid ?: return
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Escuchar cambios en tiempo real
        listenerRegistration = db.collection("events")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                    return@addSnapshotListener
                }
                
                val eventsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                _uiState.value = _uiState.value.copy(
                    events = eventsList,
                    isLoading = false,
                    errorMessage = null
                )
            }
    }
    
    fun createEvent(
        title: String,
        date: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) {
            onError("El título es obligatorio")
            return
        }
        
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Usuario no autenticado")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        val event = Event(
            title = title,
            date = date,
            description = description,
            userId = userId
        )
        
        db.collection("events")
            .add(event)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
                onError(e.message ?: "Error al crear el evento")
            }
    }
    
    fun updateEvent(
        eventId: String,
        title: String,
        date: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) {
            onError("El título es obligatorio")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        val updates = mapOf(
            "title" to title,
            "date" to date,
            "description" to description
        )
        
        db.collection("events")
            .document(eventId)
            .update(updates)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
                onError(e.message ?: "Error al actualizar el evento")
            }
    }
    
    fun deleteEvent(eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        db.collection("events")
            .document(eventId)
            .delete()
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
                onError(e.message ?: "Error al eliminar el evento")
            }
    }
    
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

