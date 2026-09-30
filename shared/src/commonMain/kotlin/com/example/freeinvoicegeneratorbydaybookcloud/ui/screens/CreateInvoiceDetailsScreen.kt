package com.example.freeinvoicegeneratorbydaybookcloud.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.majorCurrencies
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.DateFormatOption
import com.example.freeinvoicegeneratorbydaybookcloud.ui.components.*
import com.example.freeinvoicegeneratorbydaybookcloud.platform.LocalPlatformActions
import com.example.freeinvoicegeneratorbydaybookcloud.platform.rememberImagePicker
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.CreateInvoiceViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.formatInputDate
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.parseDate
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun CreateInvoiceDetailsScreen(
    viewModel: CreateInvoiceViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var organizationName by remember { mutableStateOf(uiState.organizationName) }
    var organizationAddress by remember { mutableStateOf(uiState.organizationAddress) }
    var invoiceNumber by remember { mutableStateOf(uiState.invoiceNumber) }
    var invoiceDate by remember { mutableStateOf(uiState.invoiceDate) }
    var dueDate by remember { mutableStateOf(uiState.dueDate) }
    var customerName by remember { mutableStateOf(uiState.customerName) }
    var customerAddress by remember { mutableStateOf(uiState.customerAddress) }
    var logoPath by remember(uiState.organizationLogoPath) { mutableStateOf(uiState.organizationLogoPath) }
    var currencyCode by remember(uiState.currencyCode) { mutableStateOf(uiState.currencyCode) }
    var dateFormat by remember(uiState.dateFormat) { mutableStateOf(uiState.dateFormat) }
    val selectedCurrency = majorCurrencies.firstOrNull { it.code == currencyCode } ?: majorCurrencies.first()
    val platform = LocalPlatformActions.current
    val logoPicker = rememberImagePicker { picked ->
        if (picked != null) {
            logoPath = picked
            platform.showToast("Logo uploaded successfully")
        }
    }
    var showInvoiceDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    val parsedInvoiceDate = parseDisplayDate(invoiceDate)
    val parsedDueDate = parseDisplayDate(dueDate)
    val datesValid = parsedInvoiceDate != null && parsedDueDate != null && parsedDueDate >= parsedInvoiceDate

    val canProceed = organizationName.isNotBlank()
        && organizationAddress.isNotBlank()
        && customerName.isNotBlank()
        && customerAddress.isNotBlank()
        && invoiceNumber.isNotBlank()
        && invoiceDate.isNotBlank()
        && dueDate.isNotBlank()
        && datesValid

    if (showInvoiceDatePicker) {
        InvoiceDatePickerDialog(
            initialDate = invoiceDate,
            onDismiss = { showInvoiceDatePicker = false },
            onDateSelected = {
                invoiceDate = it
                showInvoiceDatePicker = false
            }
        )
    }

    if (showDueDatePicker) {
        InvoiceDatePickerDialog(
            initialDate = dueDate,
            onDismiss = { showDueDatePicker = false },
            onDateSelected = {
                dueDate = it
                showDueDatePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            DaybookTopBar(
                title = "New Invoice",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    PrimaryButton(
                        text = "Continue to Items →",
                        onClick = {
                            viewModel.updateOrganization(organizationName, organizationAddress)
                            viewModel.updateOrganizationLogo(logoPath)
                            viewModel.updateInvoiceDetails(invoiceNumber, invoiceDate, dueDate)
                            viewModel.updateCurrency(selectedCurrency.code, selectedCurrency.symbol, uiState.decimalPlaces)
                            viewModel.updateDateFormat(dateFormat)
                            viewModel.updateCustomer(customerName, customerAddress)
                            viewModel.setStep(2)
                            onNext()
                        },
                        enabled = canProceed
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            InvoiceStepIndicator(currentStep = 1, steps = listOf("Details", "Items", "Additional", "Review", "Template"))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── From (Organization) ───────────────────────────────
                FormCard(title = "FROM — YOUR BUSINESS") {
                    DaybookTextField(
                        value = organizationName,
                        onValueChange = { organizationName = it },
                        label = "Business Name",
                        placeholder = "Enter business name",
                        singleLine = true
                    )
                    DaybookTextField(
                        value = organizationAddress,
                        onValueChange = { organizationAddress = it },
                        label = "Address",
                        placeholder = "Enter business address",
                        minLines = 2
                    )
                    OutlinedButton(
                        onClick = logoPicker,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (logoPath == null) "Choose Logo" else "Change Logo")
                    }
                    if (logoPath != null) {
                        TextButton(
                            onClick = {
                                logoPath = null
                                platform.showToast("Logo removed")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remove Logo")
                        }
                    }
                    logoPath?.let { LogoPreview(it) }
                }

                // ── Bill To (Customer) ────────────────────────────────
                FormCard(title = "BILL TO") {
                    DaybookTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = "Customer Name",
                        singleLine = true
                    )
                    DaybookTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = "Address",
                        minLines = 2
                    )
                }

                // ── Invoice Details ───────────────────────────────────
                FormCard(title = "INVOICE DETAILS") {
                    DaybookTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = "Invoice Number",
                        singleLine = true,
                        placeholder = "INV-001"
                    )
                    SimpleChoiceMenu(
                        label = "Currency",
                        value = selectedCurrency.displayName,
                        options = majorCurrencies.map { it.displayName },
                        onSelect = { selected ->
                            currencyCode = majorCurrencies.first { it.displayName == selected }.code
                        }
                    )
                    SimpleChoiceMenu(
                        label = "Date Format",
                        value = dateFormat.label,
                        options = DateFormatOption.entries.map { it.label },
                        onSelect = { label ->
                            dateFormat = DateFormatOption.entries.first { it.label == label }
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DatePickerField(
                            value = invoiceDate,
                            label = "Invoice Date",
                            modifier = Modifier.weight(1f),
                            onClick = { showInvoiceDatePicker = true }
                        )
                        DatePickerField(
                            value = dueDate,
                            label = "Due Date",
                            modifier = Modifier.weight(1f),
                            onClick = { showDueDatePicker = true }
                        )
                    }
                    if (!datesValid) {
                        Text(
                            text = "Due date must be on or after the invoice date.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LogoPreview(uri: String) {
    val platform = LocalPlatformActions.current
    val bitmap = remember(uri) { platform.decodeLogo(uri) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Logo preview",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable form card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DatePickerField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Choose $label")
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val initialMillis = parseDate(initialDate)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(formatInputDate(millis))
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun parseDisplayDate(value: String): LocalDate? = runCatching {
    Instant.fromEpochMilliseconds(parseDate(value)).toLocalDateTime(TimeZone.UTC).date
}.getOrNull()

@Composable
fun FormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Unified text field
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DaybookTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) {{ Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }} else null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        singleLine = singleLine,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
