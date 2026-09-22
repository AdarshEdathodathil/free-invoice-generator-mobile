package com.example.freeinvoicegeneratorbydaybookcloud.domain.repository

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Customer
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for customer data.
 * Implementations live in the data layer; this interface has no knowledge of Room.
 */
interface CustomerRepository {

    /** Observe all customers, alphabetically ordered. */
    fun observeCustomers(): Flow<List<Customer>>

    /** Get a single customer by ID, or null if not found. */
    suspend fun getCustomerById(id: Long): Customer?

    /** Observe a single customer reactively, or emit null if not found. */
    fun observeCustomerById(id: Long): Flow<Customer?>

    /**
     * Save a new customer.
     * @return the row ID of the inserted record.
     */
    suspend fun saveCustomer(customer: Customer): Long

    /** Update an existing customer record. */
    suspend fun updateCustomer(customer: Customer)

    /** Delete a customer record. */
    suspend fun deleteCustomer(customer: Customer)

    /**
     * Search customers by name, email, or phone.
     * @param query LIKE pattern (e.g. "John").
     */
    fun searchCustomers(query: String): Flow<List<Customer>>
}

