package com.apex.dms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apex.dms.data.ActorRole
import com.apex.dms.data.AppStore
import com.apex.dms.ui.screens.AccessScreen
import com.apex.dms.ui.screens.dealer.DealerHomeScreen
import com.apex.dms.ui.screens.dealer.DealerOrderDetailScreen
import com.apex.dms.ui.screens.dealer.DealerOrdersScreen
import com.apex.dms.ui.screens.dealer.DealerProductDetailScreen
import com.apex.dms.ui.screens.dealer.DealerProfileScreen
import com.apex.dms.ui.screens.dealer.DealerQuoteDetailScreen
import com.apex.dms.ui.screens.dealer.DealerRequestCartScreen
import com.apex.dms.ui.screens.dealer.DealerRequestDetailScreen
import com.apex.dms.ui.screens.dealer.DealerRequestsScreen
import com.apex.dms.ui.screens.dealer.DealerShopScreen
import com.apex.dms.ui.screens.staff.StaffActivityScreen
import com.apex.dms.ui.screens.staff.StaffDataScreen
import com.apex.dms.ui.screens.staff.StaffDealerDetailScreen
import com.apex.dms.ui.screens.staff.StaffHomeScreen
import com.apex.dms.ui.screens.staff.StaffIntegrationsScreen
import com.apex.dms.ui.screens.staff.StaffMoreScreen
import com.apex.dms.ui.screens.staff.StaffOrderDetailScreen
import com.apex.dms.ui.screens.staff.StaffOrdersScreen
import com.apex.dms.ui.screens.staff.StaffProductDetailScreen
import com.apex.dms.ui.screens.staff.StaffQuoteScreen
import com.apex.dms.ui.screens.staff.StaffReportsScreen
import com.apex.dms.ui.screens.staff.StaffRequestDetailScreen
import com.apex.dms.ui.screens.staff.StaffRequestsScreen
import com.apex.dms.ui.screens.staff.StaffSalesScreen
import com.apex.dms.ui.screens.staff.StaffSettingsScreen

@Composable
fun DmsApp(store: AppStore) {
    val navController = rememberNavController()
    val startDestination = when {
        !store.onboardingCompleted || store.session.role == null -> Routes.Access
        store.session.role == ActorRole.DEALER -> Routes.DealerHome
        else -> Routes.StaffHome
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Access) {
            AccessScreen(store) { role ->
                val target = if (role == ActorRole.DEALER) Routes.DealerHome else Routes.StaffHome
                navController.navigate(target) { popUpTo(Routes.Access) { inclusive = true } }
            }
        }

        composable(Routes.DealerHome) {
            DealerShell(navController, Routes.DealerHome, store) { modifier ->
                DealerHomeScreen(
                    store = store,
                    onShop = { navController.navigate(Routes.DealerShop) },
                    onProduct = { navController.navigate(Routes.dealerProduct(it)) },
                    onRequests = { navController.navigate(Routes.DealerRequests) },
                    onOrders = { navController.navigate(Routes.DealerOrders) },
                    onCart = { navController.navigate(Routes.DealerCart) },
                    modifier = modifier,
                )
            }
        }
        composable(Routes.DealerShop) {
            DealerShell(navController, Routes.DealerShop, store) { modifier ->
                DealerShopScreen(store, { navController.navigate(Routes.dealerProduct(it)) }, { navController.navigate(Routes.DealerCart) }, modifier)
            }
        }
        composable(Routes.DealerRequests) {
            DealerShell(navController, Routes.DealerRequests, store) { modifier ->
                DealerRequestsScreen(store, { navController.navigate(Routes.dealerRequest(it)) }, { navController.navigate(Routes.dealerQuote(it)) }, modifier)
            }
        }
        composable(Routes.DealerOrders) {
            DealerShell(navController, Routes.DealerOrders, store) { modifier ->
                DealerOrdersScreen(store, { navController.navigate(Routes.dealerOrder(it)) }, modifier)
            }
        }
        composable(Routes.DealerProfile) {
            DealerShell(navController, Routes.DealerProfile, store) { modifier ->
                DealerProfileScreen(
                    store,
                    onRequests = { navController.navigate(Routes.DealerRequests) },
                    onOrders = { navController.navigate(Routes.DealerOrders) },
                    onShop = { navController.navigate(Routes.DealerShop) },
                    onExit = {
                        if (store.session.isImpersonating) {
                            store.stopImpersonation()
                            navController.navigate(Routes.StaffHome) { popUpTo(0) }
                        } else {
                            store.exitToAccess()
                            navController.navigate(Routes.Access) { popUpTo(0) }
                        }
                    },
                    modifier = modifier,
                )
            }
        }
        composable(Routes.DealerCart) {
            DealerRequestCartScreen(store, { navController.popBackStack() }) { id -> navController.navigate(Routes.dealerRequest(id)) { popUpTo(Routes.DealerCart) { inclusive = true } } }
        }
        composable(Routes.DealerProduct, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            DealerProductDetailScreen(store, backStack.arguments?.getString("id").orEmpty(), { navController.popBackStack() }, { navController.navigate(Routes.DealerCart) })
        }
        composable(Routes.DealerRequestDetail, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            DealerRequestDetailScreen(store, backStack.arguments?.getString("id").orEmpty(), { navController.popBackStack() }, { navController.navigate(Routes.dealerQuote(it)) })
        }
        composable(Routes.DealerQuoteDetail, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            DealerQuoteDetailScreen(store, backStack.arguments?.getString("id").orEmpty(), { navController.popBackStack() }, { navController.navigate(Routes.dealerOrder(it)) })
        }
        composable(Routes.DealerOrderDetail, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            DealerOrderDetailScreen(store, backStack.arguments?.getString("id").orEmpty(), { navController.popBackStack() }) { navController.navigate(Routes.DealerCart) }
        }

        composable(Routes.StaffHome) {
            StaffShell(navController, Routes.StaffHome) { modifier ->
                StaffHomeScreen(store, { navController.navigate(Routes.StaffRequests) }, { navController.navigate(Routes.StaffOrders) }, { navController.navigate(Routes.StaffData) }, { navController.navigate(Routes.staffRequest(it)) }, modifier)
            }
        }
        composable(Routes.StaffRequests) {
            StaffShell(navController, Routes.StaffRequests) { modifier -> StaffRequestsScreen(store, { navController.navigate(Routes.staffRequest(it)) }, { navController.navigate(Routes.staffQuote(it)) }, modifier) }
        }
        composable(Routes.StaffOrders) {
            StaffShell(navController, Routes.StaffOrders) { modifier -> StaffOrdersScreen(store, { navController.navigate(Routes.staffOrder(it)) }, modifier) }
        }
        composable(Routes.StaffData) {
            StaffShell(navController, Routes.StaffData) { modifier ->
                StaffDataScreen(store, { navController.navigate(Routes.staffDealer(it)) }, { navController.navigate(Routes.staffProduct(it)) }, modifier)
            }
        }
        composable(Routes.StaffMore) {
            StaffShell(navController, Routes.StaffMore) { modifier ->
                StaffMoreScreen(
                    store,
                    onReports = { navController.navigate(Routes.StaffReports) },
                    onSales = { navController.navigate(Routes.StaffSales) },
                    onActivity = { navController.navigate(Routes.StaffActivity) },
                    onIntegrations = { navController.navigate(Routes.StaffIntegrations) },
                    onSettings = { navController.navigate(Routes.StaffSettings) },
                    onExit = { store.exitToAccess(); navController.navigate(Routes.Access) { popUpTo(0) } },
                    modifier = modifier,
                )
            }
        }
        composable(Routes.StaffRequestDetail, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            StaffRequestDetailScreen(store, backStack.arguments?.getString("id").orEmpty(), { navController.popBackStack() }, { navController.navigate(Routes.staffQuote(it)) })
        }
        composable(Routes.StaffQuote, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            StaffQuoteScreen(store, backStack.arguments?.getString("id").orEmpty(), { navController.popBackStack() })
        }
        composable(Routes.StaffOrderDetail, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            StaffOrderDetailScreen(store, backStack.arguments?.getString("id").orEmpty(), { navController.popBackStack() })
        }
        composable(Routes.StaffDealerDetail, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            StaffDealerDetailScreen(store, backStack.arguments?.getString("id").orEmpty(), { navController.popBackStack() }) {
                navController.navigate(Routes.DealerHome) { popUpTo(0) }
            }
        }
        composable(Routes.StaffProductDetail, arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
            StaffProductDetailScreen(store, backStack.arguments?.getString("id").orEmpty(), { navController.popBackStack() })
        }
        composable(Routes.StaffReports) { StaffReportsScreen(store) { navController.popBackStack() } }
        composable(Routes.StaffSales) { StaffSalesScreen(store) { navController.popBackStack() } }
        composable(Routes.StaffActivity) { StaffActivityScreen(store) { navController.popBackStack() } }
        composable(Routes.StaffIntegrations) { StaffIntegrationsScreen(store) { navController.popBackStack() } }
        composable(Routes.StaffSettings) {
            StaffSettingsScreen(store, { navController.popBackStack() }) {
                store.exitToAccess(); navController.navigate(Routes.Access) { popUpTo(0) }
            }
        }
    }
}
