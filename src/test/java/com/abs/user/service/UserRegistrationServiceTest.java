package com.abs.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.abs.user.dto.UserRegistrationRequest;
import com.abs.user.dto.UserResponse;
import com.abs.user.entity.User;
import com.abs.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeIdGenerator employeeIdGenerator;

    @Mock
    private UserCreationAttempt userCreationAttempt;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void registerCreatesUserWithBaseEmailAndGeneratedEmployeeId() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");

        when(employeeIdGenerator.generate()).thenReturn("48291");
        when(userRepository.existsByEmployeeId("48291")).thenReturn(false);
        when(userCreationAttempt.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userRegistrationService.register(request);

        assertEquals("48291", response.getUserId());
        assertEquals("Ada", response.getFirstName());
        assertEquals("Lovelace", response.getLastName());
        assertEquals("ada.lovelace@abs.com", response.getEmailId());
        assertEquals(Boolean.TRUE, response.getActive());
        verify(userCreationAttempt).create(any(User.class));
    }

    @Test
    void selectsAlphabeticEmailSuffixAfterBaseCollision() {
        UserRegistrationRequest request = request("Ada", "Lovelace");
        when(employeeIdGenerator.generate()).thenReturn("48291");
        when(userRepository.existsByEmployeeId("48291")).thenReturn(false);
        when(userRepository.existsByEmailId("ada.lovelace@abs.com")).thenReturn(true);
        when(userRepository.existsByEmailId("ada.a.lovelace@abs.com")).thenReturn(true);
        when(userRepository.existsByEmailId("ada.b.lovelace@abs.com")).thenReturn(true);
        when(userRepository.existsByEmailId("ada.c.lovelace@abs.com")).thenReturn(false);
        when(userCreationAttempt.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userRegistrationService.register(request);

        assertEquals("ada.c.lovelace@abs.com", response.getEmailId());
    }

    @Test
    void rollsEmailSuffixFromZToAa() {
        UserRegistrationRequest request = request("Ada", "Lovelace");
        when(employeeIdGenerator.generate()).thenReturn("48291");
        when(userRepository.existsByEmployeeId("48291")).thenReturn(false);
        when(userRepository.existsByEmailId("ada.lovelace@abs.com")).thenReturn(true);
        for (char suffix = 'a'; suffix <= 'z'; suffix++) {
            when(userRepository.existsByEmailId("ada." + suffix + ".lovelace@abs.com")).thenReturn(true);
        }
        when(userRepository.existsByEmailId("ada.aa.lovelace@abs.com")).thenReturn(false);
        when(userCreationAttempt.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userRegistrationService.register(request);

        assertEquals("ada.aa.lovelace@abs.com", response.getEmailId());
    }

    @Test
    void normalizesAccentsAndPunctuationInEmailSegments() {
        UserRegistrationRequest request = request("  José-Anne! ", "O'Neil ");
        when(employeeIdGenerator.generate()).thenReturn("48291");
        when(userRepository.existsByEmployeeId("48291")).thenReturn(false);
        when(userRepository.existsByEmailId("joseanne.oneil@abs.com")).thenReturn(false);
        when(userCreationAttempt.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userRegistrationService.register(request);

        assertEquals("José-Anne!", response.getFirstName());
        assertEquals("O'Neil", response.getLastName());
        assertEquals("joseanne.oneil@abs.com", response.getEmailId());
    }

    @Test
    void rejectsNameWhenEmailNormalizationProducesEmptySegment() {
        UserRegistrationRequest request = request("!!!", "Lovelace");

        org.junit.jupiter.api.Assertions.assertThrows(
                com.abs.user.exception.BusinessValidationException.class,
                () -> userRegistrationService.register(request));
    }

    @Test
    void rejectsLastNameWhenEmailNormalizationProducesEmptySegment() {
        UserRegistrationRequest request = request("Ada", "!!!");

        org.junit.jupiter.api.Assertions.assertThrows(
                com.abs.user.exception.BusinessValidationException.class,
                () -> userRegistrationService.register(request));
    }

    @Test
    void retriesEmployeeIdCollisionWithoutChangingAvailableEmail() {
        UserRegistrationRequest request = request("Ada", "Lovelace");
        when(employeeIdGenerator.generate()).thenReturn("48291", "58291");
        when(userRepository.existsByEmployeeId("48291")).thenReturn(false);
        when(userRepository.existsByEmployeeId("58291")).thenReturn(false);
        when(userRepository.existsByEmailId("ada.lovelace@abs.com")).thenReturn(false);
        doThrow(new DataIntegrityViolationException("employee collision",
                new ConstraintViolationException("employee collision", null, "uq_users_employee_id")))
                .doAnswer(invocation -> invocation.getArgument(0))
                .when(userCreationAttempt).create(any(User.class));

        UserResponse response = userRegistrationService.register(request);

        assertEquals("ada.lovelace@abs.com", response.getEmailId());
        assertEquals("58291", response.getUserId());
    }

    @Test
    void regeneratesEmployeeIdWhenExistenceCheckFindsCollision() {
        UserRegistrationRequest request = request("Ada", "Lovelace");
        when(employeeIdGenerator.generate()).thenReturn("48291", "58291");
        when(userRepository.existsByEmployeeId("48291")).thenReturn(true);
        when(userRepository.existsByEmployeeId("58291")).thenReturn(false);
        when(userRepository.existsByEmailId("ada.lovelace@abs.com")).thenReturn(false);
        when(userCreationAttempt.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userRegistrationService.register(request);

        assertEquals("58291", response.getUserId());
        verify(userRepository).existsByEmployeeId("48291");
        verify(userRepository).existsByEmployeeId("58291");
    }

    @Test
    void retriesEmailCollisionWithNextSuffixAndFreshEmployeeId() {
        UserRegistrationRequest request = request("Ada", "Lovelace");
        when(employeeIdGenerator.generate()).thenReturn("48291", "58291");
        when(userRepository.existsByEmployeeId("48291")).thenReturn(false);
        when(userRepository.existsByEmployeeId("58291")).thenReturn(false);
        when(userRepository.existsByEmailId("ada.lovelace@abs.com")).thenReturn(false);
        when(userRepository.existsByEmailId("ada.a.lovelace@abs.com")).thenReturn(false);
        doThrow(new DataIntegrityViolationException("email collision",
                new ConstraintViolationException("email collision", null, "uq_users_email_id")))
                .doAnswer(invocation -> invocation.getArgument(0))
                .when(userCreationAttempt).create(any(User.class));

        UserResponse response = userRegistrationService.register(request);

        assertEquals("ada.a.lovelace@abs.com", response.getEmailId());
        assertEquals("58291", response.getUserId());
    }

    @Test
    void propagatesUnrecognizedPersistenceConstraintFailure() {
        UserRegistrationRequest request = request("Ada", "Lovelace");
        when(employeeIdGenerator.generate()).thenReturn("48291");
        when(userRepository.existsByEmployeeId("48291")).thenReturn(false);
        when(userRepository.existsByEmailId("ada.lovelace@abs.com")).thenReturn(false);
        doThrow(new DataIntegrityViolationException("other constraint",
                new ConstraintViolationException("other constraint", null, "other_constraint")))
                .when(userCreationAttempt).create(any(User.class));

        org.junit.jupiter.api.Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> userRegistrationService.register(request));
    }

            @Test
            void propagatesPersistenceFailureWithoutNamedConstraint() {
            UserRegistrationRequest request = request("Ada", "Lovelace");
            when(employeeIdGenerator.generate()).thenReturn("48291");
            when(userRepository.existsByEmployeeId("48291")).thenReturn(false);
            when(userRepository.existsByEmailId("ada.lovelace@abs.com")).thenReturn(false);
            when(userCreationAttempt.create(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("unnamed constraint"));

            org.junit.jupiter.api.Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> userRegistrationService.register(request));
            }

    private UserRegistrationRequest request(String firstName, String lastName) {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        return request;
    }
}