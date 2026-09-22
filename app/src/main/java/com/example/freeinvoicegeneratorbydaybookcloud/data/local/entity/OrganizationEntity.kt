package com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the business/organization that issues invoices.
 * Stored in the "organizations" table.
 */
@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val address: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val taxNumber: String? = null,
    val country: String? = null,
    val mobile: String? = null,
    val gstin: String? = null,
    val authorityName: String? = null,
    val authorityDesignation: String? = null,
    /** Absolute path to the organization logo image on device storage. */
    val logoPath: String? = null,
    /** Creation timestamp as epoch milliseconds. */
    val createdAt: Long = System.currentTimeMillis(),
    /** Last-updated timestamp as epoch milliseconds. */
    val updatedAt: Long = System.currentTimeMillis()
)
