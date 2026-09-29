package com.example.freeinvoicegeneratorbydaybookcloud.shared.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TemplateScreen(
    selectedTemplateId: String = "modern_teal",
    onTemplateSelected: (String) -> Unit = {}
) {
    val templates = remember { defaultInvoiceTemplates() }
    var query by rememberSaveable { mutableStateOf("") }
    var displayMode by rememberSaveable { mutableStateOf(TemplateDisplayMode.LIST) }
    val filtered = templates.filter { query.isBlank() || it.title.contains(query, true) || it.description.contains(query, true) }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Invoice Templates", style = MaterialTheme.typography.headlineSmall)
            Text("Choose your preferred invoice layout template.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search templates") },
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
            TemplateDisplayToggle(displayMode) { displayMode = it }
            if (filtered.isEmpty()) {
                Text("No templates found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (displayMode == TemplateDisplayMode.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { template ->
                        TemplateCard(template, selectedTemplateId == template.id, grid = true) { onTemplateSelected(template.id) }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { template ->
                        TemplateCard(template, selectedTemplateId == template.id, grid = false) { onTemplateSelected(template.id) }
                    }
                }
            }
        }
    }
}
