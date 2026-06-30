package com.example.seguridad.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.seguridad.data.model.IncidentStatus
import com.example.seguridad.data.model.UserRole
import com.example.seguridad.ui.screens.DashboardScreen
import com.example.seguridad.ui.screens.IncidenceFormScreen
import com.example.seguridad.ui.screens.IncidentsListScreen
import com.example.seguridad.ui.screens.LoginScreen
import com.example.seguridad.ui.screens.MapScreen
import com.example.seguridad.ui.screens.ReportsScreen
import com.example.seguridad.ui.screens.UsersManagementScreen
import com.example.seguridad.viewmodel.AuthViewModel
import com.example.seguridad.viewmodel.IncidentsViewModel
import com.example.seguridad.viewmodel.ReportsViewModel
import com.example.seguridad.viewmodel.UsersViewModel

private enum class MainTab(
    val title: String
) {
    DASHBOARD("Inicio"),
    REGISTER("Registrar"),
    INCIDENTS("Incidencias"),
    MAP("Mapa"),
    REPORTS("Reportes"),
    USERS("Usuarios")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(
    authViewModel: AuthViewModel = viewModel(),
    incidentsViewModel: IncidentsViewModel = viewModel(),
    reportsViewModel: ReportsViewModel = viewModel(),
    usersViewModel: UsersViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val incidentsState by incidentsViewModel.uiState.collectAsState()
    val reportsState by reportsViewModel.uiState.collectAsState()
    val usersState by usersViewModel.uiState.collectAsState()

    val currentUser = authState.currentUser

    LaunchedEffect(Unit) {
        authViewModel.checkSession()
    }

    if (currentUser == null) {
        LoginScreen(
            state = authState,
            onLogin = authViewModel::login,
            onRegister = authViewModel::register
        )
        return
    }

    val canAccessReports = currentUser.rol == UserRole.ADMINISTRADOR.name ||
            currentUser.rol == UserRole.POLICIA.name

    val visibleTabs = MainTab.entries.filter { tab ->
        tab != MainTab.REPORTS || canAccessReports
    }

    var selectedTab by rememberSaveable {
        mutableStateOf(MainTab.DASHBOARD.name)
    }

    LaunchedEffect(currentUser.uid, currentUser.rol) {
        incidentsViewModel.loadIncidents(currentUser)

        if (canAccessReports) {
            reportsViewModel.loadReports()
        if (currentUser.rol == UserRole.ADMINISTRADOR.name) {
            usersViewModel.loadUsers()
        }
        }

        if (!canAccessReports && selectedTab == MainTab.REPORTS.name) {
            selectedTab = MainTab.DASHBOARD.name
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Seguridad Arequipa")
                },
                actions = {
                    TextButton(
                        onClick = {
                            authViewModel.logout()
                            selectedTab = MainTab.DASHBOARD.name
                        }
                    ) {
                        Text("Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                visibleTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.name,
                        onClick = { selectedTab = tab.name },
                        label = { Text(tab.title) },
                        icon = {
                            when (tab) {
                                MainTab.DASHBOARD -> Icon(Icons.Filled.Home, contentDescription = "Inicio")
                                MainTab.REGISTER -> Icon(Icons.Filled.NoteAdd, contentDescription = "Registrar")
                                MainTab.INCIDENTS -> Icon(Icons.Filled.ListAlt, contentDescription = "Incidencias")
                                MainTab.MAP -> Icon(Icons.Filled.LocationOn, contentDescription = "Mapa")
                                MainTab.REPORTS -> Icon(Icons.Filled.Assessment, contentDescription = "Reportes")
                                MainTab.USERS -> Icon(Icons.Filled.Person, contentDescription = "Usuarios")
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        when (MainTab.valueOf(selectedTab)) {
            MainTab.DASHBOARD -> {
                Column(modifier = modifier) {
                    DashboardScreen(
                        user = currentUser,
                        stats = incidentsState.stats
                    )
                }
            }

            MainTab.REGISTER -> {
                Column(modifier = modifier) {
                    IncidenceFormScreen(
                        user = currentUser,
                        state = incidentsState,
                        onRegister = { descripcion, ubicacion, latitud, longitud ->
                            incidentsViewModel.registerIncident(
                                user = currentUser,
                                descripcion = descripcion,
                                ubicacion = ubicacion,
                                latitudText = latitud,
                                longitudText = longitud
                            )
                        }
                    )
                }
            }

            MainTab.INCIDENTS -> {
                Column(modifier = modifier) {
                    IncidentsListScreen(
                        user = currentUser,
                        state = incidentsState,
                        onRefresh = {
                            incidentsViewModel.loadIncidents(currentUser)
                        },
                        onStatusChange = { id, status: IncidentStatus ->
                            incidentsViewModel.updateStatus(
                                currentUser,
                                id,
                                status
                            )
                        },
                        onAssignPolice = { incidentId, police ->
                            incidentsViewModel.assignPolice(
                                user = currentUser,
                                incidentId = incidentId,
                                police = police
                            )
                        }
                    )
                }
            }

            MainTab.MAP -> {
                Column(modifier = modifier) {
                    MapScreen(
                        incidents = incidentsState.incidents
                    )
                }
            }

            MainTab.USERS -> {
                androidx.compose.foundation.layout.Column(modifier = modifier) {
                    UsersManagementScreen(
                        admin = currentUser,
                        state = usersState,
                        onRefresh = {
                            usersViewModel.loadUsers()
                        },
                        onChangeRole = { targetUser, newRole ->
                            usersViewModel.changeRole(
                                admin = currentUser,
                                targetUser = targetUser,
                                newRole = newRole
                            )
                        },
                        onToggleActive = { targetUser ->
                            usersViewModel.toggleActive(
                                admin = currentUser,
                                targetUser = targetUser
                            )
                        }
                    )
                }
            }
            MainTab.REPORTS -> {
                Column(modifier = modifier) {
                    ReportsScreen(
                        user = currentUser,
                        incidents = incidentsState.incidents,
                        state = reportsState,
                        onGenerate = {
                            if (currentUser.rol == UserRole.ADMINISTRADOR.name) {
                                reportsViewModel.generateReport(
                                    user = currentUser,
                                    incidents = incidentsState.incidents
                                )
                            }
                        },
                        onRefresh = {
                            if (canAccessReports) {
                                reportsViewModel.loadReports()
        if (currentUser.rol == UserRole.ADMINISTRADOR.name) {
            usersViewModel.loadUsers()
        }
                            }
                        }
                    )
                }
            }
        }
    }
}





