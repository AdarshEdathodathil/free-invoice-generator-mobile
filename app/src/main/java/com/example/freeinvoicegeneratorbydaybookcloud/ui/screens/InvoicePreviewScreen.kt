package com.example.freeinvoicegeneratorbydaybookcloud.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeinvoicegeneratorbydaybookcloud.ui.components.DaybookTopBar
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.CreateInvoiceViewModel
import com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel.InvoicePreviewEvent
import com.example.freeinvoicegeneratorbydaybookcloud.util.formatMoney
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentMethod
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption
import com.example.freeinvoicegeneratorbydaybookcloud.pdf.InvoicePdfGenerator
import com.example.freeinvoicegeneratorbydaybookcloud.util.LogoResolver

@Composable
fun InvoicePreviewScreen(
    invoiceId: Long,
    viewModel: CreateInvoiceViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val pdfActionState by viewModel.pdfActionState.collectAsStateWithLifecycle()
    val selectedTemplateId by viewModel.selectedTemplateId.collectAsStateWithLifecycle()
    val templateStyle = invoicePreviewTemplateStyle(selectedTemplateId)
    val invoice = invoices.firstOrNull { it.id == invoiceId }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.previewEvents.collect { event ->
            when (event) {
                is InvoicePreviewEvent.DownloadSuccess -> {
                    Toast.makeText(context, "Invoice downloaded successfully", Toast.LENGTH_SHORT).show()
                }
                InvoicePreviewEvent.DownloadError -> {
                    Toast.makeText(context, "Unable to download invoice", Toast.LENGTH_SHORT).show()
                }
                is InvoicePreviewEvent.ShareReady -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = InvoicePdfGenerator.PDF_MIME_TYPE
                        putExtra(Intent.EXTRA_STREAM, event.uri)
                        putExtra(Intent.EXTRA_TITLE, event.fileName)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Share invoice"))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "No apps are available to share this invoice.", Toast.LENGTH_SHORT).show()
                    }
                }
                InvoicePreviewEvent.ShareError -> {
                    Toast.makeText(context, "Unable to share invoice", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Invoice?") },
            text = { Text("This action cannot be undone. The invoice and all its items will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDelete()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (invoice == null) {
        Scaffold(
            topBar = {
                DaybookTopBar(
                    title = "Invoice",
                    onNavigationClick = onBack,
                    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        return
    }

    Scaffold(
        topBar = {
            DaybookTopBar(
                title = "Invoice Preview",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.shareInvoicePdf(invoiceId) },
                        enabled = !pdfActionState.isGeneratingPdf
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PreviewActionButton(
                        icon = Icons.Default.Download,
                        label = if (pdfActionState.isGeneratingPdf) "Generating PDF..." else "Download",
                        enabled = !pdfActionState.isGeneratingPdf,
                        onClick = { viewModel.downloadInvoicePdf(invoiceId) },
                        modifier = Modifier.weight(1f)
                    )
                    PreviewActionButton(
                        icon = Icons.Default.Share,
                        label = if (pdfActionState.isGeneratingPdf) "Generating PDF..." else "Share",
                        enabled = !pdfActionState.isGeneratingPdf,
                        onClick = { viewModel.shareInvoicePdf(invoiceId) },
                        modifier = Modifier.weight(1f)
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
                .padding(16.dp)
        ) {
            // ── Invoice Document Card ─────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(templateStyle.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = templateStyle.documentColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    // Colored header band with invoice title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                templateStyle.headerColor,
                                RoundedCornerShape(
                                    topStart = templateStyle.cardCornerRadius,
                                    topEnd = templateStyle.cardCornerRadius
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "INVOICE",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = templateStyle.onHeaderColor,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "# ${invoice.invoiceNumber}",
                                    fontSize = 13.sp,
                                    color = templateStyle.onHeaderColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Document body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // From / To row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                invoice.organizationLogoPath?.let { OrganizationLogo(it) }
                                Text(
                                    text = "FROM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = invoice.organizationName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = invoice.organizationAddress,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (invoice.invoiceType == InvoiceType.ADVANCED) {
                                    PreviewDetail(invoice.organizationCountry)
                                    PreviewDetail(invoice.organizationEmail)
                                    PreviewDetail(invoice.organizationMobile)
                                    PreviewDetail(invoice.organizationGstin, "GSTIN: ")
                                    PreviewDetail(listOf(invoice.authorityName, invoice.authorityDesignation).filter { it.isNotBlank() }.joinToString(", "))
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "BILL TO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = invoice.customerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = invoice.customerAddress,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.End
                                )
                                if (invoice.invoiceType == InvoiceType.ADVANCED) {
                                    PreviewDetail(invoice.customerCountry, alignEnd = true)
                                    PreviewDetail(invoice.customerMobile, alignEnd = true)
                                    PreviewDetail(invoice.customerEmail, alignEnd = true)
                                    PreviewDetail(invoice.customerGstin, "GSTIN: ", true)
                                }
                            }
                        }

                        // Dates row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DateBadge(label = "Issue Date", value = invoice.date, modifier = Modifier.weight(1f))
                            DateBadge(label = "Due Date", value = invoice.dueDate, modifier = Modifier.weight(1f))
                        }
                        if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.deliveryState.isNotBlank()) {
                            PreviewDetail(invoice.deliveryState, "Delivery State: ")
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.8.dp
                        )

                        // Items table
                        Column {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        templateStyle.tableHeaderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Description",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(2f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Qty",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.6f),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Price",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Total",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            invoice.items.forEachIndexed { index, item ->
                                val bg = if (index % 2 == 0) Color.Transparent
                                         else MaterialTheme.colorScheme.surfaceContainerLowest
                                InvoicePreviewRow(
                                    description = if (invoice.showItemDescription && item.description.isNotBlank()) "${item.name}\n${item.description}" else item.name,
                                    qty = item.quantity,
                                    unitPriceMinor = item.unitPriceMinor,
                                    totalMinor = item.totalMinor,
                                    currencySymbol = invoice.currencySymbol,
                                    decimalPlaces = invoice.decimalPlaces,
                                    internationalNumbering = invoice.internationalNumbering,
                                    bgColor = bg
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.8.dp
                        )

                        // Totals
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val money: (Long) -> String = { formatMoney(it, invoice.currencySymbol, invoice.decimalPlaces, invoice.internationalNumbering) }
                            TotalRow("Subtotal", money(invoice.subtotalMinor))
                            if (invoice.discountMinor != 0L) TotalRow("Discount", "- ${money(invoice.discountMinor)}")
                            if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.taxAmountMinor != 0L) {
                                when (invoice.taxOption) {
                                    TaxOption.CGST_SGST -> {
                                        val cgst = invoice.taxAmountMinor / 2
                                        val sgst = invoice.taxAmountMinor - cgst
                                        TotalRow("CGST (${invoice.taxRatePercent / 2}%)", money(cgst))
                                        TotalRow("SGST (${invoice.taxRatePercent / 2}%)", money(sgst))
                                    }
                                    TaxOption.IGST -> TotalRow("IGST (${invoice.taxRatePercent}%)", money(invoice.taxAmountMinor))
                                    TaxOption.NON_TAXABLE -> Unit
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Grand Total",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = money(invoice.totalMinor),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = templateStyle.accentColor
                                )
                            }
                            if (invoice.roundOffMinor != 0L) TotalRow("Round Off", money(invoice.roundOffMinor))
                        }

                        if (invoice.invoiceType == InvoiceType.ADVANCED && invoice.paymentDetails?.paymentMethod != PaymentMethod.NONE) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("PAYMENT DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                PreviewDetail(invoice.paymentDetails?.paymentMethod?.name.orEmpty(), "Method: ")
                                PreviewDetail(invoice.paymentDetails?.accountOwnerName.orEmpty(), "Account Owner: ")
                                PreviewDetail(invoice.paymentDetails?.accountNumber.orEmpty(), "Account: ")
                                PreviewDetail(invoice.paymentDetails?.bankName.orEmpty(), "Bank: ")
                                PreviewDetail(invoice.paymentDetails?.upiId.orEmpty(), "UPI: ")
                            }
                        }
                        if (invoice.additionalNotes.isNotBlank()) PreviewTextBlock("NOTES", invoice.additionalNotes)
                        if (invoice.termsAndConditions.isNotBlank()) PreviewTextBlock("TERMS AND CONDITIONS", invoice.termsAndConditions)

                        // Thank you
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    templateStyle.accentColor.copy(alpha = 0.12f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Thank you for your business! 🙏",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = templateStyle.accentColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class InvoicePreviewTemplateStyle(
    val headerColor: Color,
    val onHeaderColor: Color,
    val accentColor: Color,
    val documentColor: Color,
    val tableHeaderColor: Color,
    val cardCornerRadius: Dp
)

@Composable
private fun invoicePreviewTemplateStyle(templateId: String): InvoicePreviewTemplateStyle {
    val scheme = MaterialTheme.colorScheme
    return when (templateId) {
        "classic_business" -> InvoicePreviewTemplateStyle(
            headerColor = Color(0xFF1F2937),
            onHeaderColor = Color.White,
            accentColor = Color(0xFF374151),
            documentColor = scheme.surface,
            tableHeaderColor = Color(0xFFE5E7EB),
            cardCornerRadius = 8.dp
        )
        "elegant_blue" -> InvoicePreviewTemplateStyle(
            headerColor = Color(0xFF1D4ED8),
            onHeaderColor = Color.White,
            accentColor = Color(0xFF2563EB),
            documentColor = scheme.surface,
            tableHeaderColor = Color(0xFFDBEAFE),
            cardCornerRadius = 18.dp
        )
        "minimal_black" -> InvoicePreviewTemplateStyle(
            headerColor = Color(0xFF111827),
            onHeaderColor = Color.White,
            accentColor = Color(0xFF111827),
            documentColor = scheme.surface,
            tableHeaderColor = Color(0xFFF3F4F6),
            cardCornerRadius = 2.dp
        )
        "soft_green" -> InvoicePreviewTemplateStyle(
            headerColor = Color(0xFF047857),
            onHeaderColor = Color.White,
            accentColor = Color(0xFF059669),
            documentColor = scheme.surface,
            tableHeaderColor = Color(0xFFD1FAE5),
            cardCornerRadius = 20.dp
        )
        "premium_gold" -> InvoicePreviewTemplateStyle(
            headerColor = Color(0xFF78350F),
            onHeaderColor = Color(0xFFFFFBEB),
            accentColor = Color(0xFFB45309),
            documentColor = scheme.surface,
            tableHeaderColor = Color(0xFFFEF3C7),
            cardCornerRadius = 14.dp
        )
        "corporate_slate" -> InvoicePreviewTemplateStyle(
            headerColor = Color(0xFF334155),
            onHeaderColor = Color.White,
            accentColor = Color(0xFF475569),
            documentColor = scheme.surface,
            tableHeaderColor = Color(0xFFE2E8F0),
            cardCornerRadius = 10.dp
        )
        "creative_coral" -> InvoicePreviewTemplateStyle(
            headerColor = Color(0xFFE11D48),
            onHeaderColor = Color.White,
            accentColor = Color(0xFFF43F5E),
            documentColor = scheme.surface,
            tableHeaderColor = Color(0xFFFFE4E6),
            cardCornerRadius = 22.dp
        )
        "royal_purple" -> InvoicePreviewTemplateStyle(
            headerColor = Color(0xFF6D28D9),
            onHeaderColor = Color.White,
            accentColor = Color(0xFF7C3AED),
            documentColor = scheme.surface,
            tableHeaderColor = Color(0xFFEDE9FE),
            cardCornerRadius = 18.dp
        )
        "clean_ledger" -> InvoicePreviewTemplateStyle(
            headerColor = Color(0xFF0F766E),
            onHeaderColor = Color.White,
            accentColor = Color(0xFF0D9488),
            documentColor = scheme.surface,
            tableHeaderColor = Color(0xFFCCFBF1),
            cardCornerRadius = 6.dp
        )
        else -> InvoicePreviewTemplateStyle(
            headerColor = scheme.primary,
            onHeaderColor = scheme.onPrimary,
            accentColor = scheme.primary,
            documentColor = scheme.surface,
            tableHeaderColor = scheme.surfaceContainerLow,
            cardCornerRadius = 20.dp
        )
    }
}

@Composable
private fun OrganizationLogo(uri: String) {
    val context = LocalContext.current
    val logoResolver = remember { LogoResolver() }
    val bitmap = remember(uri) {
        logoResolver.decode(context, uri)
    }

    if (bitmap != null) {
        Box(
            modifier = Modifier
                .widthIn(max = 96.dp)
                .height(64.dp)
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Organization logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun DateBadge(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InvoicePreviewRow(
    description: String,
    qty: Int,
    unitPriceMinor: Long,
    totalMinor: Long,
    currencySymbol: String,
    decimalPlaces: Int,
    internationalNumbering: Boolean,
    bgColor: Color = Color.Transparent
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = description,
            fontSize = 13.sp,
            modifier = Modifier.weight(2f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = qty.toString(),
            fontSize = 13.sp,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatMoney(unitPriceMinor, currencySymbol, decimalPlaces, internationalNumbering),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatMoney(totalMinor, currencySymbol, decimalPlaces, internationalNumbering),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PreviewDetail(value: String, prefix: String = "", alignEnd: Boolean = false) {
    if (value.isBlank()) return
    Text(
        text = prefix + value,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
    )
}

@Composable
private fun PreviewTextBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp)
    }
}

@Composable
private fun TotalRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PreviewActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
