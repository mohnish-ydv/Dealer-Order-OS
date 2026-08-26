package com.apex.dms.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apex.dms.data.AppStore
import com.apex.dms.ui.theme.ShoppeBackground
import com.apex.dms.ui.theme.ShoppeBlue
import com.apex.dms.ui.theme.ShoppeMuted

private data class NavItem(val label: String, val route: String, val icon: ImageVector)

private val dealerItems = listOf(
    NavItem("Home", Routes.DealerHome, Icons.Rounded.Home),
    NavItem("Shop", Routes.DealerShop, Icons.Rounded.Storefront),
    NavItem("Requests", Routes.DealerRequests, Icons.Rounded.ListAlt),
    NavItem("Orders", Routes.DealerOrders, Icons.Rounded.ShoppingBag),
    NavItem("Profile", Routes.DealerProfile, Icons.Rounded.Person),
)

private val staffItems = listOf(
    NavItem("Home", Routes.StaffHome, Icons.Rounded.Home),
    NavItem("Requests", Routes.StaffRequests, Icons.Rounded.ListAlt),
    NavItem("Orders", Routes.StaffOrders, Icons.Rounded.ShoppingBag),
    NavItem("Data", Routes.StaffData, Icons.Rounded.Storefront),
    NavItem("More", Routes.StaffMore, Icons.Rounded.MoreHoriz),
)

@Composable
fun DealerShell(
    navController: NavHostController,
    currentRoute: String,
    store: AppStore,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        containerColor = ShoppeBackground,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                dealerItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { if (currentRoute != item.route) navController.navigate(item.route) { launchSingleTop = true } },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ShoppeBlue,
                            selectedTextColor = ShoppeBlue,
                            unselectedIconColor = ShoppeMuted,
                            unselectedTextColor = ShoppeMuted,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            if (store.session.isImpersonating) {
                Surface(color = Color(0xFFFFF3D6), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Viewing as dealer · Tap Profile to exit",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                        color = Color(0xFF8A5A00),
                    )
                }
            }
            Box(Modifier.fillMaxSize()) { content(Modifier.fillMaxSize()) }
        }
    }
}

@Composable
fun StaffShell(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        containerColor = ShoppeBackground,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                staffItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { if (currentRoute != item.route) navController.navigate(item.route) { launchSingleTop = true } },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ShoppeBlue,
                            selectedTextColor = ShoppeBlue,
                            unselectedIconColor = ShoppeMuted,
                            unselectedTextColor = ShoppeMuted,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { inner -> Box(Modifier.fillMaxSize().padding(inner)) { content(Modifier.fillMaxSize()) } }
}
