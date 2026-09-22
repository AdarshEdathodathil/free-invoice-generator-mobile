package com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.PaymentMethod

@Entity(
    tableName = "payment_details",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceId"], unique = true)]
)
data class PaymentDetailsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceId: Long,
    @ColumnInfo(defaultValue = "'NONE'") val paymentMethod: PaymentMethod = PaymentMethod.NONE,
    val accountNumber: String? = null,
    val accountOwnerName: String? = null,
    val bankName: String? = null,
    val upiId: String? = null
)
