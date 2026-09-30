package com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.InvoiceEntity
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.relation.InvoiceWithItems
import kotlinx.coroutines.flow.Flow
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.InvoiceType

/**
 * Data Access Object for the "invoices" table.
 *
 * Transactional operations (save/update with items) are implemented here
 * with @Transaction to ensure atomicity.
 */
@Dao
interface InvoiceDao {

    // ── Reads ─────────────────────────────────────────────────────────────────

    /** Observe all invoices, newest first (by invoiceDate). */
    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC, id DESC")
    fun observeInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoiceType = :invoiceType ORDER BY invoiceDate DESC, id DESC")
    fun observeInvoicesByType(invoiceType: InvoiceType): Flow<List<InvoiceEntity>>

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC, id DESC")
    fun observeInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC, id DESC LIMIT :limit")
    fun observeRecentInvoicesWithItems(limit: Int): Flow<List<InvoiceWithItems>>

    /** Get a single invoice by ID once. */
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    /** Observe a single invoice reactively by ID. */
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeInvoiceById(id: Long): Flow<InvoiceEntity?>

    /** Observe invoices filtered by status string (e.g. "DRAFT", "PAID"). */
    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY invoiceDate DESC, id DESC")
    fun observeInvoicesByStatus(status: String): Flow<List<InvoiceEntity>>

    /**
     * Search invoices by invoice number using a LIKE pattern.
     * Results are newest first.
     *
     * @param query Pass "%" + searchTerm + "%" from the repository.
     */
    @Query("SELECT * FROM invoices WHERE invoiceNumber LIKE :query ORDER BY invoiceDate DESC, id DESC")
    fun searchInvoices(query: String): Flow<List<InvoiceEntity>>

    /**
     * Check whether an invoice number already exists in the database.
     * Useful for unique-number enforcement at the business-logic layer.
     */
    @Query("SELECT COUNT(*) > 0 FROM invoices WHERE invoiceNumber = :invoiceNumber")
    suspend fun invoiceNumberExists(invoiceNumber: String): Boolean

    // ── Relation query ────────────────────────────────────────────────────────

    /**
     * Observe an invoice together with all its line items in a single query.
     * The @Transaction annotation ensures both reads are part of the same DB transaction.
     */
    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeInvoiceWithItems(id: Long): Flow<InvoiceWithItems?>

    // ── Writes ────────────────────────────────────────────────────────────────

    /**
     * Insert a new invoice.
     * @return the auto-generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    /** Update an existing invoice. */
    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    /** Delete an invoice (FK CASCADE removes its items automatically). */
    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)
}
