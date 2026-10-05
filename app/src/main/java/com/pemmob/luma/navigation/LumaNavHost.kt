package com.pemmob.luma.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pemmob.luma.ui.auth.AuthUiState
import com.pemmob.luma.ui.auth.AuthViewModel
import com.pemmob.luma.ui.auth.LoginRoute
import com.pemmob.luma.ui.auth.RegisterRoute
import com.pemmob.luma.ui.dashboard.DashboardRoute
import com.pemmob.luma.ui.debt.AddDebtScreen
import com.pemmob.luma.ui.debt.AddPaymentScreen
import com.pemmob.luma.ui.debt.DebtDetailScreen
import com.pemmob.luma.ui.debt.DebtScreen
import com.pemmob.luma.ui.notification.NotificationContent
import com.pemmob.luma.ui.profile.ProfileRoute
import com.pemmob.luma.ui.splitbill.SplitBillScreen
import com.pemmob.luma.ui.statistics.StatisticsContent
import com.pemmob.luma.ui.transaction.TransactionScreen

// ===== DATA CLASS UNTUK BOTTOM NAV ITEMS =====

private data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any
)

private val bottomNavItems = listOf(
    BottomNavItem("Beranda", Icons.Filled.Home, Icons.Outlined.Home, DashboardPlaceholderRoute),
    BottomNavItem("Transaksi", Icons.Filled.Receipt, Icons.Outlined.Receipt, TransactionRoute),
    BottomNavItem("Utang", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, DebtRoute),
    BottomNavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person, ProfileRoute)
)

// ===== ROUTES YANG TERMASUK MAIN BOTTOM NAV (menampilkan bottom bar) =====

private val bottomNavRoutes = setOf(
    DashboardPlaceholderRoute::class,
    TransactionRoute::class,
    DebtRoute::class,
    ProfileRoute::class
)

@Composable
fun LumaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavRoutes.any { routeClass ->
        currentDestination?.hasRoute(routeClass) == true
    }

    val navigateToTab: (Any) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(DashboardPlaceholderRoute) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                LumaBottomNavigationBar(
                    currentDestination = currentDestination,
                    onNavigate = navigateToTab
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            modifier = modifier.padding(innerPadding)
        ) {
            // ===== SPLASH =====
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

            // ===== AUTH GRAPH =====
            navigation<AuthGraph>(startDestination = LoginRoute) {
                composable<LoginRoute> {
                    LoginRoute(
                        onNavigateToRegister = { navController.navigate(RegisterRoute) },
                        onLoginSuccess = {
                            navController.navigate(MainGraph) {
                                popUpTo<AuthGraph> { inclusive = true }
                            }
                        }
                    )
                }
                composable<RegisterRoute> {
                    RegisterRoute(
                        onNavigateToLogin = { navController.popBackStack() },
                        onRegisterSuccess = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            // ===== MAIN GRAPH =====
            navigation<MainGraph>(startDestination = DashboardPlaceholderRoute) {

                // Dashboard
                composable<DashboardPlaceholderRoute> {
                    DashboardRoute(
                        onAddTransactionClick = { navigateToTab(TransactionRoute) },
                        onSeeAllTransactionsClick = { navigateToTab(TransactionRoute) },
                        onDebtClick = { navigateToTab(DebtRoute) },
                        onSplitBillClick = { navController.navigate(SplitBillRoute) },
                        onStatisticsClick = { navController.navigate(StatisticsRoute) }
                    )
                }

                // Transaksi
                composable<TransactionRoute> {
                    TransactionScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ===== DEBT & RECEIVABLE =====

                composable<DebtRoute> {
                    DebtScreen(
                        onNavigateToDetail = { debtId ->
                            navController.navigate(DebtDetailRoute(debtId))
                        },
                        onNavigateToAddDebt = { navController.navigate(AddDebtRoute) },
                        onNavigateToSplitBill = { navController.navigate(SplitBillRoute) }
                    )
                }

                composable<DebtDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<DebtDetailRoute>()
                    DebtDetailScreen(
                        debtId = route.debtId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAddPayment = { debtId ->
                            navController.navigate(AddPaymentRoute(debtId))
                        }
                    )
                }

                composable<AddDebtRoute> {
                    AddDebtScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<AddPaymentRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<AddPaymentRoute>()
                    AddPaymentScreen(
                        debtId = route.debtId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ===== SPLIT BILL =====

                composable<SplitBillRoute> {
                    SplitBillScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSplitBillCreated = { splitBillId ->
                            navController.navigate(DebtRoute) {
                                popUpTo(SplitBillRoute) { inclusive = true }
                            }
                        }
                    )
                }

                // ===== STATISTICS =====

                composable<StatisticsRoute> {
                    StatisticsContent(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ===== NOTIFICATION =====

                composable<NotificationRoute> {
                    NotificationContent(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ===== PROFIL =====

                composable<ProfileRoute> {
                    ProfileRoute(
                        onLogoutSuccess = {
                            navController.navigate(AuthGraph) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ===== BOTTOM NAVIGATION BAR =====

@Composable
private fun LumaBottomNavigationBar(
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (Any) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hasRoute(item.route::class) == true
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

// ===== SPLASH SCREEN =====

@Composable
private fun SplashScreen(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.checkSession() }

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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
