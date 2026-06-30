package com.example.seguridad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridad.data.model.AppUser
import com.example.seguridad.data.model.UserRole
import com.example.seguridad.data.repository.AuthRepository
import com.example.seguridad.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: AppUser? = null,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun checkSession() {
        viewModelScope.launch {
            val firebaseUser = authRepository.currentUser()

            if (firebaseUser == null) {
                _uiState.value = AuthUiState(currentUser = null)
                return@launch
            }

            val appUser = userRepository.getUser(firebaseUser.uid)

            _uiState.value = AuthUiState(currentUser = appUser)
        }
    }

    fun login(correo: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val firebaseUser = authRepository.login(correo.trim(), password)
                var appUser = userRepository.getUser(firebaseUser.uid)

                if (appUser == null) {
                    appUser = AppUser(
                        uid = firebaseUser.uid,
                        nombre = correo.substringBefore("@"),
                        correo = correo.trim(),
                        rol = UserRole.CIUDADANO.name
                    )
                    userRepository.saveUser(appUser)
                }

                _uiState.value = AuthUiState(currentUser = appUser)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message ?: "Error al iniciar sesiÃ³n.")
            }
        }
    }

    fun register(nombre: String, correo: String, password: String, rol: UserRole) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val firebaseUser = authRepository.register(correo.trim(), password)

                val appUser = AppUser(
                    uid = firebaseUser.uid,
                    nombre = nombre.trim(),
                    correo = correo.trim(),
                    rol = UserRole.CIUDADANO.name
                )

                userRepository.saveUser(appUser)

                _uiState.value = AuthUiState(currentUser = appUser)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message ?: "Error al registrar usuario.")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState(currentUser = null)
    }
}


