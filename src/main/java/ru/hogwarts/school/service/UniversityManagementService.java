package ru.hogwarts.school.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

@Service
@Transactional
public class UniversityManagementService {
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final FacultyMapper facultyMapper;

    public UniversityManagementService(FacultyRepository facultyRepository, StudentRepository studentRepository, FacultyMapper facultyMapper) {
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.facultyMapper = facultyMapper;
    }

    public void deleteFacultyWithStudents(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There is no Faculty with ID " + facultyId)
        );

        List<Student> facultyStudents = studentRepository.findByFacultyId(facultyId);
        for (Student student : facultyStudents) {
            student.setFaculty(null);
            studentRepository.save(student);
        }

        facultyRepository.deleteById(facultyId);
    }

    public Faculty findFacultyEntity(Long facultyId) {
        return facultyRepository.findById(facultyId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There is no Faculty with ID " + facultyId)
        );
    }

    public List<Student> getStudentsByFaculty(Long facultyId) {
        return studentRepository.findByFacultyId(facultyId);
    }

    @Transactional(readOnly = true)
    public FacultyDto getStudentFacultyDto(Long studentId) {
        Student student = studentRepository.findWithFacultyById(studentId)
                                           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                   "There is no Student with ID " + studentId));

        Faculty faculty = student.getFaculty();
        if (faculty == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Student with ID " + studentId + " has no Faculty");
        }

        return facultyMapper.toDto(faculty);
    }

}
