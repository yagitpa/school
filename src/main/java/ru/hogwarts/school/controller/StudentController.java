package ru.hogwarts.school.controller;

import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
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
    public Student createStudent(@RequestBody Student student) {
        return studentService.createStudent(student);
    }

    @GetMapping("/{id}")
    public Student findStudent(@PathVariable long id) {
        return studentService.findStudent(id);
    }

    @PutMapping
    public Student updateStudent(@RequestBody Student student) {
        return studentService.updateStudent(student);
    }

    @DeleteMapping("/{id}")
    public Student deleteStudent(@PathVariable long id) {
        return studentService.deleteStudent(id);
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/age/{age}")
    public List<Student> getStudentByAge(@PathVariable int age) {
        return studentService.getStudentsByAge(age);
    }

    @GetMapping("/age-between")
    public List<Student> getStudentsByAgeBetween(
            @RequestParam int minAge,
            @RequestParam int maxAge
    ) {
        return studentService.getStudentsByAgeBetween(minAge, maxAge);
    }

    @GetMapping("{id}/faculty")
    public Faculty getStudentFaculty(@PathVariable long id) {
        return studentService.getStudentFaculty(id);
    }

    @GetMapping("/count")
    public Integer getTotalCountOfStudents() {
        return studentService.getTotalCountOfStudents();
    }

    @GetMapping("/average-age")
    public Double getAverageAgeOfStudents() {
        return studentService.getAverageAgeOfStudents();
    }

    @GetMapping("/last-five")
    public List<Student> getLastFiveStudents() {
        return studentService.getLastFiveStudents();
    }
}
