package ru.hogwarts.school.service;

import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
    private final FacultyService facultyService;

    private final FacultyMapper facultyMapper;

    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper,
                          @Lazy FacultyService facultyService,
                          FacultyMapper facultyMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
        this.facultyService = facultyService;
        this.facultyMapper = facultyMapper;
    }

    @Transactional
    public StudentDto createStudent(StudentDto studentDto) {
        Student student = studentMapper.toEntity(studentDto);

        if (studentDto.getFacultyId() != null) {
            Faculty faculty = facultyService.findFacultyEntity(studentDto.getFacultyId());
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
            Faculty faculty = facultyService.findFacultyEntity(studentDto.getFacultyId());
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

    public Faculty getStudentFaculty(Long studentId) {
        Student student = findStudentEntity(studentId);
        Faculty faculty = student.getFaculty();
        if (faculty == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Student with ID " + studentId + " has no faculty"
            );
        }
        return faculty;
    }

    public List<Student> getStudentsByFaculty(Long facultyId) {
        return studentRepository.findByFacultyId(facultyId);
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

    @Transactional
    public void updateStudentEntity(Student student) {
        if (!studentRepository.existsById(student.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no student with ID " + student.getId()
            );
        }
        studentRepository.save(student);
    }

    @Transactional
    public FacultyDto getStudentFacultyDto(long studentId) {
        Student student = findStudentEntity(studentId);
        Faculty faculty = student.getFaculty();

        if (faculty == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Student with ID " + studentId + " has no Faculty");
        }

        return facultyMapper.toDto(faculty);
    }
}