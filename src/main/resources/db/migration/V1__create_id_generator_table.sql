CREATE TABLE id_generator (
    generator_name VARCHAR(50) NOT NULL,
    next_value BIGINT NOT NULL,
    CONSTRAINT pk_id_generator PRIMARY KEY (generator_name)
);

INSERT INTO id_generator (generator_name, next_value) VALUES ('users', 1);