package ru.hogwarts.school.controller;

import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.dto.StudentDto;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {
    private final StudentService studentService;

    private final FacultyMapper facultyMapper;

    public StudentController(StudentService studentService,
                             FacultyMapper facultyMapper) {
        this.studentService = studentService;
        this.facultyMapper = facultyMapper;
    }

    @PostMapping
    public StudentDto createStudent(@RequestBody StudentDto studentDto) {
        return studentService.createStudent(studentDto);
    }

    @GetMapping("/{id}")
    public StudentDto findStudent(@PathVariable long id) {
        return studentService.findStudent(id);
    }

    @PutMapping
    public StudentDto updateStudent(@RequestBody StudentDto studentDto) {
        return studentService.updateStudent(studentDto);
    }

    @DeleteMapping("/{id}")
    public StudentDto deleteStudent(@PathVariable long id) {
        return studentService.deleteStudent(id);
    }

    @GetMapping
    public List<StudentDto> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/age/{age}")
    public List<StudentDto> getStudentByAge(@PathVariable int age) {
        return studentService.getStudentsByAge(age);
    }

    @GetMapping("/age-between")
    public List<StudentDto> getStudentsByAgeBetween(
            @RequestParam int minAge,
            @RequestParam int maxAge
    ) {
        return studentService.getStudentsByAgeBetween(minAge, maxAge);
    }

    @GetMapping("{id}/faculty")
    public FacultyDto getStudentFaculty(@PathVariable long id) {
        return studentService.getStudentFacultyDto(id);
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
    public List<StudentDto> getLastFiveStudents() {
        return studentService.getLastFiveStudents();
    }
}