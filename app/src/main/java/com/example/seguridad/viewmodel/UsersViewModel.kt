package com.example.seguridad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridad.data.model.AppUser
import com.example.seguridad.data.model.UserRole
import com.example.seguridad.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsersUiState(
    val isLoading: Boolean = false,
    val users: List<AppUser> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class UsersViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    successMessage = null
                )

                val users = loadSortedUsers()

                _uiState.value = UsersUiState(
                    isLoading = false,
                    users = users
                )
            } catch (e: Exception) {
                _uiState.value = UsersUiState(
                    isLoading = false,
                    error = e.message ?: "No se pudieron cargar los usuarios."
                )
            }
        }
    }

    fun changeRole(
        admin: AppUser,
        targetUser: AppUser,
        newRole: UserRole
    ) {
        viewModelScope.launch {
            try {
                if (admin.rol != UserRole.ADMINISTRADOR.name) {
                    _uiState.value = _uiState.value.copy(
                        error = "Solo un administrador puede cambiar roles."
                    )
                    return@launch
                }

                if (admin.uid == targetUser.uid && newRole != UserRole.ADMINISTRADOR) {
                    _uiState.value = _uiState.value.copy(
                        error = "No puedes quitarte tu propio rol de administrador."
                    )
                    return@launch
                }

                userRepository.updateUserRole(
                    uid = targetUser.uid,
                    role = newRole
                )

                val users = loadSortedUsers()

                _uiState.value = UsersUiState(
                    isLoading = false,
                    users = users,
                    successMessage = "Rol actualizado correctamente."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo cambiar el rol."
                )
            }
        }
    }

    fun toggleActive(
        admin: AppUser,
        targetUser: AppUser
    ) {
        viewModelScope.launch {
            try {
                if (admin.rol != UserRole.ADMINISTRADOR.name) {
                    _uiState.value = _uiState.value.copy(
                        error = "Solo un administrador puede activar o desactivar usuarios."
                    )
                    return@launch
                }

                if (admin.uid == targetUser.uid) {
                    _uiState.value = _uiState.value.copy(
                        error = "No puedes desactivar tu propia cuenta."
                    )
                    return@launch
                }

                userRepository.updateUserActive(
                    uid = targetUser.uid,
                    active = !targetUser.activo
                )

                val users = loadSortedUsers()

                _uiState.value = UsersUiState(
                    isLoading = false,
                    users = users,
                    successMessage = "Estado de usuario actualizado."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo actualizar el usuario."
                )
            }
        }
    }

    private suspend fun loadSortedUsers(): List<AppUser> {
        return userRepository.getAllUsers()
            .sortedWith(
                compareBy<AppUser> { it.rol }
                    .thenBy { it.nombre.lowercase() }
            )
    }
}