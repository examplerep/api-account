package com.examplerep.account.service;

import com.examplerep.account.CustomerType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerServiceTest {

    private static final long SEED_BUSINESS_ID = 1000000000000000000L;
    private static final long SEED_INDIVIDUAL_ID = 2000000000000000000L;

    @Inject
    CustomerService customerService;

    @Test
    @Order(1)
    @Transactional
    void listCustomers_includesSeedData() {
        assertEquals(2, customerService.listCustomers().size());
    }

    @Test
    @Order(2)
    @Transactional
    void findCustomer_individualName_isFirstLast_andSeedMatches() {
        Customer c = customerService.findCustomer(SEED_INDIVIDUAL_ID).orElseThrow();
        assertEquals(CustomerType.INDIVIDUAL, c.customerType());
        assertEquals("Test Individual", c.name());
    }

    @Test
    @Order(3)
    @Transactional
    void findCustomer_businessName_isLegalName() {
        Customer c = customerService.findCustomer(SEED_BUSINESS_ID).orElseThrow();
        assertEquals(CustomerType.BUSINESS, c.customerType());
        assertEquals("Acme Example Corp", c.name());
    }

    @Test
    @Order(4)
    @Transactional
    void findCustomer_missing_returnsEmpty() {
        assertTrue(customerService.findCustomer(999L).isEmpty());
    }

    @Test
    @Order(5)
    @Transactional
    void createIndividual_and_createBusiness_thenDelete() {
        Customer ind =
                customerService.create(
                        new CustomerIndividual(null, "Pat", null, "Lee", null));
        assertEquals(CustomerType.INDIVIDUAL, ind.customerType());
        assertEquals("Pat Lee", ind.name());

        Customer bus =
                customerService.create(new CustomerBusiness(null, "New Biz LLC"));
        assertEquals(CustomerType.BUSINESS, bus.customerType());
        assertEquals("New Biz LLC", bus.name());

        assertTrue(customerService.delete(ind.customerId()));
        assertTrue(customerService.delete(bus.customerId()));
    }

    @Test
    @Order(6)
    @Transactional
    void createIndividual_withSuffix_nameIncludesCommaSuffix() {
        Customer c =
                customerService.create(
                        new CustomerIndividual(null, "Pat", null, "Lee", "Jr"));
        assertEquals("Pat Lee, Jr", c.name());
        assertTrue(customerService.delete(c.customerId()));
    }

    @Test
    @Order(7)
    @Transactional
    void updateIndividual_updatesCreatedCustomer() {
        Customer created =
                customerService.create(
                        new CustomerIndividual(null, "First", null, "Last", null));
        Customer updated =
                customerService.update(
                        created.customerId(),
                        new CustomerIndividual(
                                created.customerId(), "Up", "Mid", "Dated", "Sr"));
        assertEquals("Up Dated, Sr", updated.name());
        assertTrue(customerService.delete(created.customerId()));
    }

    @Test
    @Order(8)
    @Transactional
    void updateBusiness_updatesCreatedCustomer() {
        Customer created =
                customerService.create(new CustomerBusiness(null, "Before Name"));
        Customer updated =
                customerService.update(
                        created.customerId(),
                        new CustomerBusiness(created.customerId(), "After Name"));
        assertEquals("After Name", updated.name());
        assertTrue(customerService.delete(created.customerId()));
    }

    @Test
    @Order(9)
    @Transactional
    void updateIndividual_onBusinessCustomer_throwsBadRequest() {
        assertThrows(
                BadRequestException.class,
                () ->
                        customerService.update(
                                SEED_BUSINESS_ID,
                                new CustomerIndividual(null, "A", null, "B", null)));
    }

    @Test
    @Order(10)
    @Transactional
    void updateBusiness_onIndividualCustomer_throwsBadRequest() {
        assertThrows(
                BadRequestException.class,
                () ->
                        customerService.update(
                                SEED_INDIVIDUAL_ID, new CustomerBusiness(null, "X")));
    }

    @Test
    @Order(11)
    @Transactional
    void updateIndividual_notFound_throws() {
        assertThrows(
                NotFoundException.class,
                () ->
                        customerService.update(
                                1L, new CustomerIndividual(null, "A", null, "B", null)));
    }

    @Test
    @Order(12)
    @Transactional
    void deleteCustomer_unknown_returnsFalse() {
        assertFalse(customerService.delete(1L));
    }

    @Test
    @Order(13)
    @Transactional
    void createIndividual_blankFirstName_failsValidation() {
        assertThrows(
                ConstraintViolationException.class,
                () ->
                        customerService.create(
                                new CustomerIndividual(null, "", null, "Last", null)));
    }
}
