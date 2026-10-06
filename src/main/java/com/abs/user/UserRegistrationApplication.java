package com.abs.user;

import com.abs.user.service.EmployeeIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserRegistrationApplication {

    @Bean
    public EmployeeIdGenerator employeeIdGenerator() {
        return new EmployeeIdGenerator();
    }

    public static void main(String[] args) {
        SpringApplication.run(UserRegistrationApplication.class, args);
    }
}