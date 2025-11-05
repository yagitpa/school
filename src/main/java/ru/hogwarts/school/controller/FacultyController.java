package ru.hogwarts.school.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.*;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;

@RestController
@RequestMapping("/faculty")
public class FacultyController {
    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PostMapping
    public ResponseEntity<FacultyDto> createFaculty(@Valid @RequestBody FacultyCreateDto facultyCreateDto) {
        FacultyDto createdFaculty = facultyService.createFaculty(facultyCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFaculty);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyDto> findFaculty(@PathVariable long id) {
        FacultyDto faculty = facultyService.findFaculty(id);
        return ResponseEntity.ok(faculty);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyDto> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody FacultyUpdateDto facultyUpdateDto) {
        FacultyDto updatedFaculty = facultyService.updateFaculty(id, facultyUpdateDto);
        return ResponseEntity.ok(updatedFaculty);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FacultyDto>> getAllFaculties() {
        List<FacultyDto> faculties = facultyService.getAllFaculties();
        return ResponseEntity.ok(faculties);
    }

    @GetMapping("/color/{color}")
    public ResponseEntity<List<FacultyDto>> getFacultiesByColor(@PathVariable String color) {
        List<FacultyDto> faculties = facultyService.getFacultiesByColor(color);
        return ResponseEntity.ok(faculties);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FacultyDto>> getFacultiesByNameOrColor(@RequestParam String nameOrColor) {
        List<FacultyDto> faculties = facultyService.getFacultiesByNameOrColor(nameOrColor);
        return ResponseEntity.ok(faculties);
    }

    @GetMapping("{id}/students")
    public ResponseEntity<List<StudentDto>> getFacultyStudents(@PathVariable long id) {
        List<StudentDto> students = facultyService.getFacultyStudents(id);
        return ResponseEntity.ok(students);
    }
}