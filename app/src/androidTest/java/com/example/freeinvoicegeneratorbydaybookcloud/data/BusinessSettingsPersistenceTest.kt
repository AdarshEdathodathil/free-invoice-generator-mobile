package com.example.freeinvoicegeneratorbydaybookcloud.data

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.freeinvoicegeneratorbydaybookcloud.data.preferences.BusinessSettingsRepository
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BusinessSettingsPersistenceTest {
    @Test
    fun organizationDefaultsRemainAfterRepositoryIsRecreated() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val isolatedName = "business-settings-${UUID.randomUUID()}"
        val isolatedContext = object : ContextWrapper(baseContext) {
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
                baseContext.getSharedPreferences(isolatedName, mode)
        }

        try {
            BusinessSettingsRepository(isolatedContext).update(
                name = "Saved Business",
                address = "42 Persistent Avenue",
                email = "accounts@example.com",
                phone = "+91 90000 00000"
            )

            val restored = BusinessSettingsRepository(isolatedContext).settings.value
            assertEquals("Saved Business", restored.name)
            assertEquals("42 Persistent Avenue", restored.address)
            assertEquals("accounts@example.com", restored.email)
            assertEquals("+91 90000 00000", restored.phone)
        } finally {
            baseContext.deleteSharedPreferences(isolatedName)
        }
    }
}
