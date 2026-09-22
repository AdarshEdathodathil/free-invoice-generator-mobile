package com.example.freeinvoicegeneratorbydaybookcloud.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.database.AppDatabase
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.database.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {
    private val dbName = "migration-1-2-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrationPreservesVersionOneInvoiceAndAddsDefaults() {
        helper.createDatabase(dbName, 1).apply {
            execSQL("INSERT INTO organizations (id,name,address,phone,taxNumber,createdAt,updatedAt) VALUES (1,'Old Co','Old Address','123','GST-OLD',1,1)")
            execSQL("INSERT INTO customers (id,name,address,phone,taxNumber,createdAt,updatedAt) VALUES (1,'Old Customer','Address','456','CUST-GST',1,1)")
            execSQL("INSERT INTO invoices (id,invoiceNumber,organizationId,customerId,invoiceDate,dueDate,subtotalMinor,taxAmountMinor,discountMinor,totalMinor,status,notes,createdAt,updatedAt) VALUES (1,'OLD-001',1,1,1,2,10000,0,0,10000,'DRAFT','Legacy note',1,1)")
            execSQL("INSERT INTO invoice_items (id,invoiceId,name,description,quantity,unitPriceMinor,taxPercent,lineSubtotalMinor,taxAmountMinor,totalMinor,createdAt,updatedAt) VALUES (1,1,'Old Item',NULL,1,10000,0,10000,0,10000,1,1)")
            close()
        }

        helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2).apply {
            query("SELECT invoiceType,currencyCode,taxOption,additionalNotes FROM invoices WHERE id=1").use {
                assertEquals(true, it.moveToFirst())
                assertEquals("SIMPLE", it.getString(0))
                assertEquals("USD", it.getString(1))
                assertEquals("NON_TAXABLE", it.getString(2))
                assertEquals("Legacy note", it.getString(3))
            }
            query("SELECT mobile,gstin FROM organizations WHERE id=1").use {
                it.moveToFirst(); assertEquals("123", it.getString(0)); assertEquals("GST-OLD", it.getString(1))
            }
            query("SELECT taxableAmountMinor FROM invoice_items WHERE id=1").use {
                it.moveToFirst(); assertEquals(10_000L, it.getLong(0))
            }
            close()
        }
    }
}
