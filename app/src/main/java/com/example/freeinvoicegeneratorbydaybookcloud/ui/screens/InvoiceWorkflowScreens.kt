package com.example.freeinvoicegeneratorbydaybookcloud.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.majorCurrencies
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.*
import com.example.freeinvoicegeneratorbydaybookcloud.ui.components.*
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.CreateInvoiceViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val workflowDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
private val simpleSteps = listOf("Details", "Items", "Additional", "Review")
private val advancedSteps = listOf("Business", "Customer", "Details", "Items", "Payment", "Review")

@Composable
fun InvoiceTypeSelectionScreen(
    viewModel: CreateInvoiceViewModel,
    onBack: () -> Unit,
    onSimple: () -> Unit,
    onAdvanced: () -> Unit
) {
    Scaffold(
        topBar = { DaybookTopBar("Create Invoice", onBack, Icons.AutoMirrored.Filled.ArrowBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Choose invoice type", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            InvoiceTypeCard(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = "Simple Invoice",
                description = "Minimal invoice for quick billing.",
                onClick = { viewModel.selectInvoiceType(InvoiceType.SIMPLE); onSimple() }
            )
            InvoiceTypeCard(
                icon = Icons.Default.Description,
                title = "Advanced Invoice",
                description = "GST, taxes, payment details and customization.",
                onClick = { viewModel.selectInvoiceType(InvoiceType.ADVANCED); onAdvanced() }
            )
        }
    }
}

@Composable
private fun InvoiceTypeCard(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SimpleAdditionalScreen(viewModel: CreateInvoiceViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var notes by remember(state.additionalNotes) { mutableStateOf(state.additionalNotes) }
    var terms by remember(state.termsAndConditions) { mutableStateOf(state.termsAndConditions) }
    WorkflowScaffold("Additional", 3, simpleSteps, onBack, "Review", {
        viewModel.updateAdditional(notes, terms); viewModel.setStep(4); onNext()
    }) {
        FormCard("ADDITIONAL") {
            DaybookTextField(notes, { notes = it }, "Notes", minLines = 3)
            DaybookTextField(terms, { terms = it }, "Terms and Conditions", minLines = 3)
        }
    }
}

@Composable
fun AdvancedOrganizationScreen(viewModel: CreateInvoiceViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember(state.organizationName) { mutableStateOf(state.organizationName) }
    var address by remember(state.organizationAddress) { mutableStateOf(state.organizationAddress) }
    var country by remember(state.organizationCountry) { mutableStateOf(state.organizationCountry) }
    var email by remember(state.organizationEmail) { mutableStateOf(state.organizationEmail) }
    var mobile by remember(state.organizationMobile) { mutableStateOf(state.organizationMobile) }
    var gstin by remember(state.organizationGstin) { mutableStateOf(state.organizationGstin) }
    var authority by remember(state.authorityName) { mutableStateOf(state.authorityName) }
    var designation by remember(state.authorityDesignation) { mutableStateOf(state.authorityDesignation) }
    var logo by remember(state.organizationLogoPath) { mutableStateOf(state.organizationLogoPath) }
    val context = LocalContext.current
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            logo = it.toString()
        }
    }
    WorkflowScaffold("Organization", 1, advancedSteps, onBack, "Customer", {
        viewModel.updateAdvancedOrganization(name, address, country, email, mobile, gstin, authority, designation, logo)
        viewModel.setStep(2); onNext()
    }, nextEnabled = name.isNotBlank() && address.isNotBlank()) {
        FormCard("YOUR BUSINESS") {
            DaybookTextField(name, { name = it }, "Business Name", singleLine = true)
            DaybookTextField(address, { address = it }, "Address", minLines = 2)
            DaybookTextField(country, { country = it }, "Country", singleLine = true)
            DaybookTextField(email, { email = it }, "Email", singleLine = true)
            DaybookTextField(mobile, { mobile = it }, "Mobile", singleLine = true)
            DaybookTextField(gstin, { gstin = it }, "GSTIN", singleLine = true)
            DaybookTextField(authority, { authority = it }, "Authority Name", singleLine = true)
            DaybookTextField(designation, { designation = it }, "Authority Designation", singleLine = true)
            OutlinedButton(onClick = { logoPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Business, null); Spacer(Modifier.width(8.dp))
                Text(if (logo == null) "Choose Logo" else "Change Logo")
            }
        }
    }
}

@Composable
fun AdvancedCustomerScreen(viewModel: CreateInvoiceViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember(state.customerName) { mutableStateOf(state.customerName) }
    var address by remember(state.customerAddress) { mutableStateOf(state.customerAddress) }
    var country by remember(state.customerCountry) { mutableStateOf(state.customerCountry) }
    var mobile by remember(state.customerMobile) { mutableStateOf(state.customerMobile) }
    var email by remember(state.customerEmail) { mutableStateOf(state.customerEmail) }
    var gstin by remember(state.customerGstin) { mutableStateOf(state.customerGstin) }
    WorkflowScaffold("Customer", 2, advancedSteps, onBack, "Invoice Details", {
        viewModel.updateAdvancedCustomer(name, address, country, mobile, email, gstin); viewModel.setStep(3); onNext()
    }, nextEnabled = name.isNotBlank() && address.isNotBlank()) {
        FormCard("BILL TO") {
            DaybookTextField(name, { name = it }, "Customer Name", singleLine = true)
            DaybookTextField(address, { address = it }, "Address", minLines = 2)
            DaybookTextField(country, { country = it }, "Country", singleLine = true)
            DaybookTextField(mobile, { mobile = it }, "Mobile", singleLine = true)
            DaybookTextField(email, { email = it }, "Email", singleLine = true)
            DaybookTextField(gstin, { gstin = it }, "GSTIN", singleLine = true)
        }
    }
}

@Composable
fun AdvancedDetailsScreen(viewModel: CreateInvoiceViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var number by remember(state.invoiceNumber) { mutableStateOf(state.invoiceNumber) }
    var invoiceDate by remember(state.invoiceDate) { mutableStateOf(state.invoiceDate) }
    var dueDate by remember(state.dueDate) { mutableStateOf(state.dueDate) }
    var currencyCode by remember(state.currencyCode) { mutableStateOf(state.currencyCode) }
    var decimals by remember(state.decimalPlaces) { mutableIntStateOf(state.decimalPlaces) }
    var deliveryState by remember(state.deliveryState) { mutableStateOf(state.deliveryState) }
    var taxOption by remember(state.taxOption) { mutableStateOf(state.taxOption) }
    var dateFormat by remember(state.dateFormat) { mutableStateOf(state.dateFormat) }
    var showDescription by remember(state.showItemDescription) { mutableStateOf(state.showItemDescription) }
    var showDiscount by remember(state.showItemDiscount) { mutableStateOf(state.showItemDiscount) }
    var showInvoicePicker by remember { mutableStateOf(false) }
    var showDuePicker by remember { mutableStateOf(false) }
    val currency = majorCurrencies.firstOrNull { it.code == currencyCode } ?: majorCurrencies.first()
    if (showInvoicePicker) WorkflowDatePicker(invoiceDate, { showInvoicePicker = false }) { invoiceDate = it; showInvoicePicker = false }
    if (showDuePicker) WorkflowDatePicker(dueDate, { showDuePicker = false }) { dueDate = it; showDuePicker = false }
    WorkflowScaffold("Invoice Details", 3, advancedSteps, onBack, "Items", {
        viewModel.updateConfiguration(number, invoiceDate, dueDate, currency.code, currency.symbol, decimals,
            deliveryState, taxOption, dateFormat, showDescription, showDiscount)
        viewModel.setStep(4); onNext()
    }, nextEnabled = number.isNotBlank()) {
        FormCard("INVOICE") {
            DaybookTextField(number, { number = it }, "Invoice Number", singleLine = true)
            WorkflowDateField(invoiceDate, "Invoice Date") { showInvoicePicker = true }
            WorkflowDateField(dueDate, "Due Date") { showDuePicker = true }
            ChoiceMenu("Currency", currencyCode, majorCurrencies.map { it.code }) { currencyCode = it }
            ChoiceMenu("Decimal Places", decimals.toString(), (0..3).map(Int::toString)) { decimals = it.toInt() }
            DaybookTextField(deliveryState, { deliveryState = it }, "Delivery State", singleLine = true)
            ChoiceMenu("Tax Option", taxOption.label(), TaxOption.entries.map { it.label() }) { label ->
                taxOption = TaxOption.entries.first { it.label() == label }
            }
            ChoiceMenu("Date Format", dateFormat.label, DateFormatOption.entries.map { it.label }) { label ->
                dateFormat = DateFormatOption.entries.first { it.label == label }
            }
            BooleanSettingRow("Show item descriptions", showDescription) { showDescription = it }
            BooleanSettingRow("Show item discounts", showDiscount) { showDiscount = it }
        }
    }
}

@Composable
fun AdvancedPaymentScreen(viewModel: CreateInvoiceViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var method by remember(state.paymentMethod) { mutableStateOf(state.paymentMethod) }
    var account by remember(state.accountNumber) { mutableStateOf(state.accountNumber) }
    var owner by remember(state.accountOwnerName) { mutableStateOf(state.accountOwnerName) }
    var bank by remember(state.bankName) { mutableStateOf(state.bankName) }
    var upi by remember(state.upiId) { mutableStateOf(state.upiId) }
    var notes by remember(state.additionalNotes) { mutableStateOf(state.additionalNotes) }
    var terms by remember(state.termsAndConditions) { mutableStateOf(state.termsAndConditions) }
    WorkflowScaffold("Payment & Additional", 5, advancedSteps, onBack, "Review", {
        viewModel.updatePaymentAndAdditional(method, account, owner, bank, upi, notes, terms)
        viewModel.setStep(6); onNext()
    }) {
        FormCard("PAYMENT") {
            ChoiceMenu("Payment Method", method.name, PaymentMethod.entries.map { it.name }) { method = PaymentMethod.valueOf(it) }
            if (method == PaymentMethod.BANK) {
                DaybookTextField(account, { account = it }, "Account Number", singleLine = true)
                DaybookTextField(owner, { owner = it }, "Account Owner Name", singleLine = true)
                DaybookTextField(bank, { bank = it }, "Bank Name", singleLine = true)
            }
            if (method == PaymentMethod.UPI) DaybookTextField(upi, { upi = it }, "UPI ID", singleLine = true)
        }
        FormCard("ADDITIONAL") {
            DaybookTextField(notes, { notes = it }, "Notes", minLines = 3)
            DaybookTextField(terms, { terms = it }, "Terms and Conditions", minLines = 3)
        }
    }
}

@Composable
private fun WorkflowScaffold(
    title: String, step: Int, steps: List<String>, onBack: () -> Unit, nextLabel: String,
    onNext: () -> Unit, nextEnabled: Boolean = true, content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = { DaybookTopBar(title, onBack, Icons.AutoMirrored.Filled.ArrowBack) },
        bottomBar = { Surface(shadowElevation = 4.dp) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton("Back", onBack, Modifier.weight(1f))
                PrimaryButton(nextLabel, onNext, Modifier.weight(1f), enabled = nextEnabled)
            }
        } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            InvoiceStepIndicator(step, steps)
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
        }
    }
}

@Composable
private fun BooleanSettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f)); Switch(checked, onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceMenu(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(value, {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true))
        ExposedDropdownMenu(expanded, { expanded = false }) {
            options.forEach { DropdownMenuItem({ Text(it) }, { onSelect(it); expanded = false }) }
        }
    }
}

@Composable
private fun WorkflowDateField(value: String, label: String, onClick: () -> Unit) {
    Box {
        OutlinedTextField(value, {}, readOnly = true, label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.CalendarMonth, "Choose $label") }, modifier = Modifier.fillMaxWidth())
        Box(Modifier.matchParentSize().clickable(onClick = onClick))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkflowDatePicker(initial: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val millis = runCatching { LocalDate.parse(initial, workflowDateFormatter).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull()
    val state = rememberDatePickerState(initialSelectedDateMillis = millis)
    DatePickerDialog(onDismiss, confirmButton = { TextButton({ state.selectedDateMillis?.let {
        onSelect(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().format(workflowDateFormatter))
    } }) { Text("OK") } }, dismissButton = { TextButton(onDismiss) { Text("Cancel") } }) { DatePicker(state) }
}

private fun TaxOption.label() = when (this) {
    TaxOption.CGST_SGST -> "CGST & SGST"
    TaxOption.IGST -> "IGST"
    TaxOption.NON_TAXABLE -> "Non Taxable"
}

internal fun String.toMinor(decimalPlaces: Int): Long = runCatching {
    BigDecimal(trim()).movePointRight(decimalPlaces.coerceIn(0, 3)).setScale(0, RoundingMode.HALF_UP).longValueExact()
}.getOrDefault(0L)
