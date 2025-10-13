package ru.hogwarts.school.service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;

@Service
public class FacultyService {
    private final FacultyRepository facultyRepository;
    private final StudentService studentService;

    public FacultyService(FacultyRepository facultyRepository, StudentService studentService) {
        this.facultyRepository = facultyRepository;
        this.studentService = studentService;
    }

    @Transactional
    public Faculty createFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "There is no faculty with ID " + id
                ));
    }

    @Transactional
    public Faculty updateFaculty(Faculty faculty) {
        if (!facultyRepository.existsById(faculty.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no faculty with ID " + faculty.getId()
            );
        }
        return facultyRepository.save(faculty);
    }

    @Transactional
    public void deleteFaculty(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "There is no faculty with ID " + facultyId
                ));
        List<Student> facultyStudents = studentService.getStudentsByFaculty(facultyId);
        for (Student student : facultyStudents) {
            student.setFaculty(null);
            studentService.updateStudent(student);
        }
        facultyRepository.deleteById(facultyId);
    }

    public List<Faculty> getFacultiesByColor(String color) {
        return facultyRepository.findByColorIgnoreCase(color);
    }

    public List<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    public List<Faculty> getFacultiesByNameOrColor(String nameOrColor) {
        return facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(nameOrColor, nameOrColor);
    }

    public List<Student> getFacultyStudents(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no faculty with ID " + facultyId
            );
        }
        return studentService.getStudentsByFaculty(facultyId);
    }
}