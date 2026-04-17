package com.examplerep.account.repository;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Objects;

@Entity(name = "CustomerBusiness")
@Table(name = "customer_business")
public class CustomerBusinessEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    public Long customerId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @MapsId
    public CustomerEntity customer;

    @NotBlank(message = "{customer.business.legalName.notBlank}")
    @Column(name = "legal_name", nullable = false)
    public String legalName;

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
        CustomerBusinessEntity that = (CustomerBusinessEntity) o;
        return customerId != null && Objects.equals(customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CustomerBusinessEntity{customerId=" + customerId + "}";
    }
}
