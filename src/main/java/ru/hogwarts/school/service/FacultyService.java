package ru.hogwarts.school.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.dto.FacultyCreateDto;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.dto.FacultyUpdateDto;
import ru.hogwarts.school.dto.StudentDto;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.mapper.StudentMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;

@Service
public class FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyRepository facultyRepository;
    private final FacultyMapper facultyMapper;
    private final UniversityManagementService universityManagementService;
    private final StudentMapper studentMapper;

    public FacultyService(FacultyRepository facultyRepository, FacultyMapper facultyMapper,
                          UniversityManagementService universityManagementService,
                          StudentMapper studentMapper) {
        this.facultyRepository = facultyRepository;
        this.facultyMapper = facultyMapper;
        this.universityManagementService = universityManagementService;
        this.studentMapper = studentMapper;
    }

    @Transactional
    public FacultyDto createFaculty(FacultyCreateDto facultyCreateDto) {
        logger.info("Was invoked method for CREATE Faculty with name: {}", facultyCreateDto.name());

        Faculty faculty = facultyMapper.toEntity(facultyCreateDto);
        logger.debug("Mapped FacultyCreateDto to Faculty entity: {}", facultyCreateDto.name());

        Faculty savedFaculty = facultyRepository.save(faculty);
        logger.debug("Faculty saved to database with ID: {}", savedFaculty.getId());

        FacultyDto result = facultyMapper.toDto(savedFaculty);
        logger.info("Faculty successfully created with ID: {} and name: {}", savedFaculty.getId(),
                savedFaculty.getName());

        return result;
    }

    public FacultyDto findFaculty(long id) {
        logger.info("Was invoked method for FIND Faculty by ID: {}", id);

        Faculty faculty = facultyRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Faculty not found with ID: {} when ask FIND Faculty", id);
                    return new FacultyNotFoundException(id);
                }
        );

        logger.debug("Faculty found: {} (ID: {})", faculty.getName(), faculty.getId());
        return facultyMapper.toDto(faculty);
    }

    @Transactional
    public FacultyDto updateFaculty(Long id, FacultyUpdateDto facultyUpdateDto) {
        logger.info("Was invoked method for UPDATE Faculty with ID: {}", id);
        logger.debug("UPDATE Faculty data - name: {}, color: {}", facultyUpdateDto.name(), facultyUpdateDto.color());

        Faculty existingFaculty = findFacultyEntity(id);
        logger.debug("Found existing Faculty: {} (ID: {})", existingFaculty.getName(), existingFaculty.getId());

        existingFaculty.setName(facultyUpdateDto.name());
        existingFaculty.setColor(facultyUpdateDto.color());
        logger.debug("Faculty (ID: {}) fields updated", id);

        Faculty updatedFaculty = facultyRepository.save(existingFaculty);
        logger.debug("Faculty (ID: {}) saved to database", id);

        FacultyDto result = facultyMapper.toDto(updatedFaculty);
        logger.info("Faculty successfully updated with ID: {}", id);

        return result;
    }

    @Transactional
    public void deleteFaculty(Long facultyId) {
        logger.info("Was invoked method for DELETE Faculty with ID: {}", facultyId);

        logger.debug("Delegating Faculty (ID: {}) delete operation to UniversityManagementService", facultyId);
        universityManagementService.deleteFacultyWithStudents(facultyId);

        logger.info("Faculty with ID: {} successfully deleted", facultyId);
    }

    public List<FacultyDto> getFacultiesByColor(String color) {
        logger.info("Was invoked method for GET faculties filtered by Color: {}", color);

        List<Faculty> faculties = facultyRepository.findByColorIgnoreCase(color);
        logger.debug("Found {} faculties with Color: {}", faculties.size(), color);

        return facultyMapper.toDtoList(faculties);
    }

    public List<FacultyDto> getAllFaculties() {
        logger.info("Was invoked method for GET ALL faculties");

        List<Faculty> faculties = facultyRepository.findAll();
        logger.debug("Found {} total faculties", faculties.size());

        return facultyMapper.toDtoList(faculties);
    }

    public List<FacultyDto> getFacultiesByNameOrColor(String nameOrColor) {
        logger.info("Was invoked method for GET faculties by Name or Color: {}", nameOrColor);

        List<Faculty> faculties = facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(nameOrColor, nameOrColor);
        logger.debug("Found {} total faculties matching by Name or Color: {}", faculties.size(), nameOrColor);

        return facultyMapper.toDtoList(faculties);
    }

    public List<StudentDto> getFacultyStudents(Long facultyId) {
        logger.info("Was invoked method for GET Faculty students for faculty ID: {}", facultyId);

        if (!facultyRepository.existsById(facultyId)) {
            logger.error("Faculty not found with ID: {} when ask for FIND faculty students", facultyId);
            throw new FacultyNotFoundException(facultyId);
        }

        logger.info("Faculty exist, retrieving students");
        List<Student> students = universityManagementService.getStudentsByFaculty(facultyId);
        logger.debug("Found {} total students for Faculty with ID: {}", students.size(), facultyId);

        return studentMapper.toDtoList(students);
    }

    public Faculty findFacultyEntity(long id) {
        logger.info("Was invoked method for FIND faculty entity by ID: {}", id);

        Faculty faculty = facultyRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Faculty entity not found with ID: {}", id);
                    return new FacultyNotFoundException(id);
                }
        );

        logger.debug("Faculty entity found: {} (ID: {})", faculty.getName(), id);
        return faculty;
    }
}