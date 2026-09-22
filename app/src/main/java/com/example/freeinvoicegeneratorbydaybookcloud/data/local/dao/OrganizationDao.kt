package com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.OrganizationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "organizations" table.
 * The app is expected to have a single organization record.
 */
@Dao
interface OrganizationDao {

    // ── Reads ─────────────────────────────────────────────────────────────────

    /** Observe the first (and typically only) organization record reactively. */
    @Query("SELECT * FROM organizations LIMIT 1")
    fun observeOrganization(): Flow<OrganizationEntity?>

    /** Get the first organization once (suspend, off main thread). */
    @Query("SELECT * FROM organizations LIMIT 1")
    suspend fun getOrganization(): OrganizationEntity?

    /** Observe an organization by its specific ID. */
    @Query("SELECT * FROM organizations WHERE id = :id")
    fun observeOrganizationById(id: Long): Flow<OrganizationEntity?>

    /** Get an organization by ID once. */
    @Query("SELECT * FROM organizations WHERE id = :id")
    suspend fun getOrganizationById(id: Long): OrganizationEntity?

    /** Observe all organizations (supports future multi-org expansion). */
    @Query("SELECT * FROM organizations ORDER BY name ASC")
    fun observeOrganizations(): Flow<List<OrganizationEntity>>

    // ── Writes ────────────────────────────────────────────────────────────────

    /**
     * Insert or replace an organization.
     * @return the row ID of the inserted/replaced record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: OrganizationEntity): Long

    /** Update an existing organization record. */
    @Update
    suspend fun updateOrganization(organization: OrganizationEntity)
}

