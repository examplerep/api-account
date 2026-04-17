package com.examplerep.account.service;

import com.examplerep.account.CustomerType;
import java.time.Instant;

public record Customer(
        Long customerId,
        CustomerType customerType,
        String name,
        Instant createdAt,
        Instant updatedAt) {}
