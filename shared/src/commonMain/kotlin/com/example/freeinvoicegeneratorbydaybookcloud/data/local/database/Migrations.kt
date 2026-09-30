package com.example.freeinvoicegeneratorbydaybookcloud.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE organizations ADD COLUMN country TEXT")
        connection.execSQL("ALTER TABLE organizations ADD COLUMN mobile TEXT")
        connection.execSQL("ALTER TABLE organizations ADD COLUMN gstin TEXT")
        connection.execSQL("ALTER TABLE organizations ADD COLUMN authorityName TEXT")
        connection.execSQL("ALTER TABLE organizations ADD COLUMN authorityDesignation TEXT")
        connection.execSQL("UPDATE organizations SET mobile = phone, gstin = taxNumber")

        connection.execSQL("ALTER TABLE customers ADD COLUMN country TEXT")
        connection.execSQL("ALTER TABLE customers ADD COLUMN mobile TEXT")
        connection.execSQL("ALTER TABLE customers ADD COLUMN gstin TEXT")
        connection.execSQL("UPDATE customers SET mobile = phone, gstin = taxNumber")

        connection.execSQL("ALTER TABLE invoices ADD COLUMN invoiceType TEXT NOT NULL DEFAULT 'SIMPLE'")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'USD'")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN currencySymbol TEXT NOT NULL DEFAULT '$'")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN decimalPlaces INTEGER NOT NULL DEFAULT 2")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN deliveryState TEXT")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN taxOption TEXT NOT NULL DEFAULT 'NON_TAXABLE'")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN dateFormat TEXT NOT NULL DEFAULT 'DD_MM_YYYY'")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN showItemDescription INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN showItemDiscount INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN internationalNumbering INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN roundOffMinor INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN additionalNotes TEXT")
        connection.execSQL("ALTER TABLE invoices ADD COLUMN termsAndConditions TEXT")
        connection.execSQL("UPDATE invoices SET additionalNotes = notes WHERE notes IS NOT NULL")

        connection.execSQL("ALTER TABLE invoice_items ADD COLUMN discountPercent REAL NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoice_items ADD COLUMN discountMinor INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoice_items ADD COLUMN taxableAmountMinor INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoice_items ADD COLUMN cgstPercent REAL NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoice_items ADD COLUMN cgstAmountMinor INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoice_items ADD COLUMN sgstPercent REAL NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoice_items ADD COLUMN sgstAmountMinor INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoice_items ADD COLUMN igstPercent REAL NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE invoice_items ADD COLUMN igstAmountMinor INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("UPDATE invoice_items SET taxableAmountMinor = lineSubtotalMinor")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS payment_details (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                invoiceId INTEGER NOT NULL,
                paymentMethod TEXT NOT NULL DEFAULT 'NONE',
                accountNumber TEXT,
                accountOwnerName TEXT,
                bankName TEXT,
                upiId TEXT,
                FOREIGN KEY(invoiceId) REFERENCES invoices(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_payment_details_invoiceId ON payment_details(invoiceId)")
    }
}
