package com.example.freeinvoicegeneratorbydaybookcloud.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.freeinvoicegeneratorbydaybookcloud.pdf.InvoicePdfGenerator
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.BusinessSettingsRepository
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.InvoiceSettings
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.InvoiceSettingsRepository
import com.example.freeinvoicegeneratorbydaybookcloud.domain.calculation.InvoiceCalculation
import com.example.freeinvoicegeneratorbydaybookcloud.domain.calculation.InvoiceCalculationEngine
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.*
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.CustomerRepository
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.InvoiceRepository
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.OrganizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val inputDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

private fun formattedInputDate(date: LocalDate): String = date.format(inputDateFormatter)

private fun parseDate(value: String): Long = try {
    LocalDate.parse(value.trim(), inputDateFormatter)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
} catch (_: DateTimeParseException) {
    LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun formatInputDate(value: Long): String = formattedInputDate(
    Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
)

private fun formatStoredDate(value: Long, option: DateFormatOption): String =
    Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofPattern(option.pattern, Locale.US))

data class InvoiceItemUiModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val quantity: Int,
    val unitPriceMinor: Long,
    val discountPercent: Double = 0.0,
    val cgstPercent: Double = 0.0,
    val sgstPercent: Double = 0.0,
    val igstPercent: Double = 0.0,
    val lineSubtotalMinor: Long = quantity * unitPriceMinor,
    val discountMinor: Long = 0L,
    val taxableAmountMinor: Long = lineSubtotalMinor,
    val taxAmountMinor: Long = 0L,
    val totalMinor: Long = lineSubtotalMinor
) {
    val unitPriceCents: Long get() = unitPriceMinor
    val totalCents: Long get() = totalMinor
}

data class CreateInvoiceUiState(
    val currentStep: Int = 1,
    val invoiceType: InvoiceType = InvoiceType.SIMPLE,
    val organizationName: String = "Your Company",
    val organizationAddress: String = "123 Business Street, City, State",
    val organizationCountry: String = "",
    val organizationEmail: String = "",
    val organizationMobile: String = "",
    val organizationGstin: String = "",
    val authorityName: String = "",
    val authorityDesignation: String = "",
    val organizationLogoPath: String? = null,
    val customerName: String = "",
    val customerAddress: String = "",
    val customerCountry: String = "",
    val customerMobile: String = "",
    val customerEmail: String = "",
    val customerGstin: String = "",
    val invoiceNumber: String = "INV-001",
    val invoiceDate: String = formattedInputDate(LocalDate.now()),
    val dueDate: String = formattedInputDate(LocalDate.now().plusDays(15)),
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val decimalPlaces: Int = 2,
    val deliveryState: String = "",
    val taxOption: TaxOption = TaxOption.NON_TAXABLE,
    val taxRatePercent: Int = 10,
    val dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY,
    val showItemDescription: Boolean = false,
    val showItemDiscount: Boolean = false,
    val internationalNumbering: Boolean = false,
    val roundOffMinor: Long = 0L,
    val paymentMethod: PaymentMethod = PaymentMethod.NONE,
    val accountNumber: String = "",
    val accountOwnerName: String = "",
    val bankName: String = "",
    val upiId: String = "",
    val items: List<InvoiceItemUiModel> = emptyList(),
    val additionalNotes: String = "",
    val termsAndConditions: String = ""
) {
    val calculation: InvoiceCalculation
        get() = InvoiceCalculationEngine.calculate(
            invoiceType = invoiceType,
            taxOption = taxOption,
            items = items.map { it.toDomainInput() },
            roundOffMinor = roundOffMinor
        )
    val subtotalMinor: Long get() = calculation.subtotalMinor
    val discountMinor: Long get() = calculation.discountMinor
    val taxAmountMinor: Long get() = calculation.taxAmountMinor
    val totalMinor: Long get() = calculation.totalMinor
    val subtotalCents: Long get() = subtotalMinor
    val taxCents: Long get() = taxAmountMinor
    val totalCents: Long get() = totalMinor
}

private fun InvoiceItemUiModel.toDomainInput(): InvoiceItem = InvoiceItem(
    id = id.toLongOrNull() ?: 0L,
    name = name,
    description = description.ifBlank { null },
    quantity = quantity.toDouble(),
    unitPriceMinor = unitPriceMinor,
    discountPercent = discountPercent,
    cgstPercent = cgstPercent,
    sgstPercent = sgstPercent,
    igstPercent = igstPercent,
    lineSubtotalMinor = 0L,
    taxAmountMinor = 0L,
    totalMinor = 0L
)

data class InvoiceUiModel(
    val id: Long,
    val invoiceNumber: String,
    val invoiceType: InvoiceType,
    val organizationName: String,
    val organizationAddress: String,
    val organizationCountry: String,
    val organizationEmail: String,
    val organizationMobile: String,
    val organizationGstin: String,
    val authorityName: String,
    val authorityDesignation: String,
    val organizationLogoPath: String?,
    val customerName: String,
    val customerAddress: String,
    val customerCountry: String,
    val customerMobile: String,
    val customerEmail: String,
    val customerGstin: String,
    val date: String,
    val dueDate: String,
    val amountMinor: Long,
    val status: String,
    val currencyCode: String,
    val currencySymbol: String,
    val decimalPlaces: Int,
    val deliveryState: String,
    val taxOption: TaxOption,
    val dateFormat: DateFormatOption,
    val showItemDescription: Boolean,
    val showItemDiscount: Boolean,
    val internationalNumbering: Boolean,
    val roundOffMinor: Long,
    val additionalNotes: String,
    val termsAndConditions: String,
    val paymentDetails: PaymentDetails?,
    val items: List<InvoiceItemUiModel>,
    val subtotalMinor: Long,
    val discountMinor: Long,
    val taxAmountMinor: Long,
    val totalMinor: Long
) {
    val amountCents: Long get() = amountMinor
    val subtotalCents: Long get() = subtotalMinor
    val taxCents: Long get() = taxAmountMinor
    val totalCents: Long get() = totalMinor
    val taxRatePercent: Int get() = when (taxOption) {
        TaxOption.CGST_SGST -> items.firstOrNull()?.let { (it.cgstPercent + it.sgstPercent).roundToInt() } ?: 0
        TaxOption.IGST -> items.firstOrNull()?.igstPercent?.roundToInt() ?: 0
        TaxOption.NON_TAXABLE -> 0
    }
}

data class PdfActionState(
    val isGeneratingPdf: Boolean = false
)

sealed interface InvoicePreviewEvent {
    data class DownloadSuccess(val uri: Uri) : InvoicePreviewEvent
    data object DownloadError : InvoicePreviewEvent
    data class ShareReady(val uri: Uri, val fileName: String) : InvoicePreviewEvent
    data object ShareError : InvoicePreviewEvent
}

private fun Invoice.toUiModel(): InvoiceUiModel = InvoiceUiModel(
    id = id,
    invoiceNumber = invoiceNumber,
    invoiceType = invoiceType,
    organizationName = organization?.name.orEmpty(),
    organizationAddress = organization?.address.orEmpty(),
    organizationCountry = organization?.country.orEmpty(),
    organizationEmail = organization?.email.orEmpty(),
    organizationMobile = organization?.mobile ?: organization?.phone.orEmpty(),
    organizationGstin = organization?.gstin ?: organization?.taxNumber.orEmpty(),
    authorityName = organization?.authorityName.orEmpty(),
    authorityDesignation = organization?.authorityDesignation.orEmpty(),
    organizationLogoPath = organization?.logoPath,
    customerName = customer?.name.orEmpty(),
    customerAddress = customer?.address.orEmpty(),
    customerCountry = customer?.country.orEmpty(),
    customerMobile = customer?.mobile ?: customer?.phone.orEmpty(),
    customerEmail = customer?.email.orEmpty(),
    customerGstin = customer?.gstin ?: customer?.taxNumber.orEmpty(),
    date = formatStoredDate(invoiceDate, dateFormat),
    dueDate = formatStoredDate(dueDate, dateFormat),
    amountMinor = totalMinor,
    status = status.name.lowercase().replaceFirstChar { it.uppercase() },
    currencyCode = currencyCode,
    currencySymbol = currencySymbol,
    decimalPlaces = decimalPlaces,
    deliveryState = deliveryState.orEmpty(),
    taxOption = taxOption,
    dateFormat = dateFormat,
    showItemDescription = showItemDescription,
    showItemDiscount = showItemDiscount,
    internationalNumbering = internationalNumbering,
    roundOffMinor = roundOffMinor,
    additionalNotes = additionalNotes ?: notes.orEmpty(),
    termsAndConditions = termsAndConditions.orEmpty(),
    paymentDetails = paymentDetails,
    items = items.map { item ->
        InvoiceItemUiModel(
            id = item.id.toString(),
            name = item.name,
            description = item.description.orEmpty(),
            quantity = item.quantity.roundToInt(),
            unitPriceMinor = item.unitPriceMinor,
            discountPercent = item.discountPercent,
            cgstPercent = item.cgstPercent,
            sgstPercent = item.sgstPercent,
            igstPercent = item.igstPercent,
            lineSubtotalMinor = item.lineSubtotalMinor,
            discountMinor = item.discountMinor,
            taxableAmountMinor = item.taxableAmountMinor,
            taxAmountMinor = item.taxAmountMinor,
            totalMinor = item.totalMinor
        )
    },
    subtotalMinor = subtotalMinor,
    discountMinor = discountMinor,
    taxAmountMinor = taxAmountMinor,
    totalMinor = totalMinor
)

@HiltViewModel
class CreateInvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val organizationRepository: OrganizationRepository,
    private val invoiceSettingsRepository: InvoiceSettingsRepository,
    private val businessSettingsRepository: BusinessSettingsRepository,
    private val invoicePdfGenerator: InvoicePdfGenerator
) : ViewModel() {
    private var currentInvoiceSettings = invoiceSettingsRepository.settings.value
    private var currentBusinessSettings = businessSettingsRepository.settings.value
    private var editingInvoice: Invoice? = null

    private val _uiState = MutableStateFlow(newState(InvoiceType.SIMPLE))
    val uiState: StateFlow<CreateInvoiceUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _editingInvoiceId = MutableStateFlow<Long?>(null)
    val editingInvoiceId: StateFlow<Long?> = _editingInvoiceId.asStateFlow()

    private val _pdfActionState = MutableStateFlow(PdfActionState())
    val pdfActionState: StateFlow<PdfActionState> = _pdfActionState.asStateFlow()

    private val _previewEvents = MutableSharedFlow<InvoicePreviewEvent>()
    val previewEvents: SharedFlow<InvoicePreviewEvent> = _previewEvents.asSharedFlow()

    val invoices: StateFlow<List<InvoiceUiModel>> = invoiceRepository.observeInvoices()
        .map { invoices -> invoices.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedTemplateId: StateFlow<String> = invoiceSettingsRepository.settings
        .map { it.templateId }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            invoiceSettingsRepository.settings.value.templateId
        )

    private var lastSuggestedInvoiceNumber = "${currentInvoiceSettings.prefix}001"

    init {
        viewModelScope.launch {
            invoices.collect { saved ->
                val next = nextInvoiceNumber(saved)
                _uiState.update { state ->
                    if (state.invoiceNumber == lastSuggestedInvoiceNumber) state.copy(invoiceNumber = next) else state
                }
                lastSuggestedInvoiceNumber = next
            }
        }
        viewModelScope.launch {
            businessSettingsRepository.settings.collect { settings ->
                currentBusinessSettings = settings
                if (_editingInvoiceId.value == null) {
                    _uiState.update { it.copy(
                        organizationName = settings.name,
                        organizationAddress = settings.address,
                        organizationEmail = settings.email,
                        organizationMobile = settings.phone
                    ) }
                }
            }
        }
        viewModelScope.launch {
            invoiceSettingsRepository.settings.collect { settings ->
                val previous = currentInvoiceSettings
                currentInvoiceSettings = settings
                if (_editingInvoiceId.value == null) {
                    _uiState.update { state ->
                        state.copy(
                            invoiceNumber = settings.prefix + state.invoiceNumber.removePrefix(previous.prefix),
                            currencyCode = settings.currencyCode,
                            currencySymbol = settings.currency.symbol,
                            taxRatePercent = settings.taxRatePercent
                        )
                    }
                }
                lastSuggestedInvoiceNumber = nextInvoiceNumber(invoices.value)
            }
        }
    }

    fun selectInvoiceType(type: InvoiceType) {
        editingInvoice = null
        _editingInvoiceId.value = null
        _uiState.value = newState(type).copy(invoiceNumber = nextInvoiceNumber(invoices.value))
    }

    fun setStep(step: Int) { _uiState.update { it.copy(currentStep = step) } }
    fun updateOrganization(name: String, address: String) {
        _uiState.update { it.copy(organizationName = name, organizationAddress = address) }
    }
    fun updateOrganizationLogo(logoPath: String?) {
        _uiState.update { it.copy(organizationLogoPath = logoPath) }
    }
    fun updateCustomer(name: String, address: String) {
        _uiState.update { it.copy(customerName = name, customerAddress = address) }
    }
    fun updateInvoiceDetails(number: String, date: String, due: String) {
        _uiState.update { it.copy(invoiceNumber = number, invoiceDate = date, dueDate = due) }
    }
    fun updateAdvancedOrganization(
        name: String, address: String, country: String, email: String, mobile: String,
        gstin: String, authorityName: String, designation: String, logoPath: String?
    ) {
        _uiState.update { it.copy(
            organizationName = name, organizationAddress = address, organizationCountry = country,
            organizationEmail = email, organizationMobile = mobile, organizationGstin = gstin,
            authorityName = authorityName, authorityDesignation = designation, organizationLogoPath = logoPath
        ) }
    }
    fun updateAdvancedCustomer(name: String, address: String, country: String, mobile: String, email: String, gstin: String) {
        _uiState.update { it.copy(
            customerName = name, customerAddress = address, customerCountry = country,
            customerMobile = mobile, customerEmail = email, customerGstin = gstin
        ) }
    }
    fun updateConfiguration(
        number: String, date: String, due: String, currencyCode: String, currencySymbol: String,
        decimalPlaces: Int, deliveryState: String, taxOption: TaxOption, dateFormat: DateFormatOption,
        showDescription: Boolean, showDiscount: Boolean
    ) {
        _uiState.update { it.copy(
            invoiceNumber = number, invoiceDate = date, dueDate = due,
            currencyCode = currencyCode, currencySymbol = currencySymbol,
            decimalPlaces = decimalPlaces.coerceIn(0, 3), deliveryState = deliveryState,
            taxOption = taxOption, dateFormat = dateFormat,
            showItemDescription = showDescription, showItemDiscount = showDiscount
        ) }
    }
    fun updateItemOptions(internationalNumbering: Boolean, roundOffMinor: Long) {
        _uiState.update { it.copy(internationalNumbering = internationalNumbering, roundOffMinor = roundOffMinor) }
    }
    fun updatePaymentAndAdditional(
        method: PaymentMethod, accountNumber: String, owner: String, bank: String, upi: String,
        notes: String, terms: String
    ) {
        _uiState.update { it.copy(
            paymentMethod = method, accountNumber = accountNumber, accountOwnerName = owner,
            bankName = bank, upiId = upi, additionalNotes = notes, termsAndConditions = terms
        ) }
    }
    fun updateAdditional(notes: String, terms: String) {
        _uiState.update { it.copy(additionalNotes = notes, termsAndConditions = terms) }
    }

    fun addItem(
        name: String,
        quantity: Int,
        unitPriceMinor: Long,
        description: String = "",
        discountPercent: Double = 0.0,
        taxPercent: Double = _uiState.value.taxRatePercent.toDouble()
    ) {
        val state = _uiState.value
        val item = InvoiceItemUiModel(
            name = name,
            description = description,
            quantity = quantity,
            unitPriceMinor = unitPriceMinor,
            discountPercent = if (state.showItemDiscount) discountPercent else 0.0,
            cgstPercent = if (state.taxOption == TaxOption.CGST_SGST) taxPercent / 2.0 else 0.0,
            sgstPercent = if (state.taxOption == TaxOption.CGST_SGST) taxPercent / 2.0 else 0.0,
            igstPercent = if (state.taxOption == TaxOption.IGST) taxPercent else 0.0
        )
        _uiState.update { it.copy(items = it.items + item) }
    }

    fun updateItem(
        itemId: String,
        name: String,
        quantity: Int,
        unitPriceMinor: Long,
        description: String = "",
        discountPercent: Double = 0.0,
        taxPercent: Double = _uiState.value.taxRatePercent.toDouble()
    ) {
        val state = _uiState.value
        _uiState.update { current -> current.copy(items = current.items.map { item ->
            if (item.id != itemId) item else item.copy(
                name = name, description = description, quantity = quantity, unitPriceMinor = unitPriceMinor,
                discountPercent = if (state.showItemDiscount) discountPercent else 0.0,
                cgstPercent = if (state.taxOption == TaxOption.CGST_SGST) taxPercent / 2.0 else 0.0,
                sgstPercent = if (state.taxOption == TaxOption.CGST_SGST) taxPercent / 2.0 else 0.0,
                igstPercent = if (state.taxOption == TaxOption.IGST) taxPercent else 0.0
            )
        }) }
    }
    fun removeItem(itemId: String) { _uiState.update { it.copy(items = it.items.filterNot { item -> item.id == itemId }) } }

    fun startNewInvoice() {
        editingInvoice = null
        _editingInvoiceId.value = null
        _saveError.value = null
        _uiState.value = newState(InvoiceType.SIMPLE).copy(invoiceNumber = nextInvoiceNumber(invoices.value))
    }

    fun beginEditing(invoiceId: Long, onReady: () -> Unit) {
        viewModelScope.launch {
            val invoice = invoiceRepository.getInvoiceById(invoiceId) ?: run {
                _saveError.value = "Invoice not found."
                return@launch
            }
            editingInvoice = invoice
            _editingInvoiceId.value = invoice.id
            _saveError.value = null
            _uiState.value = CreateInvoiceUiState(
                invoiceType = invoice.invoiceType,
                organizationName = invoice.organization?.name.orEmpty(),
                organizationAddress = invoice.organization?.address.orEmpty(),
                organizationCountry = invoice.organization?.country.orEmpty(),
                organizationEmail = invoice.organization?.email.orEmpty(),
                organizationMobile = invoice.organization?.mobile ?: invoice.organization?.phone.orEmpty(),
                organizationGstin = invoice.organization?.gstin ?: invoice.organization?.taxNumber.orEmpty(),
                authorityName = invoice.organization?.authorityName.orEmpty(),
                authorityDesignation = invoice.organization?.authorityDesignation.orEmpty(),
                organizationLogoPath = invoice.organization?.logoPath,
                customerName = invoice.customer?.name.orEmpty(),
                customerAddress = invoice.customer?.address.orEmpty(),
                customerCountry = invoice.customer?.country.orEmpty(),
                customerMobile = invoice.customer?.mobile ?: invoice.customer?.phone.orEmpty(),
                customerEmail = invoice.customer?.email.orEmpty(),
                customerGstin = invoice.customer?.gstin ?: invoice.customer?.taxNumber.orEmpty(),
                invoiceNumber = invoice.invoiceNumber,
                invoiceDate = formatInputDate(invoice.invoiceDate),
                dueDate = formatInputDate(invoice.dueDate),
                currencyCode = invoice.currencyCode,
                currencySymbol = invoice.currencySymbol,
                decimalPlaces = invoice.decimalPlaces,
                deliveryState = invoice.deliveryState.orEmpty(),
                taxOption = invoice.taxOption,
                dateFormat = invoice.dateFormat,
                showItemDescription = invoice.showItemDescription,
                showItemDiscount = invoice.showItemDiscount,
                internationalNumbering = invoice.internationalNumbering,
                roundOffMinor = invoice.roundOffMinor,
                paymentMethod = invoice.paymentDetails?.paymentMethod ?: PaymentMethod.NONE,
                accountNumber = invoice.paymentDetails?.accountNumber.orEmpty(),
                accountOwnerName = invoice.paymentDetails?.accountOwnerName.orEmpty(),
                bankName = invoice.paymentDetails?.bankName.orEmpty(),
                upiId = invoice.paymentDetails?.upiId.orEmpty(),
                additionalNotes = invoice.additionalNotes ?: invoice.notes.orEmpty(),
                termsAndConditions = invoice.termsAndConditions.orEmpty(),
                items = invoice.items.map { it.toUiItem() },
                taxRatePercent = invoice.items.firstOrNull()?.taxPercent?.roundToInt() ?: currentInvoiceSettings.taxRatePercent
            )
            onReady()
        }
    }

    fun deleteInvoice(invoiceId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val invoice = invoiceRepository.getInvoiceById(invoiceId) ?: return@launch
                invoiceRepository.deleteInvoice(invoice)
                if (_editingInvoiceId.value == invoiceId) startNewInvoice()
                onSuccess()
            } catch (error: Exception) {
                _saveError.value = error.message ?: "Could not delete the invoice."
            }
        }
    }

    fun downloadInvoicePdf(invoiceId: Long) {
        if (_pdfActionState.value.isGeneratingPdf) return
        val invoice = invoices.value.firstOrNull { it.id == invoiceId } ?: return
        _pdfActionState.value = PdfActionState(isGeneratingPdf = true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { invoicePdfGenerator.saveToDownloads(invoice) }
            result
                .onSuccess { _previewEvents.emit(InvoicePreviewEvent.DownloadSuccess(it)) }
                .onFailure { _previewEvents.emit(InvoicePreviewEvent.DownloadError) }
            _pdfActionState.value = PdfActionState()
        }
    }

    fun shareInvoicePdf(invoiceId: Long) {
        if (_pdfActionState.value.isGeneratingPdf) return
        val invoice = invoices.value.firstOrNull { it.id == invoiceId } ?: return
        _pdfActionState.value = PdfActionState(isGeneratingPdf = true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                invoicePdfGenerator.createSharePdf(invoice).map { file ->
                    invoicePdfGenerator.contentUriFor(file) to file.name
                }
            }
            result
                .onSuccess { (uri, fileName) -> _previewEvents.emit(InvoicePreviewEvent.ShareReady(uri, fileName)) }
                .onFailure { _previewEvents.emit(InvoicePreviewEvent.ShareError) }
            _pdfActionState.value = PdfActionState()
        }
    }

    fun createInvoice(onSuccess: (Long) -> Unit) {
        if (_isSaving.value) return
        val state = _uiState.value
        _isSaving.value = true
        _saveError.value = null
        viewModelScope.launch {
            try {
                val existing = editingInvoice
                val organization = Organization(
                    id = existing?.organizationId ?: 0L,
                    name = state.organizationName.trim(),
                    address = state.organizationAddress.trim(),
                    country = state.organizationCountry.trim().ifBlank { null },
                    email = state.organizationEmail.trim().ifBlank { null },
                    phone = state.organizationMobile.trim().ifBlank { null },
                    mobile = state.organizationMobile.trim().ifBlank { null },
                    taxNumber = state.organizationGstin.trim().ifBlank { null },
                    gstin = state.organizationGstin.trim().ifBlank { null },
                    authorityName = state.authorityName.trim().ifBlank { null },
                    authorityDesignation = state.authorityDesignation.trim().ifBlank { null },
                    logoPath = state.organizationLogoPath,
                    createdAt = existing?.organization?.createdAt ?: System.currentTimeMillis()
                )
                val organizationId = if (existing == null) organizationRepository.saveOrganization(organization)
                else { organizationRepository.updateOrganization(organization); organization.id }

                val customer = Customer(
                    id = existing?.customerId ?: 0L,
                    name = state.customerName.trim(),
                    address = state.customerAddress.trim(),
                    country = state.customerCountry.trim().ifBlank { null },
                    email = state.customerEmail.trim().ifBlank { null },
                    phone = state.customerMobile.trim().ifBlank { null },
                    mobile = state.customerMobile.trim().ifBlank { null },
                    taxNumber = state.customerGstin.trim().ifBlank { null },
                    gstin = state.customerGstin.trim().ifBlank { null },
                    createdAt = existing?.customer?.createdAt ?: System.currentTimeMillis()
                )
                val customerId = if (existing == null) customerRepository.saveCustomer(customer)
                else { customerRepository.updateCustomer(customer); customer.id }

                val calculation = state.calculation
                val invoice = (existing ?: Invoice(
                    invoiceNumber = state.invoiceNumber,
                    organizationId = organizationId,
                    customerId = customerId,
                    invoiceDate = parseDate(state.invoiceDate),
                    dueDate = parseDate(state.dueDate),
                    subtotalMinor = calculation.subtotalMinor,
                    taxAmountMinor = calculation.taxAmountMinor,
                    totalMinor = calculation.totalMinor
                )).copy(
                    invoiceNumber = state.invoiceNumber.trim(),
                    organizationId = organizationId,
                    customerId = customerId,
                    invoiceDate = parseDate(state.invoiceDate),
                    dueDate = parseDate(state.dueDate),
                    subtotalMinor = calculation.subtotalMinor,
                    discountMinor = calculation.discountMinor,
                    taxAmountMinor = calculation.taxAmountMinor,
                    totalMinor = calculation.totalMinor,
                    status = existing?.status ?: InvoiceStatus.DRAFT,
                    notes = state.additionalNotes.ifBlank { null },
                    invoiceType = state.invoiceType,
                    currencyCode = state.currencyCode,
                    currencySymbol = state.currencySymbol,
                    decimalPlaces = state.decimalPlaces,
                    deliveryState = state.deliveryState.ifBlank { null },
                    taxOption = if (state.invoiceType == InvoiceType.SIMPLE) TaxOption.NON_TAXABLE else state.taxOption,
                    dateFormat = state.dateFormat,
                    showItemDescription = state.invoiceType == InvoiceType.ADVANCED && state.showItemDescription,
                    showItemDiscount = state.invoiceType == InvoiceType.ADVANCED && state.showItemDiscount,
                    internationalNumbering = state.invoiceType == InvoiceType.ADVANCED && state.internationalNumbering,
                    roundOffMinor = calculation.roundOffMinor,
                    additionalNotes = state.additionalNotes.ifBlank { null },
                    termsAndConditions = state.termsAndConditions.ifBlank { null },
                    updatedAt = System.currentTimeMillis()
                )
                val payment = if (state.invoiceType == InvoiceType.ADVANCED) PaymentDetails(
                    invoiceId = existing?.id ?: 0L,
                    paymentMethod = state.paymentMethod,
                    accountNumber = state.accountNumber.ifBlank { null },
                    accountOwnerName = state.accountOwnerName.ifBlank { null },
                    bankName = state.bankName.ifBlank { null },
                    upiId = state.upiId.ifBlank { null }
                ) else null
                val invoiceId = if (existing == null) {
                    invoiceRepository.saveInvoice(invoice, calculation.items, payment)
                } else {
                    invoiceRepository.updateInvoice(invoice, calculation.items, payment)
                    existing.id
                }
                editingInvoice = null
                _editingInvoiceId.value = null
                _uiState.value = newState(InvoiceType.SIMPLE).copy(invoiceNumber = nextInvoiceNumber(invoices.value, invoiceId))
                onSuccess(invoiceId)
            } catch (error: Exception) {
                _saveError.value = error.message ?: "Could not save the invoice. Please try again."
            } finally {
                _isSaving.value = false
            }
        }
    }

    private fun newState(type: InvoiceType): CreateInvoiceUiState = CreateInvoiceUiState(
        invoiceType = type,
        organizationName = currentBusinessSettings.name,
        organizationAddress = currentBusinessSettings.address,
        organizationEmail = currentBusinessSettings.email,
        organizationMobile = currentBusinessSettings.phone,
        invoiceNumber = "${currentInvoiceSettings.prefix}001",
        currencyCode = currentInvoiceSettings.currencyCode,
        currencySymbol = currentInvoiceSettings.currency.symbol,
        taxRatePercent = currentInvoiceSettings.taxRatePercent,
        taxOption = if (type == InvoiceType.ADVANCED) TaxOption.CGST_SGST else TaxOption.NON_TAXABLE
    )

    private fun nextInvoiceNumber(invoices: List<InvoiceUiModel>, savedId: Long = 0L): String {
        val nextId = maxOf(invoices.maxOfOrNull { it.id } ?: 0L, savedId) + 1L
        return "${currentInvoiceSettings.prefix}${nextId.toString().padStart(3, '0')}"
    }
}

private fun InvoiceItem.toUiItem(): InvoiceItemUiModel = InvoiceItemUiModel(
    id = id.toString(), name = name, description = description.orEmpty(), quantity = quantity.roundToInt(),
    unitPriceMinor = unitPriceMinor, discountPercent = discountPercent, cgstPercent = cgstPercent,
    sgstPercent = sgstPercent, igstPercent = igstPercent, lineSubtotalMinor = lineSubtotalMinor,
    discountMinor = discountMinor, taxableAmountMinor = taxableAmountMinor,
    taxAmountMinor = taxAmountMinor, totalMinor = totalMinor
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    invoiceRepository: InvoiceRepository,
    organizationRepository: OrganizationRepository,
    businessSettingsRepository: BusinessSettingsRepository
) : ViewModel() {
    val organizationName = combine(
        organizationRepository.observeOrganization(),
        businessSettingsRepository.settings
    ) { organization, businessSettings ->
        organization?.name?.trim()?.takeIf { it.isNotBlank() }
            ?: businessSettings.name.trim()
                .takeIf { it.isNotBlank() && it != BusinessSettingsRepository.DEFAULT_NAME }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val recentInvoices = invoiceRepository.observeRecentInvoices(3)
        .map { list -> list.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val totalInvoicesCount = invoiceRepository.observeInvoices().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

@HiltViewModel
class InvoicesViewModel @Inject constructor(invoiceRepository: InvoiceRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val invoices = invoiceRepository.observeInvoices().map { list -> list.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun setSearchQuery(query: String) { _searchQuery.value = query }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: android.app.Application,
    private val invoiceSettingsRepository: InvoiceSettingsRepository,
    private val businessSettingsRepository: BusinessSettingsRepository
) : androidx.lifecycle.AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("daybook_settings", android.content.Context.MODE_PRIVATE)
    val organizationName = businessSettingsRepository.settings.map { it.name }
        .stateIn(viewModelScope, SharingStarted.Eagerly, businessSettingsRepository.settings.value.name)
    val organizationAddress = businessSettingsRepository.settings.map { it.address }
        .stateIn(viewModelScope, SharingStarted.Eagerly, businessSettingsRepository.settings.value.address)
    val userEmail = businessSettingsRepository.settings.map { it.email }
        .stateIn(viewModelScope, SharingStarted.Eagerly, businessSettingsRepository.settings.value.email)
    val phoneNumber = businessSettingsRepository.settings.map { it.phone }
        .stateIn(viewModelScope, SharingStarted.Eagerly, businessSettingsRepository.settings.value.phone)
    private val _themeMode = MutableStateFlow(runCatching {
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    }.getOrDefault(ThemeMode.SYSTEM))
    val themeMode = _themeMode.asStateFlow()
    val invoiceSettings: StateFlow<InvoiceSettings> = invoiceSettingsRepository.settings
    fun setThemeMode(mode: ThemeMode) { _themeMode.value = mode; prefs.edit().putString("theme_mode", mode.name).apply() }
    fun updateInvoiceSettings(prefix: String, taxRate: String, currencyCode: String) {
        invoiceSettingsRepository.update(prefix, taxRate.toIntOrNull() ?: invoiceSettings.value.taxRatePercent, currencyCode)
    }
    fun selectInvoiceTemplate(templateId: String) {
        invoiceSettingsRepository.updateTemplate(templateId)
    }
    fun updateOrgName(name: String) {
        val current = businessSettingsRepository.settings.value
        businessSettingsRepository.update(name, current.address, current.email, current.phone)
    }
    fun updateOrganization(name: String, address: String, email: String, phone: String) {
        businessSettingsRepository.update(name, address, email, phone)
    }
}
