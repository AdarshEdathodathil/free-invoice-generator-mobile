package com.example.freeinvoicegeneratorbydaybookcloud.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeinvoicegeneratorbydaybookcloud.ui.components.*
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.InvoicesViewModel

@Composable
fun InvoicesListScreen(
    viewModel: InvoicesViewModel,
    onNavigateToPreview: (Long) -> Unit,
    onTabSelected: (String) -> Unit
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val filteredInvoices = invoices.filter { inv ->
        searchQuery.isBlank()
            || inv.invoiceNumber.contains(searchQuery, true)
            || inv.customerName.contains(searchQuery, true)
    }

    Scaffold(
        bottomBar = {
            DaybookBottomNavigation(
                currentRoute = "invoices",
                onTabSelected = onTabSelected
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onTabSelected("create") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Invoice") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top area: title + search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Invoices",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Inline search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Search invoice or customer…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredInvoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.Description,
                        title = if (searchQuery.isNotBlank()) "No results found"
                               else "No invoices yet",
                        description = if (searchQuery.isNotBlank())
                            "Try a different search term."
                        else
                            "Tap 'New Invoice' to create your first one."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(2.dp)) }
                    items(filteredInvoices) { invoice ->
                        InvoiceListItem(
                            invoiceNumber = invoice.invoiceNumber,
                            customerName = invoice.customerName,
                            amountCents = invoice.amountCents,
                            date = invoice.date,
                            onClick = { onNavigateToPreview(invoice.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}
