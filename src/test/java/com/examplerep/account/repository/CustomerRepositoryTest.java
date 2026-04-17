package com.examplerep.account.repository;

import com.examplerep.account.CustomerType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerRepositoryTest {

    @Inject
    CustomerRepository customerRepository;

    @Test
    @Order(1)
    @Transactional
    void findAllWithDetails_includesFlywaySeedRows() {
        assertEquals(2, customerRepository.findAllWithDetails().size());
    }

    @Test
    @Order(2)
    @Transactional
    void findByIdWithDetails_loadsIndividualFromSeed() {
        CustomerEntity c = customerRepository
                .findByIdWithDetails(2000000000000000000L)
                .orElseThrow();
        assertEquals(CustomerType.INDIVIDUAL, c.customerType);
        assertNotNull(c.individual);
        assertEquals("Test", c.individual.firstName);
        assertEquals("Individual", c.individual.lastName);
    }

    @Test
    @Order(3)
    @Transactional
    void persistBusiness_persistsAndFinds() {
        Instant now = Instant.now();
        CustomerEntity c = new CustomerEntity();
        c.customerId = customerRepository.nextCustomerId(CustomerType.BUSINESS);
        c.customerType = CustomerType.BUSINESS;
        c.createdAt = now;
        c.updatedAt = now;

        CustomerBusinessEntity b = new CustomerBusinessEntity();
        b.customer = c;
        b.legalName = "Repository Test LLC";
        b.createdAt = now;
        b.updatedAt = now;
        c.business = b;

        customerRepository.persistCustomer(c);

        CustomerEntity loaded =
                customerRepository.findByIdWithDetails(c.customerId).orElseThrow();
        assertEquals("Repository Test LLC", loaded.business.legalName);
    }
}
