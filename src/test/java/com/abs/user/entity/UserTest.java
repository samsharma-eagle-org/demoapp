package com.abs.user.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void lombokConstructorsBuilderSettersAndGettersCoverUserFields() {
        User user = User.builder()
                .id(1L)
                .employeeId("48291")
                .firstName("Ada")
                .lastName("Lovelace")
                .emailId("ada.lovelace@abs.com")
                .active(true)
                .build();
            assertNotNull(User.builder().toString());

        assertEquals(Long.valueOf(1L), user.getId());
        assertEquals("48291", user.getEmployeeId());
        assertEquals("Ada", user.getFirstName());
        assertEquals("Lovelace", user.getLastName());
        assertEquals("ada.lovelace@abs.com", user.getEmailId());
        assertTrue(user.getActive());

        User empty = new User();
        empty.setId(2L);
        empty.setEmployeeId("58291");
        empty.setFirstName("Grace");
        empty.setLastName("Hopper");
        empty.setEmailId("grace.hopper@abs.com");
        empty.setActive(false);

        assertEquals(Long.valueOf(2L), empty.getId());
        assertEquals("58291", empty.getEmployeeId());
        assertEquals("Grace", empty.getFirstName());
        assertEquals("Hopper", empty.getLastName());
        assertEquals("grace.hopper@abs.com", empty.getEmailId());
        assertEquals(Boolean.FALSE, empty.getActive());
    }
}