package ru.hogwarts.school.controller;

import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.dto.StudentDto;
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
    public FacultyDto createFaculty(@RequestBody FacultyDto facultyDto) {
        return facultyService.createFaculty(facultyDto);
    }

    @GetMapping("/{id}")
    public FacultyDto findFaculty(@PathVariable long id) {
        return facultyService.findFaculty(id);
    }

    @PutMapping
    public FacultyDto updateFaculty(@RequestBody FacultyDto facultyDto) {
        return facultyService.updateFaculty(facultyDto);
    }

    @DeleteMapping("/{id}")
    public void deleteFaculty(@PathVariable long id) {
        facultyService.deleteFaculty(id);
    }

    @GetMapping
    public List<FacultyDto> getAllFaculties() {
        return facultyService.getAllFaculties();
    }

    @GetMapping("/color/{color}")
    public List<FacultyDto> getFacultiesByColor(@PathVariable String color) {
        return facultyService.getFacultiesByColor(color);
    }

    @GetMapping("/search")
    public List<FacultyDto> getFacultiesByNameOrColor(@RequestParam String nameOrColor) {
        return facultyService.getFacultiesByNameOrColor(nameOrColor);
    }

    @GetMapping("{id}/students")
    public List<StudentDto> getFacultyStudents(@PathVariable long id) {
        return facultyService.getFacultyStudents(id);
    }
}