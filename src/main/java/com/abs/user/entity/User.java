package com.abs.user.entity;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.abs.user.entity.converter.BooleanYnConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @TableGenerator(
            name = "users_id_generator",
            table = "id_generator",
            pkColumnName = "generator_name",
            valueColumnName = "next_value",
            pkColumnValue = "users",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "users_id_generator")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "employee_id", nullable = false, length = 5, unique = true)
    private String employeeId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email_id", nullable = false, length = 320, unique = true)
    private String emailId;

    @Convert(converter = BooleanYnConverter.class)
    @Column(name = "is_active", nullable = false, length = 1)
    private Boolean active;
}