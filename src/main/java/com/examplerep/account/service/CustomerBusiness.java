package com.examplerep.account.service;

import jakarta.validation.constraints.NotBlank;

public record CustomerBusiness(Long customerId, @NotBlank(message = "{customer.business.legalName.notBlank}") String legalName) {}
