CREATE TABLE cars (
    id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    price DECIMAL(12, 2) NOT NULL CHECK (price > 0)
);

CREATE TABLE people (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INTEGER NOT NULL CHEK (age >= 0),
    has_diver_license BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE cars_and_people (
    person_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    PRIMARY KEY (person_id, car_id),
    CONSTRAINT people_cars_person
        FOREIGN KEY (person_id) REFERENCES people(id),
    CONSTRAINT people_cars_car
        FOREIGN KEY (car_id) REFERENCES cars(id)
);

INSERT INTO cars (brand, model, price) VALUES
    ('Toyota', 'Corolla', 12000.00),
    ('Honda', 'Accord', 18000.00),
    ('Ford', 'Focus', 13000.00);

INSERT INTO people (name, age, has_driver_license) VALUES
    ('Petr Petrov', 33, true),
    ('Ivan Ivanov', 30, true),
    ('Alisa Yandex', 0, false);

INSERT INTO cars_and_people (person_id, car_id) VALUE
    (1, 1),
    (1, 2),
    (2, 2),
    (2, 3);
