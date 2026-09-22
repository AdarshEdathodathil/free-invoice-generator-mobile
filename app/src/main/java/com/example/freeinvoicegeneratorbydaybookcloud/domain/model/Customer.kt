package com.example.freeinvoicegeneratorbydaybookcloud.domain.model

/**
 * Domain model representing a customer who receives invoices.
 * Framework-independent — no Room or Android annotations.
 */
data class Customer(
    val id: Long = 0L,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val taxNumber: String? = null,
    val country: String? = null,
    val mobile: String? = null,
    val gstin: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
