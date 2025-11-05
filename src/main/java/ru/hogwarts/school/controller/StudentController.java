package ru.hogwarts.school.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.*;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody StudentCreateDto studentCreateDto) {
        StudentDto createdStudent = studentService.createStudent(studentCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> findStudent(@PathVariable long id) {
        StudentDto student = studentService.findStudent(id);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateDto studentUpdateDto) {
        StudentDto updatedStudent = studentService.updateStudent(id, studentUpdateDto);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StudentDto> deleteStudent(@PathVariable long id) {
        StudentDto deletedStudent = studentService.deleteStudent(id);
        return ResponseEntity.ok(deletedStudent);
    }

    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        List<StudentDto> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/age/{age}")
    public ResponseEntity<List<StudentDto>> getStudentByAge(@PathVariable int age) {
        List<StudentDto> students = studentService.getStudentsByAge(age);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/age-between")
    public ResponseEntity<List<StudentDto>> getStudentsByAgeBetween(
            @RequestParam int minAge,
            @RequestParam int maxAge
    ) {
        List<StudentDto> students = studentService.getStudentsByAgeBetween(minAge, maxAge);
        return ResponseEntity.ok(students);
    }

    @GetMapping("{id}/faculty")
    public ResponseEntity<FacultyDto> getStudentFaculty(@PathVariable long id) {
        FacultyDto faculty = studentService.getStudentFacultyDto(id);
        return ResponseEntity.ok(faculty);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getTotalCountOfStudents() {
        Integer count = studentService.getTotalCountOfStudents();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/average-age")
    public ResponseEntity<Double> getAverageAgeOfStudents() {
        Double averageAge = studentService.getAverageAgeOfStudents();
        return ResponseEntity.ok(averageAge);
    }

    @GetMapping("/last-five")
    public ResponseEntity<List<StudentDto>> getLastFiveStudents() {
        List<StudentDto> students = studentService.getLastFiveStudents();
        return ResponseEntity.ok(students);
    }
}