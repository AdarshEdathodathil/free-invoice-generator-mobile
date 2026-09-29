package com.example.freeinvoicegeneratorbydaybookcloud.shared.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class TemplateDisplayMode { LIST, GRID }

@Composable
fun TemplateDisplayToggle(mode: TemplateDisplayMode, onModeChange: (TemplateDisplayMode) -> Unit) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)).padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DisplayButton(mode == TemplateDisplayMode.LIST, "List view", { onModeChange(TemplateDisplayMode.LIST) }) {
            Icon(Icons.AutoMirrored.Filled.ViewList, null, Modifier.size(18.dp))
        }
        DisplayButton(mode == TemplateDisplayMode.GRID, "Grid view", { onModeChange(TemplateDisplayMode.GRID) }) {
            Icon(Icons.Default.GridView, null, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DisplayButton(selected: Boolean, description: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .semantics { contentDescription = description }
    ) {
        CompositionLocalProvider(LocalContentColor provides if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) {
            content()
        }
    }
}

@Composable
fun TemplateCard(template: InvoiceTemplate, selected: Boolean, grid: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = template.container),
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, if (selected) template.accent else template.accent.copy(alpha = 0.28f))
    ) {
        if (grid) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TemplatePreview(template.accent)
                TemplateText(template, TextAlign.Center)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TemplatePreview(template.accent, large = true)
                TemplateText(template, TextAlign.Start, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TemplateText(template: InvoiceTemplate, alignment: TextAlign, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(template.title, Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, textAlign = alignment, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(template.description, Modifier.fillMaxWidth(), textAlign = alignment, maxLines = if (alignment == TextAlign.Center) 2 else 3, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TemplatePreview(accent: Color, large: Boolean = false) {
    Column(
        modifier = Modifier.width(if (large) 80.dp else 58.dp).height(if (large) 104.dp else 62.dp)
            .clip(RoundedCornerShape(if (large) 10.dp else 6.dp)).background(Color.White).padding(if (large) 8.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(if (large) 14.dp else 8.dp).background(accent))
        repeat(3) { index -> Box(Modifier.fillMaxWidth(if (index == 0) 0.85f else 0.62f + index * 0.12f).height(5.dp).background(accent.copy(alpha = 0.20f), RoundedCornerShape(50))) }
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth(0.72f).height(7.dp).align(Alignment.End).background(accent.copy(alpha = 0.55f), RoundedCornerShape(50)))
    }
}
