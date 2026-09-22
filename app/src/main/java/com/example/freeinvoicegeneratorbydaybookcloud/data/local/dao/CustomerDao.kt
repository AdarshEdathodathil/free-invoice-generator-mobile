package com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "customers" table.
 */
@Dao
interface CustomerDao {

    // ── Reads ─────────────────────────────────────────────────────────────────

    /** Observe all customers, sorted alphabetically by name. */
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun observeCustomers(): Flow<List<CustomerEntity>>

    /** Get a single customer by ID once. */
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    /** Observe a single customer reactively by ID. */
    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeCustomerById(id: Long): Flow<CustomerEntity?>

    /**
     * Search customers by name, email, or phone using a LIKE pattern.
     * Results are sorted alphabetically.
     *
     * @param query Pass "%" + searchTerm + "%" from the repository (e.g. "%john%").
     */
    @Query("""
        SELECT * FROM customers
        WHERE name LIKE :query
           OR email LIKE :query
           OR phone LIKE :query
        ORDER BY name ASC
    """)
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    // ── Writes ────────────────────────────────────────────────────────────────

    /**
     * Insert a new customer.
     * @return the row ID of the inserted record.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    /** Update an existing customer record. */
    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    /** Delete a customer record. */
    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
}

