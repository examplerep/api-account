package com.examplerep.account.service;

import com.examplerep.account.CustomerType;
import com.examplerep.account.repository.CustomerBusinessEntity;
import com.examplerep.account.repository.CustomerEntity;
import com.examplerep.account.repository.CustomerIndividualEntity;
import com.examplerep.account.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CustomerService {

    @Inject
    CustomerRepository customerRepository;

    @Transactional
    public List<Customer> listCustomers() {
        return customerRepository.findAllWithDetails().stream().map(this::toDomain).toList();
    }

    @Transactional
    public Optional<Customer> findCustomer(Long customerId) {
        return customerRepository.findByIdWithDetails(customerId).map(this::toDomain);
    }

    @Transactional
    public Optional<CustomerDetail> findCustomerDetail(Long customerId) {
        return customerRepository.findByIdWithDetails(customerId).map(this::toDetail);
    }

    @Transactional
    public Customer create(@Valid CustomerIndividual details) {
        Instant now = Instant.now();
        CustomerEntity c = new CustomerEntity();
        c.customerId = customerRepository.nextCustomerId(CustomerType.INDIVIDUAL);
        c.customerType = CustomerType.INDIVIDUAL;
        c.createdAt = now;
        c.updatedAt = now;

        CustomerIndividualEntity ind = new CustomerIndividualEntity();
        ind.customer = c;
        ind.firstName = details.firstName();
        ind.middleName = details.middleName();
        ind.lastName = details.lastName();
        ind.suffix = details.suffix();
        ind.createdAt = now;
        ind.updatedAt = now;
        c.individual = ind;

        customerRepository.persistCustomer(c);
        return toDomain(c);
    }

    @Transactional
    public Customer create(@Valid CustomerBusiness details) {
        Instant now = Instant.now();
        CustomerEntity c = new CustomerEntity();
        c.customerId = customerRepository.nextCustomerId(CustomerType.BUSINESS);
        c.customerType = CustomerType.BUSINESS;
        c.createdAt = now;
        c.updatedAt = now;

        CustomerBusinessEntity bus = new CustomerBusinessEntity();
        bus.customer = c;
        bus.legalName = details.legalName();
        bus.createdAt = now;
        bus.updatedAt = now;
        c.business = bus;

        customerRepository.persistCustomer(c);
        return toDomain(c);
    }

    @Transactional
    public Customer update(Long customerId, @Valid CustomerIndividual details) {
        CustomerEntity c =
                customerRepository.findByIdWithDetails(customerId).orElseThrow(NotFoundException::new);
        if (c.customerType != CustomerType.INDIVIDUAL) {
            throw new BadRequestException("Customer is not INDIVIDUAL");
        }
        Instant now = Instant.now();
        CustomerIndividualEntity ind = c.individual;
        ind.firstName = details.firstName();
        ind.middleName = details.middleName();
        ind.lastName = details.lastName();
        ind.suffix = details.suffix();
        ind.updatedAt = now;
        c.updatedAt = now;
        return toDomain(c);
    }

    @Transactional
    public Customer update(Long customerId, @Valid CustomerBusiness details) {
        CustomerEntity c =
                customerRepository.findByIdWithDetails(customerId).orElseThrow(NotFoundException::new);
        if (c.customerType != CustomerType.BUSINESS) {
            throw new BadRequestException("Customer is not BUSINESS");
        }
        Instant now = Instant.now();
        CustomerBusinessEntity bus = c.business;
        bus.legalName = details.legalName();
        bus.updatedAt = now;
        c.updatedAt = now;
        return toDomain(c);
    }

    @Transactional
    public boolean delete(Long customerId) {
        return customerRepository
                .findByIdWithDetails(customerId)
                .map(
                        e -> {
                            customerRepository.delete(e);
                            return true;
                        })
                .orElse(false);
    }

    private Customer toDomain(CustomerEntity entity) {
        return new Customer(
                entity.customerId,
                entity.customerType,
                resolveDisplayName(entity),
                entity.createdAt,
                entity.updatedAt);
    }

    private CustomerDetail toDetail(CustomerEntity entity) {
        Customer summary = toDomain(entity);
        if (entity.customerType == CustomerType.INDIVIDUAL && entity.individual != null) {
            return new CustomerDetail(summary, toIndividual(entity.individual), null);
        }
        if (entity.customerType == CustomerType.BUSINESS && entity.business != null) {
            return new CustomerDetail(summary, null, toBusiness(entity.business));
        }
        return new CustomerDetail(summary, null, null);
    }

    private static CustomerIndividual toIndividual(CustomerIndividualEntity ind) {
        return new CustomerIndividual(ind.customerId, ind.firstName, ind.middleName, ind.lastName, ind.suffix);
    }

    private static CustomerBusiness toBusiness(CustomerBusinessEntity bus) {
        return new CustomerBusiness(bus.customerId, bus.legalName);
    }

    /**
     * Business: {@code legalName}. Individual: {@code firstName + " " + lastName}, then {@code ", " + suffix} when suffix
     * is present.
     */
    static String resolveDisplayName(CustomerEntity entity) {
        if (entity.customerType == CustomerType.BUSINESS) {
            if (entity.business == null) {
                return null;
            }
            return entity.business.legalName;
        }
        if (entity.individual == null) {
            return null;
        }
        return formatIndividualDisplayName(entity.individual);
    }

    static String formatIndividualDisplayName(CustomerIndividualEntity ind) {
        String base = ind.firstName.strip() + " " + ind.lastName.strip();
        if (ind.suffix != null && !ind.suffix.isBlank()) {
            return base + ", " + ind.suffix.strip();
        }
        return base;
    }
}
