package com.examplerep.account.api;

import com.examplerep.account.service.CustomerBusiness;
import com.examplerep.account.service.CustomerIndividual;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Update payload: send exactly one of individual or business matching the existing customer type.")
public record UpdateCustomerRequest(
        @Schema(description = "Use when customer is INDIVIDUAL") CustomerIndividual individual,
        @Schema(description = "Use when customer is BUSINESS") CustomerBusiness business) {}
