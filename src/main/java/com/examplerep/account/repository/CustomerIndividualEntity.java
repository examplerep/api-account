package com.examplerep.account.repository;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Objects;

@Entity(name = "CustomerIndividual")
@Table(name = "customer_individual")
public class CustomerIndividualEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    public Long customerId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @MapsId
    public CustomerEntity customer;

    @NotBlank(message = "{customer.individual.firstName.notBlank}")
    @Column(name = "first_name", nullable = false)
    public String firstName;

    @Column(name = "middle_name")
    public String middleName;

    @NotBlank(message = "{customer.individual.lastName.notBlank}")
    @Column(name = "last_name", nullable = false)
    public String lastName;

    @Column(name = "suffix")
    public String suffix;

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
        CustomerIndividualEntity that = (CustomerIndividualEntity) o;
        return customerId != null && Objects.equals(customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CustomerIndividualEntity{customerId=" + customerId + "}";
    }
}
