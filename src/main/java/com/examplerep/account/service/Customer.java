package com.examplerep.account.service;

import com.examplerep.account.CustomerType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Customer summary (display name and timestamps).")
public record Customer(
        Long customerId,
        CustomerType customerType,
        String name,
        Instant createdAt,
        Instant updatedAt) {}
