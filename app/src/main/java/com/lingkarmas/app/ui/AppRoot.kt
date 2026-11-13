package com.lingkarmas.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lingkarmas.app.ui.screens.*
import com.lingkarmas.app.ui.theme.*
import com.lingkarmas.app.data.Repo

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val tabs = listOf(
        Tab("home", "Beranda", Icons.Default.Home),
        Tab("catalog", "Katalog", Icons.Default.GridView),
        Tab("history", "Pesanan", Icons.Default.Receipt),
        Tab("profile", "Profil", Icons.Default.Person),
    )

    // Sesi berakhir (token ditolak server) -> kembali ke login
    LaunchedEffect(Repo.user, route) {
        if (Repo.user == null && route != null && route != "login" && route != "register") {
            nav.navigate("login") { popUpTo(0); launchSingleTop = true }
        }
    }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            if (route in tabs.map { it.route }) NavigationBar(containerColor = Color.White) {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = route == t.route,
                        onClick = { nav.navigate(t.route) { popUpTo("home"); launchSingleTop = true } },
                        icon = { Icon(t.icon, contentDescription = null) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy, selectedTextColor = Navy,
                            indicatorColor = Gold.copy(alpha = 0.35f))
                    )
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = if (Repo.user != null) "home" else "login", modifier = Modifier.padding(pad)) {
            composable("login") {
                LoginScreen(
                    onLogin = { nav.navigate("home") { popUpTo("login") { inclusive = true } } },
                    onRegister = { nav.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(onBack = { nav.popBackStack() }, onDone = { nav.popBackStack() })
            }
            composable("home") { HomeScreen({ nav.navigate("order/$it") }, { nav.navigate("catalog") }) }
            composable("catalog") { CatalogScreen { nav.navigate("order/$it") } }
            composable("order/{id}", listOf(navArgument("id") { type = NavType.LongType })) {
                OrderScreen(it.arguments!!.getLong("id"), { nav.popBackStack() }, {
                    nav.navigate("history") { popUpTo("home") }
                })
            }
            composable("history") { HistoryScreen() }
            composable("profile") {
                ProfileScreen(onLogout = { nav.navigate("login") { popUpTo(0) } })
            }
        }
    }
}