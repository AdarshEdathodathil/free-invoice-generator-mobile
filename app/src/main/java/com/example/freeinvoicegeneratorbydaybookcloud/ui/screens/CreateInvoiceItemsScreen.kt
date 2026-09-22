package com.example.freeinvoicegeneratorbydaybookcloud.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption
import com.example.freeinvoicegeneratorbydaybookcloud.ui.components.*
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.CreateInvoiceViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.InvoiceItemUiModel
import java.math.BigDecimal

@Composable
fun CreateInvoiceItemsScreen(viewModel: CreateInvoiceViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val advanced = state.invoiceType == InvoiceType.ADVANCED
    var dialogOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<InvoiceItemUiModel?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("100") }
    var discount by remember { mutableStateOf("0") }
    var tax by remember { mutableStateOf(state.taxRatePercent.toString()) }
    var international by remember(state.internationalNumbering) { mutableStateOf(state.internationalNumbering) }
    var roundOff by remember(state.roundOffMinor) {
        mutableStateOf(BigDecimal.valueOf(state.roundOffMinor, state.decimalPlaces).toPlainString())
    }

    fun open(item: InvoiceItemUiModel? = null) {
        editing = item
        name = item?.name.orEmpty()
        description = item?.description.orEmpty()
        quantity = item?.quantity?.toString() ?: "1"
        price = item?.let { BigDecimal.valueOf(it.unitPriceMinor, state.decimalPlaces).toPlainString() } ?: "100"
        discount = item?.discountPercent?.toString() ?: "0"
        tax = when (state.taxOption) {
            TaxOption.CGST_SGST -> item?.let { it.cgstPercent + it.sgstPercent }?.toString()
            TaxOption.IGST -> item?.igstPercent?.toString()
            TaxOption.NON_TAXABLE -> "0"
        } ?: state.taxRatePercent.toString()
        dialogOpen = true
    }

    Scaffold(
        topBar = { DaybookTopBar("Invoice Items", { viewModel.setStep(if (advanced) 3 else 1); onBack() }, Icons.AutoMirrored.Filled.ArrowBack) },
        bottomBar = { Surface(shadowElevation = 4.dp) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton("Back", onBack, Modifier.weight(1f))
                PrimaryButton(if (advanced) "Payment" else "Additional", {
                    if (advanced) viewModel.updateItemOptions(international, roundOff.toMinor(state.decimalPlaces))
                    viewModel.setStep(if (advanced) 5 else 3)
                    onNext()
                }, Modifier.weight(1f), state.items.isNotEmpty())
            }
        } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            InvoiceStepIndicator(
                if (advanced) 4 else 2,
                if (advanced) listOf("Business", "Customer", "Details", "Items", "Payment", "Review")
                else listOf("Details", "Items", "Additional", "Review")
            )
            LazyColumn(Modifier.fillMaxWidth().weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (advanced) item {
                    FormCard("ITEM OPTIONS") {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("International numbering", Modifier.weight(1f))
                            Switch(international, { international = it })
                        }
                        OutlinedTextField(roundOff, { roundOff = it }, label = { Text("Round Off (${state.currencyCode})") },
                            singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                }
                item { SectionHeader("Invoice Items", if (state.items.isNotEmpty()) "Add" else null, { open() }) }
                items(state.items, key = { it.id }) { item ->
                    InvoiceItemCard(item.name, item.quantity, item.unitPriceMinor, item.totalMinor, state.currencySymbol,
                        onEdit = { open(item) }, onDelete = { viewModel.removeItem(item.id) })
                }
                item {
                    OutlinedButton(onClick = { open() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add Item")
                    }
                }
            }
        }
    }

    if (dialogOpen) AlertDialog(
        onDismissRequest = { dialogOpen = false },
        title = { Text(if (editing == null) "Add Item" else "Edit Item", fontWeight = FontWeight.Bold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Item Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            if (advanced && state.showItemDescription) OutlinedTextField(description, { description = it },
                label = { Text("Description") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(quantity, { quantity = it }, label = { Text("Qty") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(price, { price = it }, label = { Text("Price") }, singleLine = true, modifier = Modifier.weight(1.4f))
            }
            if (advanced) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.showItemDiscount) OutlinedTextField(discount, { discount = it }, label = { Text("Discount %") },
                    singleLine = true, modifier = Modifier.weight(1f))
                if (state.taxOption != TaxOption.NON_TAXABLE) OutlinedTextField(tax, { tax = it }, label = { Text("Tax %") },
                    singleLine = true, modifier = Modifier.weight(1f))
            }
        } },
        confirmButton = { Button(onClick = {
            val qty = (quantity.toIntOrNull() ?: 1).coerceAtLeast(1)
            val amount = price.toMinor(state.decimalPlaces)
            val discountRate = discount.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
            val taxRate = tax.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
            if (name.isNotBlank() && amount >= 0) {
                editing?.let { viewModel.updateItem(it.id, name.trim(), qty, amount, description.trim(), discountRate, taxRate) }
                    ?: viewModel.addItem(name.trim(), qty, amount, description.trim(), discountRate, taxRate)
                dialogOpen = false
            }
        }) { Text(if (editing == null) "Add" else "Save") } },
        dismissButton = { TextButton({ dialogOpen = false }) { Text("Cancel") } },
        shape = RoundedCornerShape(8.dp)
    )
}
