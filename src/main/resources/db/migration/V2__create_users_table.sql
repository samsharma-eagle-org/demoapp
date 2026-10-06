CREATE TABLE users (
    id BIGINT NOT NULL,
    employee_id VARCHAR(5) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email_id VARCHAR(320) NOT NULL,
    is_active VARCHAR(1) DEFAULT 'Y' NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_employee_id UNIQUE (employee_id),
    CONSTRAINT uq_users_email_id UNIQUE (email_id),
    CONSTRAINT ck_users_is_active CHECK (is_active IN ('Y', 'N'))
);