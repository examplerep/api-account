package com.examplerep.account.repository;

import com.examplerep.account.CustomerType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Objects;

@Entity(name = "Customer")
@Table(name = "customer")
public class CustomerEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    public Long customerId;

    @Column(name = "customer_type", nullable = false, columnDefinition = "smallint")
    public CustomerType customerType;

    @Valid
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    public CustomerIndividualEntity individual;

    @Valid
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    public CustomerBusinessEntity business;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomerEntity that = (CustomerEntity) o;
        return customerId != null && Objects.equals(customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CustomerEntity{customerId=" + customerId + ", customerType=" + customerType + "}";
    }
}
