package com.example.freeinvoicegeneratorbydaybookcloud.shared.templates

import androidx.compose.ui.graphics.Color

data class InvoiceTemplate(
    val id: String,
    val title: String,
    val description: String,
    val accent: Color,
    val container: Color
)

fun defaultInvoiceTemplates(): List<InvoiceTemplate> = listOf(
    InvoiceTemplate("modern_teal", "Modern Teal", "Clean minimal layout matching the Daybook.Cloud brand.", Color(0xFF0F8F83), Color(0xFFE6FFFB)),
    InvoiceTemplate("coral_breeze", "Coral Breeze", "Soft coral highlights with mint table accents.", Color(0xFFE85D5D), Color(0xFFFFF1F1)),
    InvoiceTemplate("blue_split", "Blue Split", "A strong blue split layout for modern businesses.", Color(0xFF2563EB), Color(0xFFEFF6FF)),
    InvoiceTemplate("royal_plum", "Royal Plum", "Confident plum accents for polished invoices.", Color(0xFF7C3AED), Color(0xFFF5F3FF)),
    InvoiceTemplate("minimal_letter", "Minimal Letter", "A restrained, print-friendly invoice layout.", Color(0xFF475569), Color(0xFFF8FAFC)),
    InvoiceTemplate("total_focus", "Total Focus", "A prominent total area for retail and service invoices.", Color(0xFFEA580C), Color(0xFFFFF7ED))
)
