package com.examplerep.account.api;

import com.examplerep.account.CustomerType;
import com.examplerep.account.service.CustomerBusiness;
import com.examplerep.account.service.CustomerIndividual;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Create a customer. Set customerType and include exactly one of individual or business.")
public record CreateCustomerRequest(
        @NotNull @Schema(required = true) CustomerType customerType,
        @Schema(description = "Required when customerType is INDIVIDUAL") CustomerIndividual individual,
        @Schema(description = "Required when customerType is BUSINESS") CustomerBusiness business) {}
