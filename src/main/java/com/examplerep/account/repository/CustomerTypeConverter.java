package com.examplerep.account.repository;

import com.examplerep.account.CustomerType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link CustomerType} as {@code SMALLINT}: BUSINESS = 1, INDIVIDUAL = 2. */
@Converter(autoApply = true)
public class CustomerTypeConverter implements AttributeConverter<CustomerType, Short> {

    public static final short BUSINESS_CODE = 1;
    public static final short INDIVIDUAL_CODE = 2;

    @Override
    public Short convertToDatabaseColumn(CustomerType attribute) {
        if (attribute == null) {
            return null;
        }
        return switch (attribute) {
            case BUSINESS -> BUSINESS_CODE;
            case INDIVIDUAL -> INDIVIDUAL_CODE;
        };
    }

    @Override
    public CustomerType convertToEntityAttribute(Short db) {
        if (db == null) {
            return null;
        }
        return switch (db) {
            case BUSINESS_CODE -> CustomerType.BUSINESS;
            case INDIVIDUAL_CODE -> CustomerType.INDIVIDUAL;
            default -> throw new IllegalArgumentException("Unknown customer_type code: " + db);
        };
    }
}
