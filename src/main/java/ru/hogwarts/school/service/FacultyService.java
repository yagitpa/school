package ru.hogwarts.school.service;

import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;

@Service
public class FacultyService {
    private final FacultyRepository facultyRepository;
    private final StudentService studentService;
    private final FacultyMapper facultyMapper;

    public FacultyService(FacultyRepository facultyRepository, @Lazy StudentService studentService,
                          FacultyMapper facultyMapper) {
        this.facultyRepository = facultyRepository;
        this.studentService = studentService;
        this.facultyMapper = facultyMapper;
    }

    @Transactional
    public FacultyDto createFaculty(FacultyDto facultyDto) {
        Faculty faculty = facultyMapper.toEntity(facultyDto);
        Faculty savedFaculty = facultyRepository.save(faculty);
        return facultyMapper.toDto(savedFaculty);
    }

    public FacultyDto findFaculty(long id) {
        Faculty faculty = facultyRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There is no faculty with ID " + id)
        );
        return facultyMapper.toDto(faculty);
    }

    @Transactional
    public FacultyDto updateFaculty(FacultyDto facultyDto) {
        if (!facultyRepository.existsById(facultyDto.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no faculty with ID " + facultyDto.getId()
            );
        }
        Faculty faculty = facultyMapper.toEntity(facultyDto);
        Faculty updatedFaculty = facultyRepository.save(faculty);
        return facultyMapper.toDto(updatedFaculty);
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
            studentService.updateStudentEntity(student);
        }
        facultyRepository.deleteById(facultyId);
    }

    public List<FacultyDto> getFacultiesByColor(String color) {
        List<Faculty> faculties = facultyRepository.findByColorIgnoreCase(color);
        return facultyMapper.toDtoList(faculties);
    }

    public List<FacultyDto> getAllFaculties() {
        List<Faculty> faculties = facultyRepository.findAll();
        return facultyMapper.toDtoList(faculties);
    }

    public List<FacultyDto> getFacultiesByNameOrColor(String nameOrColor) {
        List<Faculty> faculties = facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(nameOrColor, nameOrColor);
        return facultyMapper.toDtoList(faculties);
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

    public Faculty findFacultyEntity(long id) {
        return facultyRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "There is no faculty with ID " + id
                                ));
    }
}