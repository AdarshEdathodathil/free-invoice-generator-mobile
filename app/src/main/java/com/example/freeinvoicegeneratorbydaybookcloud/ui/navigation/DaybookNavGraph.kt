package com.example.freeinvoicegeneratorbydaybookcloud.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.freeinvoicegeneratorbydaybookcloud.ui.screens.*
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.*
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType

@Composable
fun DaybookNavGraph(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val createInvoiceViewModel: CreateInvoiceViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val invoicesViewModel: InvoicesViewModel = viewModel()

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
                onNavigateToInvoiceSettings = { navController.navigate("invoice_settings") },
                onNavigateToTemplates = { navController.navigate("templates") },
                onNavigateToAppearance = { navController.navigate("appearance") },
                onNavigateToHelp = { navController.navigate("help_support") },
                onNavigateToAbout = { navController.navigate("about") },
                onLogout = {
                    (context as? Activity)?.finishAffinity()
                },
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
                    val route = if (createInvoiceViewModel.uiState.value.invoiceType == InvoiceType.ADVANCED) "advanced_org" else "create_details"
                    navController.popBackStack(route, false)
                },
                onSelectTemplate = {
                    navController.navigate("create_template")
                }
            )
        }
        composable("create_template") {
            CreateInvoiceTemplateStepScreen(
                viewModel = createInvoiceViewModel,
                onBack = { navController.popBackStack() },
                onFinish = {
                    val editing = createInvoiceViewModel.editingInvoiceId.value != null
                    createInvoiceViewModel.createInvoice { invoiceId ->
                        Toast.makeText(
                            context,
                            if (editing) "Invoice updated successfully" else "Invoice created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
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
                        val route = if (createInvoiceViewModel.uiState.value.invoiceType == InvoiceType.ADVANCED) "advanced_org" else "create_details"
                        navController.navigate(route)
                    }
                },
                onDelete = {
                    createInvoiceViewModel.deleteInvoice(invoiceId) {
                        Toast.makeText(context, "Invoice deleted successfully", Toast.LENGTH_SHORT).show()
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
