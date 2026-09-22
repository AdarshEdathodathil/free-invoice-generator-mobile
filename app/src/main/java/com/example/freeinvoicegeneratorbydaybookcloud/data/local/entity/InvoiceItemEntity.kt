package com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a line item within an invoice.
 * Stored in the "invoice_items" table.
 *
 * Foreign key:
 *  - invoiceId → invoices.id  (CASCADE on delete — deleting an invoice removes all its items)
 *
 * Rules:
 *  - Quantity and taxPercent may use Double (they represent ratios/counts, not currency).
 *  - All monetary amounts MUST use Long (smallest currency unit).
 */
@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["invoiceId"])
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val invoiceId: Long,
    val name: String,
    val description: String? = null,
    /** Number of units — Double supports fractional quantities (e.g. 1.5 hours). */
    val quantity: Double,
    /** Unit price in smallest currency unit (e.g. paise). */
    val unitPriceMinor: Long,
    /** Tax percentage applied to this line (e.g. 18.0 for 18% GST). */
    val taxPercent: Double = 0.0,
    /** Subtotal before tax: quantity × unitPriceMinor */
    val lineSubtotalMinor: Long,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val discountPercent: Double = 0.0,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val discountMinor: Long = 0L,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val taxableAmountMinor: Long = 0L,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val cgstPercent: Double = 0.0,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val cgstAmountMinor: Long = 0L,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val sgstPercent: Double = 0.0,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val sgstAmountMinor: Long = 0L,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val igstPercent: Double = 0.0,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val igstAmountMinor: Long = 0L,
    /** Tax amount on this line item. */
    val taxAmountMinor: Long,
    /** Total including tax: lineSubtotalMinor + taxAmountMinor */
    val totalMinor: Long,
    /** Creation timestamp as epoch milliseconds. */
    val createdAt: Long = System.currentTimeMillis(),
    /** Last-updated timestamp as epoch milliseconds. */
    val updatedAt: Long = System.currentTimeMillis()
)
