package com.example.freeinvoicegeneratorbydaybookcloud.data.repository

import com.example.freeinvoicegeneratorbydaybookcloud.data.local.dao.CustomerDao
import com.example.freeinvoicegeneratorbydaybookcloud.data.mapper.toDomain
import com.example.freeinvoicegeneratorbydaybookcloud.data.mapper.toEntity
import com.example.freeinvoicegeneratorbydaybookcloud.domain.model.Customer
import com.example.freeinvoicegeneratorbydaybookcloud.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed implementation of [CustomerRepository].
 */
class CustomerRepositoryImpl @Inject constructor(
    private val dao: CustomerDao
) : CustomerRepository {

    override fun observeCustomers(): Flow<List<Customer>> =
        dao.observeCustomers().map { list -> list.map { it.toDomain() } }

    override suspend fun getCustomerById(id: Long): Customer? =
        dao.getCustomerById(id)?.toDomain()

    override fun observeCustomerById(id: Long): Flow<Customer?> =
        dao.observeCustomerById(id).map { it?.toDomain() }

    override suspend fun saveCustomer(customer: Customer): Long =
        dao.insertCustomer(customer.toEntity())

    override suspend fun updateCustomer(customer: Customer) =
        dao.updateCustomer(customer.toEntity())

    override suspend fun deleteCustomer(customer: Customer) =
        dao.deleteCustomer(customer.toEntity())

    override fun searchCustomers(query: String): Flow<List<Customer>> {
        val likeQuery = "%$query%"
        return dao.searchCustomers(likeQuery).map { list -> list.map { it.toDomain() } }
    }
}

