package com.pemmob.luma.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.pemmob.luma.ui.auth.AuthUiState
import com.pemmob.luma.ui.auth.AuthViewModel
import com.pemmob.luma.ui.auth.LoginRoute
import com.pemmob.luma.ui.auth.RegisterRoute

@Composable
fun LumaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier
    ) {
        // Splash Screen / Pengecekan Sesi (Session Management)
        composable<SplashRoute> {
            SplashScreen(
                onAuthenticated = {
                    navController.navigate(MainGraph) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(AuthGraph) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                }
            )
        }

        // Sub-graph Otentikasi (Fokus Fahri)
        navigation<AuthGraph>(startDestination = LoginRoute) {
            composable<LoginRoute> {
                LoginRoute(
                    onNavigateToRegister = {
                        navController.navigate(RegisterRoute)
                    },
                    onLoginSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo<AuthGraph> { inclusive = true }
                        }
                    }
                )
            }

            composable<RegisterRoute> {
                RegisterRoute(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo<AuthGraph> { inclusive = true }
                        }
                    }
                )
            }
        }

        // Sub-graph Utama Placeholder (Dashboard Nindy/Fahmi/Melysa)
        navigation<MainGraph>(startDestination = DashboardPlaceholderRoute) {
            composable<DashboardPlaceholderRoute> {
                DashboardPlaceholderScreen(
                    onLogoutSuccess = {
                        navController.navigate(AuthGraph) {
                            popUpTo<MainGraph> { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SplashScreen(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkSession()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onAuthenticated()
            is AuthUiState.LoggedOut -> onUnauthenticated()
            is AuthUiState.Error -> onUnauthenticated()
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LUMA",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun DashboardPlaceholderScreen(
    onLogoutSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            viewModel.checkSession()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.LoggedOut) {
            onLogoutSuccess()
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dashboard LUMA",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selamat Datang, ${currentUser?.fullName ?: currentUser?.email ?: "Pengguna"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.onLogoutClick() },
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Keluar (Logout)")
                    }
                }
            }
        }
    }
}
