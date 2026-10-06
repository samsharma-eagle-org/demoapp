package com.abs.user.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BooleanYnConverterTest {

    private final BooleanYnConverter converter = new BooleanYnConverter();

    @Test
    void convertsBooleanValuesToAnsiCharacters() {
        assertEquals("Y", converter.convertToDatabaseColumn(Boolean.TRUE));
        assertEquals("N", converter.convertToDatabaseColumn(Boolean.FALSE));
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertsAnsiCharactersToBooleanValues() {
        assertEquals(Boolean.TRUE, converter.convertToEntityAttribute("Y"));
        assertEquals(Boolean.FALSE, converter.convertToEntityAttribute("N"));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void rejectsUnsupportedStoredValue() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("X"));
    }
}