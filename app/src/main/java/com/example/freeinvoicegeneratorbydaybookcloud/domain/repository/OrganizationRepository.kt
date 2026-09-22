package com.example.freeinvoicegeneratorbydaybookcloud.domain.repository

import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Organization
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for organization data.
 * Implementations live in the data layer; this interface has no knowledge of Room.
 */
interface OrganizationRepository {

    /** Observe the single organization (emits null if none saved yet). */
    fun observeOrganization(): Flow<Organization?>

    /** Get the organization once (suspend, off main thread). */
    suspend fun getOrganization(): Organization?

    /**
     * Save a new organization or replace if one already exists.
     * @return the row ID of the inserted/replaced record.
     */
    suspend fun saveOrganization(organization: Organization): Long

    /** Update an existing organization record. */
    suspend fun updateOrganization(organization: Organization)
}

