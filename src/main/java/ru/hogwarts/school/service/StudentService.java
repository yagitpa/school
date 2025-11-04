package ru.hogwarts.school.service;

import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.dto.StudentDto;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.mapper.StudentMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final UniversityManagementService universityManagementService;


    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper,
                          UniversityManagementService universityManagementService) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
        this.universityManagementService = universityManagementService;
    }

    @Transactional
    public StudentDto createStudent(StudentDto studentDto) {
        Student student = studentMapper.toEntity(studentDto);

        if (studentDto.getFacultyId() != null) {
            Faculty faculty = universityManagementService.findFacultyEntity(studentDto.getFacultyId());
            student.setFaculty(faculty);
        }
        
        Student savedStudent = studentRepository.save(student);
        return studentMapper.toDto(savedStudent);
    }

    public StudentDto findStudent(long id) {
        Student student = studentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There is no student with ID " + id
                ));
        return studentMapper.toDto(student);
    }

    @Transactional
    public StudentDto updateStudent(StudentDto studentDto) {
        if (!studentRepository.existsById(studentDto.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no student with ID " + studentDto.getId()
            );
        }

        Student student = studentMapper.toEntity(studentDto);

        if (studentDto.getFacultyId() != null) {
            Faculty faculty = universityManagementService.findFacultyEntity(studentDto.getFacultyId());
            student.setFaculty(faculty);
        } else {
            student.setFaculty(null);
        }

        Student updatedStudent = studentRepository.save(student);
        return studentMapper.toDto(updatedStudent);
    }

    @Transactional
    public StudentDto deleteStudent(long id) {
        Student student = studentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "There is no student with ID " + id
                ));
        studentRepository.deleteById(id);
        return studentMapper.toDto(student);
    }

    public List<StudentDto> getStudentsByAge(int age) {
        List<Student> students = studentRepository.findByAge(age);
        return studentMapper.toDtoList(students);
    }

    public List<StudentDto> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return studentMapper.toDtoList(students);
    }

    public List<StudentDto> getStudentsByAgeBetween(int minAge, int maxAge) {
        List<Student> students = studentRepository.findByAgeBetween(minAge, maxAge);
        return studentMapper.toDtoList(students);
    }

    public Integer getTotalCountOfStudents() {
        return studentRepository.getTotalCountOfStudents();
    }

    public Double getAverageAgeOfStudents() {
        return studentRepository.getAverageAgeOfStudents();
    }

    public List<StudentDto> getLastFiveStudents() {
        List<Student> students = studentRepository.getLastFiveStudents();
        return studentMapper.toDtoList(students);
    }

    public Student findStudentEntity(long id) {
        return studentRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "There is no student with ID " + id
                                ));
    }

    @Transactional(readOnly = true)
    public FacultyDto getStudentFacultyDto(long studentId) {
        return universityManagementService.getStudentFacultyDto(studentId);
    }
}