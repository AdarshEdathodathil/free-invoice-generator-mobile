package com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a customer who receives invoices.
 * Stored in the "customers" table.
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val taxNumber: String? = null,
    val country: String? = null,
    val mobile: String? = null,
    val gstin: String? = null,
    /** Creation timestamp as epoch milliseconds. */
    val createdAt: Long = System.currentTimeMillis(),
    /** Last-updated timestamp as epoch milliseconds. */
    val updatedAt: Long = System.currentTimeMillis()
)
