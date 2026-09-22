package com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "invoice_items" table.
 */
@Dao
interface InvoiceItemDao {

    // ── Reads ─────────────────────────────────────────────────────────────────

    /** Get all items for a specific invoice once (suspend). */
    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItemEntity>

    /** Observe all items for a specific invoice reactively. */
    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun observeItemsForInvoice(invoiceId: Long): Flow<List<InvoiceItemEntity>>

    // ── Writes ────────────────────────────────────────────────────────────────

    /**
     * Insert a single invoice item.
     * @return the auto-generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItem(item: InvoiceItemEntity): Long

    /**
     * Insert a list of invoice items atomically.
     * Called inside @Transaction methods in [InvoiceDao] or repository.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<InvoiceItemEntity>)

    /** Update an existing invoice item. */
    @Update
    suspend fun updateItem(item: InvoiceItemEntity)

    /** Delete a single invoice item. */
    @Delete
    suspend fun deleteItem(item: InvoiceItemEntity)

    /**
     * Delete all items for a given invoice.
     * Used during invoice updates to replace items atomically.
     */
    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Long)
}

