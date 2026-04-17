package com.examplerep.account.service;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Business customer fields.")
public record CustomerBusiness(Long customerId, @NotBlank(message = "{customer.business.legalName.notBlank}") String legalName) {}
