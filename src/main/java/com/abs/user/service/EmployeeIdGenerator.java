package com.abs.user.service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

public class EmployeeIdGenerator {

    private final Random random;

    public EmployeeIdGenerator() {
        this(new SecureRandom());
    }

    EmployeeIdGenerator(Random random) {
        this.random = random;
    }

    public String generate() {
        int employeeId = 10000 + random.nextInt(90000);
        return String.format(Locale.ROOT, "%05d", employeeId);
    }
}