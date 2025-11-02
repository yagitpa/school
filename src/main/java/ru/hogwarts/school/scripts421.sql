ALTER TABLE students ADD CONSTRAINT student_min_age CHECK (age >= 16);

ALTER TABLE students ALTER COLUMN name SET NOT NULL;
ALTER TABLE students ADD CONSTRAINT student_name_unique UNIQUE (name);

ALTER TABLE faculties ADD CONSTRAINT faculty_name_color_unique UNIQUE (name, color);

ALTER TABLE students ALTER COLUMN age SET DEFAULT 20;