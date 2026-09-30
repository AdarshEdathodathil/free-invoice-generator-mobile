package com.example.freeinvoicegeneratorbydaybookcloud.data.mapper

import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.OrganizationEntity
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Organization

/** Maps an [OrganizationEntity] (Room) to its [Organization] domain model. */
fun OrganizationEntity.toDomain(): Organization = Organization(
    id = id,
    name = name,
    address = address,
    email = email,
    phone = phone,
    taxNumber = taxNumber,
    country = country,
    mobile = mobile,
    gstin = gstin,
    authorityName = authorityName,
    authorityDesignation = authorityDesignation,
    logoPath = logoPath,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/** Maps an [Organization] domain model to its [OrganizationEntity] (Room). */
fun Organization.toEntity(): OrganizationEntity = OrganizationEntity(
    id = id,
    name = name,
    address = address,
    email = email,
    phone = phone,
    taxNumber = taxNumber,
    country = country,
    mobile = mobile,
    gstin = gstin,
    authorityName = authorityName,
    authorityDesignation = authorityDesignation,
    logoPath = logoPath,
    createdAt = createdAt,
    updatedAt = updatedAt
)
