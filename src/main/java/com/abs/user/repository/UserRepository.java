package com.abs.user.repository;

import com.abs.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailId(String emailId);

    boolean existsByEmployeeId(String employeeId);
}