package com.examplerep.account.repository;

import com.examplerep.account.CustomerType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<CustomerEntity> {

    @PersistenceContext
    EntityManager entityManager;

    /**
     * Allocates the next {@code customer_id} using the database function {@code next_customer_id}.
     */
    public long nextCustomerId(CustomerType customerType) {
        Object row = entityManager
                .createNativeQuery("SELECT next_customer_id(?1)")
                .setParameter(1, customerType.name())
                .getSingleResult();
        return ((Number) row).longValue();
    }

    public void persistCustomer(@Valid CustomerEntity entity) {
        persist(entity);
    }

    public Optional<CustomerEntity> findByIdWithDetails(Long customerId) {
        return find(
                        "SELECT c FROM Customer c LEFT JOIN FETCH c.individual LEFT JOIN FETCH c.business WHERE c.customerId = ?1",
                        customerId)
                .firstResultOptional();
    }

    public List<CustomerEntity> findAllWithDetails() {
        return find(
                        "SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.individual LEFT JOIN FETCH c.business ORDER BY c.customerId")
                .list();
    }
}
