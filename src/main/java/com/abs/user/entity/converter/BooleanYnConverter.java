package com.abs.user.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BooleanYnConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        if ("Y".equals(value)) {
            return Boolean.TRUE;
        }
        if ("N".equals(value)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Unsupported active flag value.");
    }
}