package com.abs.user.service;

import java.text.Normalizer;
import java.util.Locale;

import com.abs.user.dto.UserRegistrationRequest;
import com.abs.user.dto.UserResponse;
import com.abs.user.entity.User;
import com.abs.user.exception.BusinessValidationException;
import com.abs.user.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final EmployeeIdGenerator employeeIdGenerator;
    private final UserCreationAttempt userCreationAttempt;

    public UserResponse register(UserRegistrationRequest request) {
        String firstName = request.getFirstName().trim();
        String lastName = request.getLastName().trim();
        String normalizedFirstName = normalizeEmailSegment(firstName);
        String normalizedLastName = normalizeEmailSegment(lastName);

        if (normalizedFirstName.isEmpty() || normalizedLastName.isEmpty()) {
            throw new BusinessValidationException(
                    "INVALID_NAME_FOR_EMAIL",
                    "Each name must contain at least one letter or digit for email generation.");
        }

        int emailSuffixIndex = 0;
        while (true) {
            String emailId = buildEmail(normalizedFirstName, normalizedLastName, emailSuffixIndex);
            if (userRepository.existsByEmailId(emailId)) {
                log.warn("event=user_email_collision suffix_index={}", emailSuffixIndex);
                emailSuffixIndex++;
                continue;
            }

            String employeeId = generateAvailableEmployeeId();
            User candidate = User.builder()
                    .employeeId(employeeId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .emailId(emailId)
                    .active(Boolean.TRUE)
                    .build();

            try {
                User savedUser = userCreationAttempt.create(candidate);
                log.info("event=user_created");
                return toResponse(savedUser);
            } catch (DataIntegrityViolationException exception) {
                String constraintName = constraintName(exception);
                if (isConstraint(constraintName, "uq_users_email_id")) {
                    log.warn("event=user_email_collision suffix_index={}", emailSuffixIndex);
                    emailSuffixIndex++;
                } else if (isConstraint(constraintName, "uq_users_employee_id")) {
                    log.warn("event=user_employee_id_collision");
                } else {
                    throw exception;
                }
            }
        }
    }

    private String buildEmail(String firstName, String lastName, int suffixIndex) {
        if (suffixIndex == 0) {
            return firstName + "." + lastName + "@abs.com";
        }
        return firstName + "." + alphabeticSuffix(suffixIndex - 1) + "." + lastName + "@abs.com";
    }

    private String alphabeticSuffix(int index) {
        StringBuilder suffix = new StringBuilder();
        int remaining = index;
        do {
            suffix.insert(0, (char) ('a' + remaining % 26));
            remaining = remaining / 26 - 1;
        } while (remaining >= 0);
        return suffix.toString();
    }

    private String constraintName(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException) {
                return ((ConstraintViolationException) cause).getConstraintName();
            }
            cause = cause.getCause();
        }
        return null;
    }

    private boolean isConstraint(String actualName, String expectedName) {
        return actualName != null
                && actualName.toLowerCase(Locale.ROOT).contains(expectedName.toLowerCase(Locale.ROOT));
    }

    private String generateAvailableEmployeeId() {
        String employeeId = employeeIdGenerator.generate();
        while (userRepository.existsByEmployeeId(employeeId)) {
            employeeId = employeeIdGenerator.generate();
        }
        return employeeId;
    }

    private String normalizeEmailSegment(String name) {
        String decomposed = Normalizer.normalize(name, Normalizer.Form.NFKD);
        String withoutMarks = decomposed.replaceAll("\\p{M}+", "");
        return withoutMarks.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getEmployeeId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailId(user.getEmailId())
                .active(user.getActive())
                .build();
    }
}