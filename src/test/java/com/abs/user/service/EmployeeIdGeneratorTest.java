package com.abs.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import org.junit.jupiter.api.Test;

class EmployeeIdGeneratorTest {

    @Test
    void generatesLowestFiveDigitValue() {
        EmployeeIdGenerator generator = new EmployeeIdGenerator(new FixedRandom(0));

        assertEquals("10000", generator.generate());
    }

    @Test
    void generatesHighestFiveDigitValue() {
        EmployeeIdGenerator generator = new EmployeeIdGenerator(new FixedRandom(89999));

        assertEquals("99999", generator.generate());
    }

    private static final class FixedRandom extends Random {

        private final int value;

        private FixedRandom(int value) {
            this.value = value;
        }

        @Override
        public int nextInt(int bound) {
            return value;
        }
    }
}