package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.dto.StudentCreateDto;
import ru.hogwarts.school.dto.StudentDto;
import ru.hogwarts.school.dto.StudentUpdateDto;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.mapper.StudentMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

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
    public StudentDto createStudent(StudentCreateDto studentCreateDto) {
        logger.info("Was invoked method for CREATE Student with Name: {}", studentCreateDto.name());

        Student student = studentMapper.toEntity(studentCreateDto);
        logger.debug("Mapped StudentCreateDto to Student entity: {}", studentCreateDto.name());

        if (studentCreateDto.facultyId() != null) {
            logger.debug("Setting Faculty for Student, faculty ID: {}", studentCreateDto.facultyId());

            Faculty faculty = universityManagementService.findFacultyEntity(studentCreateDto.facultyId());
            student.setFaculty(faculty);
        } else {
            logger.debug("No Faculty specidied for Student with ID: {}", student.getId());
        }

        Student savedStudent = studentRepository.save(student);
        logger.debug("Student saved to database with ID: {}", savedStudent.getId());

        StudentDto result = studentMapper.toDto(savedStudent);
        logger.info("Student successfully created with ID: {} and Name: {}", savedStudent.getId(),
                savedStudent.getName());

        return result;
    }

    public StudentDto findStudent(long id) {
        logger.info("Was invoked method for FIND Student by ID: {}", id);

        Student student = studentRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Student with ID: {} not found when ask FIND Student", id);
                    return new StudentNotFoundException(id);
                }
        );
        logger.debug("Student found: {} (ID: {}, age: {})", student.getName(), student.getId(), student.getAge());
        return studentMapper.toDto(student);
    }

    @Transactional
    public StudentDto updateStudent(Long id, StudentUpdateDto studentUpdateDto) {
        logger.info("Was invoked method for UPDATE Student with ID: {}", id);
        logger.debug("Update Student data - name: {}, age: {}, faculty ID: {}", studentUpdateDto.name(),
                studentUpdateDto.age(), studentUpdateDto.facultyId());

        Student existingStudent = findStudentEntity(id);
        logger.debug("Found existing Student: {} (ID: {}, age: {}", existingStudent.getName(),
                existingStudent.getId(), existingStudent.getAge());

        existingStudent.setName(studentUpdateDto.name());
        existingStudent.setAge(studentUpdateDto.age());
        logger.debug("Student basic fields updated");

        if (studentUpdateDto.facultyId() != null) {
            logger.debug("Updating Faculty for Student, new faculty ID: {}",studentUpdateDto.facultyId());

            Faculty faculty = universityManagementService.findFacultyEntity(studentUpdateDto.facultyId());
            existingStudent.setFaculty(faculty);
        } else {
            existingStudent.setFaculty(null);
        }

        Student updatedStudent = studentRepository.save(existingStudent);
        logger.debug("Student saved to database");

        StudentDto result = studentMapper.toDto(updatedStudent);
        logger.info("Student successfully updated with ID: {}", id);

        return result;
    }

    @Transactional
    public StudentDto deleteStudent(long id) {
        logger.info("Was invoked method for DELETE Student by ID: {}", id);

        Student student = studentRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Student not found for delete with ID: {}", id);
                    return new StudentNotFoundException(id);
                }
        );

        logger.debug("Deleting Student {} (ID: {})", student.getName(),student.getId());
        studentRepository.deleteById(id);

        StudentDto result = studentMapper.toDto(student);
        logger.info("Student successfully deleted with ID: {} and Name: {}", id, student.getName());

        return result;
    }

    public List<StudentDto> getStudentsByAge(int age) {
        logger.info("Was invoked method for get students by age: {}", age);

        List<Student> students = studentRepository.findByAge(age);
        logger.debug("Found {} students with age: {}", students.size(), age);

        return studentMapper.toDtoList(students);
    }

    public List<StudentDto> getAllStudents() {
        logger.info("Was invoked method for GET ALL students");

        List<Student> students = studentRepository.findAll();
        logger.debug("Found {} total students", students.size());

        return studentMapper.toDtoList(students);
    }

    public List<StudentDto> getStudentsByAgeBetween(int minAge, int maxAge) {
        logger.info("Was invoked method for GET students by age between {} and {}", minAge, maxAge);

        List<Student> students = studentRepository.findByAgeBetween(minAge, maxAge);
        logger.debug("Found {} students with age between {} and {}", students.size(), minAge, maxAge);

        return studentMapper.toDtoList(students);
    }

    public Integer getTotalCountOfStudents() {
        logger.info("Was invoked method for GET total count of students");

        Integer count = studentRepository.getTotalCountOfStudents();
        logger.debug("Total students count: {}", count);

        return count;
    }

    public Double getAverageAgeOfStudents() {
        logger.info("Was invoked method for GET average age of students");

        Double averageAge = studentRepository.getAverageAgeOfStudents();
        logger.debug("Average students age: {}", averageAge);

        return averageAge;
    }

    public List<StudentDto> getLastFiveStudents() {
        logger.info("Was invoked method for GET last five students");

        List<Student> students = studentRepository.getLastFiveStudents();
        logger.debug("Found {} last students", students.size());

        return studentMapper.toDtoList(students);
    }

    public Student findStudentEntity(long id) {
        logger.debug("Was invoked method for FIND student entity by ID: {}", id);

        Student student = studentRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Student entity not found with ID: {}", id);
                    return new StudentNotFoundException(id);
                }
        );

        logger.debug("Student entity found: {} (ID: {}, age: {})", student.getName(), student.getId(),
                student.getAge());
        return student;
    }

    @Transactional(readOnly = true)
    public FacultyDto getStudentFacultyDto(long studentId) {
        logger.info("Was invoked method for GET student Faculty by student id: {}", studentId);

        FacultyDto facultyDto = universityManagementService.getStudentFacultyDto(studentId);

        if (facultyDto != null) {
            logger.debug("Found Faculty: {} for Student id: {}", facultyDto.name(), studentId);
        } else {
            logger.debug("No Faculty found for student id: {}", studentId);
        }

        return facultyDto;
    }

    public List<String> getAllStudentNames() {
        logger.info("Was invoked method for GET all student names");

        List<String> names = studentRepository.findAll().stream()
                                              .map(Student::getName)
                                              .toList();

        logger.debug("Found {} student names", names.size());
        return names;
    }
}