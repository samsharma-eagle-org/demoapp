package com.abs.user;

import org.junit.jupiter.api.Test;

class UserRegistrationApplicationTest {

    @Test
    void mainStartsApplicationContext() {
        UserRegistrationApplication.main(new String[] {
                "--spring.main.web-application-type=none",
                "--spring.datasource.url=jdbc:h2:mem:user-registration-main;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
        });
    }
}