package com.tecsup.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(AuthUiState(
        isAuthenticated = auth.currentUser != null
    ))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        // Observar cambios en el estado de autenticación
        auth.addAuthStateListener { firebaseAuth ->
            _uiState.value = _uiState.value.copy(
                isAuthenticated = firebaseAuth.currentUser != null
            )
        }
    }
    
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onError("Complete todos los campos")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (task.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isAuthenticated = true)
                    onSuccess()
                } else {
                    val error = task.exception?.message ?: "Error desconocido"
                    _uiState.value = _uiState.value.copy(errorMessage = error)
                    onError(error)
                }
            }
    }
    
    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            onError("Complete todos los campos")
            return
        }
        
        if (password != confirmPassword) {
            onError("Las contraseñas no coinciden")
            return
        }
        
        if (password.length < 6) {
            onError("La contraseña debe tener al menos 6 caracteres")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (task.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isAuthenticated = true)
                    onSuccess()
                } else {
                    val error = task.exception?.message ?: "Error desconocido"
                    _uiState.value = _uiState.value.copy(errorMessage = error)
                    onError(error)
                }
            }
    }
    
    fun logout() {
        auth.signOut()
        _uiState.value = _uiState.value.copy(isAuthenticated = false)
    }
    
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}

