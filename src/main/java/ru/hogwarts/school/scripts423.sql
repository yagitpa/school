SELECT
    s.name as student_name,
    s.age as student_age,
    f.name as faculty_name
FROM students s
LEFT JOIN faculties f ON s.faculty_id = f.id
ORDER BY s.name;

SELECT
    s.name as student_name,
    s.age as student_age,
    f.name as faculty_name,
    a.file_path as avatar_path
FROM students s
INNER JOIN avatars a ON s.id = a.student_id
LEFT JOIN faculties f ON s.faculty_id = f.id
ORDER BY s.name;