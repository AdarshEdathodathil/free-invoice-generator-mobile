package com.example.freeinvoicegeneratorbydaybookcloud.data.mapper

import com.example.freeinvoicegeneratorbydaybookcloud.data.local.entity.CustomerEntity
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Customer

/** Maps a [CustomerEntity] (Room) to its [Customer] domain model. */
fun CustomerEntity.toDomain(): Customer = Customer(
    id = id,
    name = name,
    email = email,
    phone = phone,
    address = address,
    taxNumber = taxNumber,
    country = country,
    mobile = mobile,
    gstin = gstin,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/** Maps a [Customer] domain model to its [CustomerEntity] (Room). */
fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    id = id,
    name = name,
    email = email,
    phone = phone,
    address = address,
    taxNumber = taxNumber,
    country = country,
    mobile = mobile,
    gstin = gstin,
    createdAt = createdAt,
    updatedAt = updatedAt
)
