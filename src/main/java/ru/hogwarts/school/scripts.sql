SELECT *
FROM students
WHERE age BETWEEN 10 and 20

SELECT name
FROM students

-- список студентов с именами, содержащими букву О (кириллица)
SELECT *
FROM students
WHERE name LIKE "%О%"

-- список студентов с именами, содержащими букву О (латиница)
SELECT *
FROM students
WHERE name LIKE "%O%"

SELECT *
FROM students
WHERE age < id

SELECT *
FROM students
ORDER BY age

SELECT *
FROM students
ORDER BY age DESC

SELECT *
FROM students
ORDER BY age, name