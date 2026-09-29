package com.example.freeinvoicegeneratorbydaybookcloud.domain.repository

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Organization
import kotlinx.coroutines.flow.Flow

interface OrganizationRepository {
    fun observeOrganization(): Flow<Organization?>
    suspend fun getOrganization(): Organization?
    suspend fun saveOrganization(organization: Organization): Long
    suspend fun updateOrganization(organization: Organization)
}
