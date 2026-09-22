package com.example.freeinvoicegeneratorbydaybookcloud.data

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.InvoiceSettingsRepository
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvoiceSettingsPersistenceTest {
    @Test
    fun invoiceDefaultsRemainAfterRepositoryIsRecreated() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val isolatedName = "invoice-settings-${UUID.randomUUID()}"
        val isolatedContext = object : ContextWrapper(baseContext) {
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
                baseContext.getSharedPreferences(isolatedName, mode)
        }

        try {
            InvoiceSettingsRepository(isolatedContext).update(
                prefix = "BILL-",
                taxRatePercent = 18,
                currencyCode = "INR"
            )

            val restored = InvoiceSettingsRepository(isolatedContext).settings.value
            assertEquals("BILL-", restored.prefix)
            assertEquals(18, restored.taxRatePercent)
            assertEquals("INR", restored.currencyCode)
            assertEquals("₹", restored.currency.symbol)
        } finally {
            baseContext.deleteSharedPreferences(isolatedName)
        }
    }
}
