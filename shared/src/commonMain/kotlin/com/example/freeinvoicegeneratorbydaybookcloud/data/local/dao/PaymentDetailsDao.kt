package com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.PaymentDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(details: PaymentDetailsEntity): Long

    @Update
    suspend fun update(details: PaymentDetailsEntity)

    @Query("SELECT * FROM payment_details WHERE invoiceId = :invoiceId LIMIT 1")
    suspend fun getByInvoiceId(invoiceId: Long): PaymentDetailsEntity?

    @Query("SELECT * FROM payment_details WHERE invoiceId = :invoiceId LIMIT 1")
    fun observeByInvoiceId(invoiceId: Long): Flow<PaymentDetailsEntity?>

    @Query("DELETE FROM payment_details WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: Long)
}
