package com.example.freeinvoicegeneratorbydaybookcloud.data.repository

import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.OrganizationDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.mapper.toDomain
import com.example.freeinvoicegeneratorbydaybookcloud.data.mapper.toEntity
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Organization
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.OrganizationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class OrganizationRepositoryImpl(
    private val dao: OrganizationDao
) : OrganizationRepository {

    override fun observeOrganization(): Flow<Organization?> =
        dao.observeOrganization().map { it?.toDomain() }

    override suspend fun getOrganization(): Organization? =
        dao.getOrganization()?.toDomain()

    override suspend fun saveOrganization(organization: Organization): Long =
        dao.insertOrganization(organization.toEntity())

    override suspend fun updateOrganization(organization: Organization) =
        dao.updateOrganization(organization.toEntity())
}

