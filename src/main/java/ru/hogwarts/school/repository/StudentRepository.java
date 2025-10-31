package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.hogwarts.school.model.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByAge(int age);

    List<Student> findByAgeBetween(int minAge, int maxAge);

    List<Student> findByFacultyId(Long facultyId);

    @Query(value = "SELECT COUNT(s) FROM Student s")
    Integer getTotalCountOfStudents();

    @Query(value = "SELECT AVG(s.age) FROM Student s")
    Double getAverageAgeOfStudents();

    @Query(value = "SELECT s.* FROM students s ORDER BY s.id DESC LIMIT 5", nativeQuery = true)
    List<Student> getLastFiveStudents();
}