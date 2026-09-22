package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

/**
 * Domain model representing the business/organization that issues invoices.
 * Framework-independent — no Room or Android annotations.
 */
data class Organization(
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
    val logoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
