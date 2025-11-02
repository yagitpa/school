-- liquibase formatted sql

-- changeset hogwarts_dev:1
CREATE INDEX idx_students_name ON students(name);

-- changeset hogwarts_dev:2
CREATE INDEX idx_faculties_name_color ON faculties(name, color);