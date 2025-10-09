package ru.hogwarts.school.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private final Map<Long, Student> students = new HashMap<>();
    private long counter = 0;

    public Student createStudent(Student student) {
        student.setId(++counter);
        students.put(student.getId(), student);
        return student;
    }

    public Student findStudent(long id) {
        Student student = students.get(id);
        if (student == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no student with ID " + id
            );
        }
        return student;
    }

    public Student updateStudent(Student student) {
        if (!students.containsKey(student.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no student with ID " + student.getId()
            );
        }
        students.put(student.getId(), student);
        return student;
    }

    public Student deleteStudent(long id) {
        if (!students.containsKey(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no student with ID " + id
            );
        }
        return students.remove(id);
    }

    public List<Student> getStudentsByAge(int age) {
        return students.values().stream()
                .filter(student -> student.getAge() == age)
                .collect(Collectors.toList());
    }

    public List<Student> getAllStudents() {
        return List.copyOf(students.values());
    }
}
