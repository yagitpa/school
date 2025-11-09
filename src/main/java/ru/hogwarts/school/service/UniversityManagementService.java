package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

@Service
@Transactional
public class UniversityManagementService {

    private static final Logger logger = LoggerFactory.getLogger(UniversityManagementService.class);

    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final FacultyMapper facultyMapper;

    public UniversityManagementService(FacultyRepository facultyRepository, StudentRepository studentRepository, FacultyMapper facultyMapper) {
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.facultyMapper = facultyMapper;
    }

    public void deleteFacultyWithStudents(Long facultyId) {
        logger.info("Was invoked method for DELETE Faculty with students, faculty ID: {}", facultyId);

        Faculty faculty = facultyRepository.findById(facultyId).orElseThrow(
                () -> {
                    logger.error("Faculty not found for deletion with ID: {}", facultyId);
                    return new FacultyNotFoundException(facultyId);
                }
        );

        logger.debug("Found Faculty to delete: {} (ID: {})", faculty.getName(), facultyId);

        List<Student> facultyStudents = studentRepository.findByFacultyId(facultyId);
        logger.debug("Found {} students associated with faculty id: {}", facultyStudents.size(), facultyId);

        if (!facultyStudents.isEmpty()) {
            logger.info("Removing faculty association from {} students", facultyStudents.size());

            for (Student student : facultyStudents) {
                logger.debug("Removing Faculty from Student: {} (id: {})", student.getName(), student.getId());

                student.setFaculty(null);
                studentRepository.save(student);
            }

            logger.info("Faculty associations removed from all students");
        } else {
            logger.debug("No students associated with faculty id: {}", facultyId);
        }

        facultyRepository.deleteById(facultyId);
        logger.info("Faculty successfully deleted with ID: {} and Name: {}", facultyId, faculty.getName());
    }

    public Faculty findFacultyEntity(Long facultyId) {
        logger.debug("Was invoked method for FIND faculty entity by ID: {}", facultyId);

        Faculty faculty = facultyRepository.findById(facultyId).orElseThrow(
                () -> {
                    logger.error("Faculty entity not found with ID: {}", facultyId);
                    return new FacultyNotFoundException(facultyId);
                }
        );

        logger.debug("Faculty entity found: {} (ID: {})", faculty.getName(), faculty.getId());
        return faculty;
    }

    public List<Student> getStudentsByFaculty(Long facultyId) {
        logger.info("Was invoked method for GET students by faculty ID: {}", facultyId);

        List<Student> students = studentRepository.findByFacultyId(facultyId);
        logger.debug("Found {} students for faculty ID: {}", students.size(), facultyId);

        return students;
    }

    @Transactional(readOnly = true)
    public FacultyDto getStudentFacultyDto(Long studentId) {
        logger.info("Was invoked method for GET student Faculty DTO by student ID: {}", studentId);

        Student student = studentRepository.findWithFacultyById(studentId)
                                           .orElseThrow(() -> {
                                               logger.error("Student not found with id: {}", studentId);
                                               return new StudentNotFoundException(studentId);
                                           });

        logger.debug("Found Student: {} (ID: {})", student.getName(), studentId);

        Faculty faculty = student.getFaculty();
        if (faculty == null) {
            logger.warn("Student with ID: {} has no Faculty assigned", studentId);
            throw FacultyNotFoundException.forStudentWithoutFaculty(studentId);
        }

        logger.debug("Found Faculty for Student: {} (faculty ID: {}, faculty Name: {})",
                student.getName(), faculty.getId(), faculty.getName());

        FacultyDto facultyDto = facultyMapper.toDto(faculty);
        logger.info("Successfully retrieved Faculty DTO for student ID: {}", studentId);

        return facultyDto;
    }
}