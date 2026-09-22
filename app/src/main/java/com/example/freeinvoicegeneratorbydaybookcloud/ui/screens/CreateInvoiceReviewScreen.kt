package com.example.freeinvoicegeneratorbydaybookcloud.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeinvoicegeneratorbydaybookcloud.ui.components.*
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.CreateInvoiceViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.formatStoredDate
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.parseDate
import com.example.freeinvoicegeneratorbydaybookcloud.util.amountInWords
import com.example.freeinvoicegeneratorbydaybookcloud.util.formatMoney
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import java.math.BigDecimal

@Composable
fun CreateInvoiceReviewScreen(
    viewModel: CreateInvoiceViewModel,
    onBack: () -> Unit,
    onEditDetails: () -> Unit,
    onSelectTemplate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveError by viewModel.saveError.collectAsStateWithLifecycle()
    val advanced = uiState.invoiceType == InvoiceType.ADVANCED
    val formattedInvoiceDate = formatStoredDate(parseDate(uiState.invoiceDate), uiState.dateFormat)
    val formattedDueDate = formatStoredDate(parseDate(uiState.dueDate), uiState.dateFormat)
    var roundOff by remember(uiState.decimalPlaces) {
        mutableStateOf(BigDecimal.valueOf(uiState.roundOffMinor, uiState.decimalPlaces).toPlainString())
    }

    Scaffold(
        topBar = {
            DaybookTopBar(
                title = "Review Invoice",
                onNavigationClick = {
                    viewModel.setStep(2)
                    onBack()
                },
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "← Back",
                        onClick = {
                            viewModel.setStep(2)
                            onBack()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = when {
                            isSaving -> "Saving…"
                            else -> "Select Template"
                        },
                        onClick = {
                            viewModel.updateItemOptions(uiState.internationalNumbering, roundOff.toMinor(uiState.decimalPlaces))
                            onSelectTemplate()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
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
            InvoiceStepIndicator(
                currentStep = if (advanced) 6 else 4,
                steps = if (advanced) listOf("Business", "Customer", "Details", "Items", "Payment", "Review", "Template")
                else listOf("Details", "Items", "Additional", "Review", "Template")
            )

            if (saveError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = saveError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Invoice Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = {
                            viewModel.setStep(1)
                            onEditDetails()
                        }) {
                            Text(text = "Edit", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.8.dp
                    )

                    // From & Bill To
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FROM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = uiState.organizationName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = uiState.organizationAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (advanced) {
                                ReviewDetail(uiState.organizationCountry)
                                ReviewDetail(uiState.organizationEmail)
                                ReviewDetail(uiState.organizationMobile)
                                ReviewDetail(uiState.organizationGstin, "GSTIN: ")
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BILL TO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = uiState.customerName.ifEmpty { "—" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = uiState.customerAddress.ifEmpty { "-" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (advanced) {
                                ReviewDetail(uiState.customerCountry)
                                ReviewDetail(uiState.customerMobile)
                                ReviewDetail(uiState.customerEmail)
                                ReviewDetail(uiState.customerGstin, "GSTIN: ")
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)

                    // Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "NUMBER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                            Text(text = uiState.invoiceNumber, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Column {
                            Text(text = "DATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                            Text(text = formattedInvoiceDate, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Column {
                            Text(text = "DUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                            Text(text = formattedDueDate, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)

                    // Items
                    Text(
                        text = "ITEMS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                    uiState.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.name} (${item.quantity}×)",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatMoney(item.totalMinor, uiState.currencySymbol, uiState.decimalPlaces, uiState.internationalNumbering),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)

                    // Totals
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = formatMoney(uiState.subtotalMinor, uiState.currencySymbol, uiState.decimalPlaces, uiState.internationalNumbering), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    if (uiState.discountMinor != 0L) ReviewTotalRow("Discount", uiState.discountMinor, uiState.currencySymbol, uiState.decimalPlaces, uiState.internationalNumbering)
                    if (advanced && uiState.taxAmountMinor != 0L) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Tax (${uiState.taxRatePercent}%)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = formatMoney(uiState.taxAmountMinor, uiState.currencySymbol, uiState.decimalPlaces, uiState.internationalNumbering), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedTextField(
                        value = roundOff,
                        onValueChange = {
                            roundOff = signedDecimalInput(it)
                            viewModel.updateItemOptions(uiState.internationalNumbering, roundOff.toMinor(uiState.decimalPlaces))
                        },
                        label = { Text("Round Off (${uiState.currencyCode})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grand Total",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatMoney(uiState.totalMinor, uiState.currencySymbol, uiState.decimalPlaces, uiState.internationalNumbering),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    ReviewBlock(
                        "AMOUNT IN WORDS",
                        amountInWords(uiState.totalMinor, uiState.currencyCode, uiState.decimalPlaces),
                        valueFontSizeSp = 14
                    )
                    if (advanced && uiState.paymentMethod != com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentMethod.NONE) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text("PAYMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ReviewDetail(uiState.paymentMethod.name, "Method: ")
                        ReviewDetail(uiState.accountOwnerName, "Account Owner: ")
                        ReviewDetail(uiState.accountNumber, "Account: ")
                        ReviewDetail(uiState.bankName, "Bank: ")
                        ReviewDetail(uiState.upiId, "UPI: ")
                    }
                    if (uiState.additionalNotes.isNotBlank()) ReviewBlock("NOTES", uiState.additionalNotes)
                    if (uiState.termsAndConditions.isNotBlank()) ReviewBlock("TERMS AND CONDITIONS", uiState.termsAndConditions)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReviewDetail(value: String, prefix: String = "") {
    if (value.isNotBlank()) Text(prefix + value, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ReviewTotalRow(label: String, amount: Long, symbol: String, decimals: Int, international: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatMoney(amount, symbol, decimals, international), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ReviewBlock(label: String, value: String, valueFontSizeSp: Int = 12) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = valueFontSizeSp.sp)
    }
}

private fun signedDecimalInput(value: String): String {
    val negative = value.startsWith("-")
    val cleaned = value.filter { it.isDigit() || it == '.' }
    val firstDot = cleaned.indexOf('.')
    val decimal = if (firstDot == -1) cleaned else cleaned.take(firstDot + 1) + cleaned.drop(firstDot + 1).replace(".", "")
    return if (negative) "-$decimal" else decimal
}
