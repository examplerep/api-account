package com.examplerep.account.service;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Customer aggregate with type-specific fields (individual or business).")
public record CustomerDetail(
        @Schema(required = true) Customer customer,
        @Schema(description = "Present when customerType is INDIVIDUAL") CustomerIndividual individual,
        @Schema(description = "Present when customerType is BUSINESS") CustomerBusiness business) {}
