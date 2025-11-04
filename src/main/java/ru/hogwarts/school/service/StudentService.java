package ru.hogwarts.school.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

@Service
public class StudentService {
    private static final Logger LOG = LoggerFactory.getLogger(StudentService.class);
    private final StudentRepository studentRepository;

    private final FacultyRepository facultyRepository;

    private final FacultyService facultyService;

    public StudentService(StudentRepository studentRepository,
                          FacultyRepository facultyRepository,
                          FacultyService facultyService) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.facultyService = facultyService;
    }

    @Transactional
    public Student createStudent(Student student) {
        LOG.info("Was invoked method for CREATE Student");
        LOG.debug("Create Student: name={}, age={}", student.getName(), student.getAge());

        Student savedStudent = studentRepository.save(student);
        LOG.debug("Student was created successfully with ID: {}", savedStudent.getId());
        return savedStudent;
    }

    public Student findStudent(long id) {
        LOG.info("Was invoked method for GET Student by ID: {}", id);
        LOG.debug("Searching for Student with ID: {}", id);

        return studentRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Find_Student. There is no Student with ID: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "There is no Student with ID " + id
                    );
                });
    }

    @Transactional
    public Student updateStudent(Student student) {
        LOG.info("Was invoked method for UPDATE Student");
        LOG.debug("Updating Student with ID: {}, name {}, age {}", student.getId(), student.getName(), student.getAge());

        if (!studentRepository.existsById(student.getId())) {
            LOG.warn("Attempt to UPDATE non-existing Student with ID: {}", student.getId());
            LOG.error("Update_Student. There is no Student with ID: {}", student.getId());
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no student with ID " + student.getId()
            );
        }
        Student updatedStudent = studentRepository.save(student);
        LOG.debug("Student updated successfully: {}", updatedStudent);
        return updatedStudent;
    }

    @Transactional
    public Student deleteStudent(long id) {
        LOG.info("Was invoked method for DELETE Student by ID: {}", id);
        LOG.debug("Deleting Student with ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error("Delete_Student. There is no Student with ID: {}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "There is no Student with ID " + id
                    );
                });

        studentRepository.deleteById(id);
        LOG.info("Student was deleted successfully: {}", student);
        return student;
    }

    public List<Student> getStudentsByAge(int age) {
        LOG.info("Was invoked method for GET students by age: {}", age);
        LOG.debug("Searching students with Age: {}", age);

        List<Student> students = studentRepository.findByAge(age);
        LOG.debug("Found {} students with Age: {}", students.size(), age);
        return students;
    }

    public List<Student> getAllStudents() {
        LOG.info("Was invoked method for GET all students");

        List<Student> students = studentRepository.findAll();
        LOG.debug("Retrieved {} students from database", students.size());
        return students;
    }

    public List<Student> getStudentsByAgeBetween(int minAge, int maxAge) {
        LOG.info("Was invoked method for GET students by Age between {} abd {}", minAge, maxAge);
        LOG.debug("Searching students with Age between {} and {}", minAge, maxAge);

        List<Student> students = studentRepository.findByAgeBetween(minAge, maxAge);
        LOG.debug("Found {} students in Age range {}-{}", students.size(), minAge, maxAge);
        return students;
    }

    public Faculty getStudentFaculty(Long studentId) {
        Student student = findStudent(studentId);
        return student.getFaculty();
    }

    public List<Student> getStudentsByFaculty(Long facultyId) {
        return studentRepository.findByFacultyId(facultyId);
    }

    public Integer getTotalCountOfStudents() {
        LOG.info("Was invoked method for GET total count of students");

        Integer count = studentRepository.getTotalCountOfStudents();
        LOG.debug("Total students count: {}", count);
        return count;
    }

    public Double getAverageAgeOfStudents() {
        LOG.info("Was invoked method for GET average Age of students");

        Double avgAge = studentRepository.getAverageAgeOfStudents();
        LOG.debug("Average students Age: {}", avgAge);
        return avgAge;
    }

    public List<Student> getLastFiveStudents() {
        LOG.info("Was invoked method for GET last Five students");

        List<Student> students = studentRepository.getLastFiveStudents();
        LOG.debug("Retrieved last {} students", students.size());
        return students;
    }
}