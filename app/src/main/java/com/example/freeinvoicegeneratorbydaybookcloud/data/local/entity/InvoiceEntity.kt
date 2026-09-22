package com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.DateFormatOption
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.TaxOption

/**
 * Room entity for an invoice.
 * Stored in the "invoices" table.
 *
 * Foreign keys:
 *  - organizationId → organizations.id  (NO ACTION on delete — org deletion is not expected)
 *  - customerId     → customers.id      (NO ACTION on delete — customer deletion is not expected)
 *
 * Monetary fields use Long (smallest currency unit, e.g. paise or cents).
 * Never use Float/Double for final stored monetary amounts.
 */
@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = OrganizationEntity::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["organizationId"]),
        Index(value = ["customerId"]),
        // invoiceNumber is not declared UNIQUE here to allow draft re-use or corrections,
        // but it IS indexed for fast lookup and search.
        Index(value = ["invoiceNumber"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val invoiceNumber: String,
    val organizationId: Long,
    val customerId: Long,
    /** Invoice date as epoch milliseconds. */
    val invoiceDate: Long,
    /** Due date as epoch milliseconds. */
    val dueDate: Long,
    /** Subtotal before tax, in smallest currency unit (e.g. paise). */
    val subtotalMinor: Long,
    /** Total tax amount, in smallest currency unit. */
    val taxAmountMinor: Long,
    /** Total discount applied, in smallest currency unit. */
    val discountMinor: Long = 0L,
    /** Final total (subtotal + tax − discount), in smallest currency unit. */
    val totalMinor: Long,
    /**
     * Invoice lifecycle status, stored as String.
     * Converted to/from [com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceStatus]
     * via [com.example.freeinvoicegeneratorbydaybookcloud.data.local.converter.InvoiceStatusConverter].
     */
    val status: String = "DRAFT",
    val notes: String? = null,
    @ColumnInfo(defaultValue = "'SIMPLE'")
    val invoiceType: InvoiceType = InvoiceType.SIMPLE,
    @ColumnInfo(defaultValue = "'USD'")
    val currencyCode: String = "USD",
    @ColumnInfo(defaultValue = "'$'")
    val currencySymbol: String = "$",
    @ColumnInfo(defaultValue = "2")
    val decimalPlaces: Int = 2,
    val deliveryState: String? = null,
    @ColumnInfo(defaultValue = "'NON_TAXABLE'")
    val taxOption: TaxOption = TaxOption.NON_TAXABLE,
    @ColumnInfo(defaultValue = "'DD_MM_YYYY'")
    val dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY,
    @ColumnInfo(defaultValue = "0")
    val showItemDescription: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val showItemDiscount: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val internationalNumbering: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val roundOffMinor: Long = 0L,
    val additionalNotes: String? = null,
    val termsAndConditions: String? = null,
    /** Creation timestamp as epoch milliseconds. */
    val createdAt: Long = System.currentTimeMillis(),
    /** Last-updated timestamp as epoch milliseconds. */
    val updatedAt: Long = System.currentTimeMillis()
)
