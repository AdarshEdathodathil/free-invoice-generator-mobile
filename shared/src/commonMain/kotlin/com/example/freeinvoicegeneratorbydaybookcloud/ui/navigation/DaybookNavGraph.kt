package com.example.freeinvoicegeneratorbydaybookcloud.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.freeinvoicegeneratorbydaybookcloud.di.LocalAppContainer
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.platform.LocalPlatformActions
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.AboutScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.AdvancedCustomerScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.AdvancedDetailsScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.AdvancedOrganizationScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.AdvancedPaymentScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.AppearanceScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.CreateInvoiceDetailsScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.CreateInvoiceItemsScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.CreateInvoiceReviewScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.CreateInvoiceTemplateStepScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.HelpSupportScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.HomeScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.InvoicePreviewScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.InvoiceSettingsScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.InvoiceTypeSelectionScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.InvoicesListScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.MoreScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.OnboardingScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.OrganizationSettingsScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.SimpleAdditionalScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.SplashScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.TemplatesScreen
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.CreateInvoiceViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.HomeViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.InvoicesViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.SettingsViewModel

@Composable
fun DaybookNavGraph(
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val platformActions = LocalPlatformActions.current
    val container = LocalAppContainer.current

    val createInvoiceViewModel: CreateInvoiceViewModel = viewModel { container.createInvoiceViewModel() }
    val homeViewModel: HomeViewModel = viewModel { container.homeViewModel() }
    val invoicesViewModel: InvoicesViewModel = viewModel { container.invoicesViewModel() }

    val openNewInvoice = {
        createInvoiceViewModel.startNewInvoice()
        navController.navigate("create_type")
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCreate = openNewInvoice,
                onNavigateToInvoices = { navController.navigate("invoices") },
                onNavigateToTemplates = { navController.navigate("templates") },
                onNavigateToSettings = { navController.navigate("more") },
                onNavigateToPreview = { id -> navController.navigate("invoice_preview/$id") },
                onTabSelected = { route ->
                    if (route != "home") {
                        if (route == "create") openNewInvoice()
                        else navController.navigate(route) { popUpTo("home") }
                    }
                }
            )
        }
        composable("invoices") {
            InvoicesListScreen(
                viewModel = invoicesViewModel,
                onNavigateToPreview = { id -> navController.navigate("invoice_preview/$id") },
                onTabSelected = { route ->
                    if (route != "invoices") {
                        if (route == "create") openNewInvoice()
                        else navController.navigate(route) { popUpTo("home") }
                    }
                }
            )
        }
        composable("templates") {
            TemplatesScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                showBottomNavigation = true,
                onTabSelected = { route ->
                    if (route != "templates") {
                        if (route == "create") openNewInvoice()
                        else navController.navigate(route) { popUpTo("home") }
                    }
                }
            )
        }
        composable("more") {
            MoreScreen(
                viewModel = settingsViewModel,
                onNavigateToOrgSettings = { navController.navigate("org_settings") },
                onNavigateToAppearance = { navController.navigate("appearance") },
                onNavigateToHelp = { navController.navigate("help_support") },
                onNavigateToAbout = { navController.navigate("about") },
                onLogout = { platformActions.exitApplication() },
                onTabSelected = { route ->
                    if (route != "more") {
                        if (route == "create") openNewInvoice()
                        else navController.navigate(route) { popUpTo("home") }
                    }
                }
            )
        }
        composable("create_type") {
            InvoiceTypeSelectionScreen(
                viewModel = createInvoiceViewModel,
                onBack = { navController.popBackStack() },
                onSimple = { navController.navigate("create_details") },
                onAdvanced = { navController.navigate("advanced_org") }
            )
        }
        composable("create_details") {
            CreateInvoiceDetailsScreen(
                viewModel = createInvoiceViewModel,
                onBack = {
                    createInvoiceViewModel.startNewInvoice()
                    navController.popBackStack()
                },
                onNext = { navController.navigate("create_items") }
            )
        }
        composable("create_items") {
            CreateInvoiceItemsScreen(
                viewModel = createInvoiceViewModel,
                onBack = { navController.popBackStack() },
                onNext = {
                    if (createInvoiceViewModel.uiState.value.invoiceType == InvoiceType.ADVANCED) {
                        navController.navigate("advanced_payment")
                    } else navController.navigate("create_additional")
                }
            )
        }
        composable("create_additional") {
            SimpleAdditionalScreen(createInvoiceViewModel, { navController.popBackStack() }) {
                navController.navigate("create_review")
            }
        }
        composable("advanced_org") {
            AdvancedOrganizationScreen(createInvoiceViewModel, { navController.popBackStack() }) {
                navController.navigate("advanced_customer")
            }
        }
        composable("advanced_customer") {
            AdvancedCustomerScreen(createInvoiceViewModel, { navController.popBackStack() }) {
                navController.navigate("advanced_details")
            }
        }
        composable("advanced_details") {
            AdvancedDetailsScreen(createInvoiceViewModel, { navController.popBackStack() }) {
                navController.navigate("create_items")
            }
        }
        composable("advanced_payment") {
            AdvancedPaymentScreen(createInvoiceViewModel, { navController.popBackStack() }) {
                navController.navigate("create_review")
            }
        }
        composable("create_review") {
            CreateInvoiceReviewScreen(
                viewModel = createInvoiceViewModel,
                onBack = { navController.popBackStack() },
                onEditDetails = {
                    val route = if (createInvoiceViewModel.uiState.value.invoiceType == InvoiceType.ADVANCED) {
                        "advanced_org"
                    } else {
                        "create_details"
                    }
                    navController.popBackStack(route, false)
                },
                onSelectTemplate = { navController.navigate("create_template") }
            )
        }
        composable("create_template") {
            CreateInvoiceTemplateStepScreen(
                viewModel = createInvoiceViewModel,
                onBack = { navController.popBackStack() },
                onFinish = {
                    val editing = createInvoiceViewModel.editingInvoiceId.value != null
                    createInvoiceViewModel.createInvoice { invoiceId ->
                        platformActions.showToast(
                            if (editing) "Invoice updated successfully" else "Invoice created successfully"
                        )
                        navController.navigate("invoice_preview/$invoiceId") {
                            popUpTo("home")
                        }
                    }
                }
            )
        }
        composable(
            route = "invoice_preview/{invoiceId}",
            arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 1L
            InvoicePreviewScreen(
                invoiceId = invoiceId,
                viewModel = createInvoiceViewModel,
                onBack = { navController.popBackStack() },
                onEdit = {
                    createInvoiceViewModel.beginEditing(invoiceId) {
                        val route = if (createInvoiceViewModel.uiState.value.invoiceType == InvoiceType.ADVANCED) {
                            "advanced_org"
                        } else {
                            "create_details"
                        }
                        navController.navigate(route)
                    }
                },
                onDelete = {
                    createInvoiceViewModel.deleteInvoice(invoiceId) {
                        platformActions.showToast("Invoice deleted successfully")
                        navController.navigate("invoices") {
                            popUpTo("home")
                        }
                    }
                }
            )
        }
        composable("org_settings") {
            OrganizationSettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("invoice_settings") {
            InvoiceSettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("appearance") {
            AppearanceScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("help_support") {
            HelpSupportScreen(onBack = { navController.popBackStack() })
        }
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
