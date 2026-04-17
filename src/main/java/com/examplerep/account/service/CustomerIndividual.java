package com.examplerep.account.service;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Individual customer fields.")
public record CustomerIndividual(
        Long customerId,
        @NotBlank(message = "{customer.individual.firstName.notBlank}") String firstName,
        String middleName,
        @NotBlank(message = "{customer.individual.lastName.notBlank}") String lastName,
        String suffix) {}
