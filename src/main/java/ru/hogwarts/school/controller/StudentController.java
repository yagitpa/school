package ru.hogwarts.school.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.*;
import ru.hogwarts.school.exception.FileProcessingException;
import ru.hogwarts.school.exception.InsufficientStudentsException;
import ru.hogwarts.school.exception.ThreadExecutionException;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    public static final int COUNT_NAMES = 6;

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody StudentCreateDto studentCreateDto) {
        logger.info("Was invoked POST endpoint for CREATE Student with name: {}", studentCreateDto.name());
        logger.debug("Student creation data - Name: {}, Age: {}, FacultyID: {}",
                studentCreateDto.name(), studentCreateDto.age(), studentCreateDto.facultyId());

        StudentDto createdStudent = studentService.createStudent(studentCreateDto);
        logger.info("Student was successfully created with ID: {} and Name: {}",
                createdStudent.id(), createdStudent.name());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> findStudent(@PathVariable long id) {
        logger.info("Was invoked GET endpoint for FIND Student by ID: {}", id);

        StudentDto student = studentService.findStudent(id);
        logger.debug("Student found - ID: {}, name: {}, age: {}",
                student.id(), student.name(), student.age());
        logger.info("Student successfully retrieved with ID: {}", id);

        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable Long id,
                                                    @Valid @RequestBody StudentUpdateDto studentUpdateDto) {
        logger.info("Was invoked PUT endpoint for UPDATE student with ID: {}", id);
        logger.debug("Student update data - name: {}, age: {}, facultyId: {}",
                studentUpdateDto.name(), studentUpdateDto.age(), studentUpdateDto.facultyId());

        StudentDto updatedStudent = studentService.updateStudent(id, studentUpdateDto);
        logger.info("Student successfully updated with ID: {} and new name: {}",
                id, updatedStudent.name());

        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StudentDto> deleteStudent(@PathVariable long id) {
        logger.info("Was invoked DELETE endpoint for DELETE student by ID: {}", id);

        StudentDto deletedStudent = studentService.deleteStudent(id);
        logger.info("Student successfully deleted with ID: {} and name: {}",
                id, deletedStudent.name());

        return ResponseEntity.ok(deletedStudent);
    }

    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        logger.info("Was invoked GET endpoint for GET ALL students");

        List<StudentDto> students = studentService.getAllStudents();
        logger.debug("Found {} total students", students.size());
        logger.info("Successfully retrieved {} students", students.size());

        return ResponseEntity.ok(students);
    }

    @GetMapping("/age/{age}")
    public ResponseEntity<List<StudentDto>> getStudentByAge(@PathVariable int age) {
        logger.info("Was invoked GET endpoint for GET students by age: {}", age);

        List<StudentDto> students = studentService.getStudentsByAge(age);
        logger.debug("Found {} students with age: {}", students.size(), age);
        logger.info("Successfully retrieved {} students with age {}", students.size(), age);

        return ResponseEntity.ok(students);
    }

    @GetMapping("/age-between")
    public ResponseEntity<List<StudentDto>> getStudentsByAgeBetween(@RequestParam int minAge,
                                                                    @RequestParam int maxAge) {
        logger.info("Was invoked GET endpoint for GET students by age between {} and {}", minAge, maxAge);

        List<StudentDto> students = studentService.getStudentsByAgeBetween(minAge, maxAge);
        logger.debug("Found {} students with age between {} and {}", students.size(), minAge, maxAge);
        logger.info("Successfully retrieved {} students with age between {} and {}",
                students.size(), minAge, maxAge);

        return ResponseEntity.ok(students);
    }

    @GetMapping("{id}/faculty")
    public ResponseEntity<FacultyDto> getStudentFaculty(@PathVariable long id) {
        logger.info("Was invoked GET endpoint for GET student faculty by Student ID: {}", id);

        FacultyDto faculty = studentService.getStudentFacultyDto(id);
        logger.debug("Found faculty for student ID: {} - faculty name: {}, color: {}",
                id, faculty.name(), faculty.color());
        logger.info("Successfully retrieved faculty for student ID: {}", id);

        return ResponseEntity.ok(faculty);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getTotalCountOfStudents() {
        logger.info("Was invoked GET endpoint for GET total count of students");

        Integer count = studentService.getTotalCountOfStudents();
        logger.debug("Total students count: {}", count);
        logger.info("Successfully retrieved total students count: {}", count);

        return ResponseEntity.ok(count);
    }

    @GetMapping("/average-age")
    public ResponseEntity<Double> getAverageAgeOfStudents() {
        logger.info("Was invoked GET endpoint for GET average age of students");

        Double averageAge = studentService.getAverageAgeOfStudents();
        logger.debug("Average students age: {}", averageAge);
        logger.info("Successfully retrieved average students age: {}", averageAge);

        return ResponseEntity.ok(averageAge);
    }

    @GetMapping("/last-five")
    public ResponseEntity<List<StudentDto>> getLastFiveStudents() {
        logger.info("Was invoked GET endpoint for GET last five students");

        List<StudentDto> students = studentService.getLastFiveStudents();
        logger.debug("Found {} last students", students.size());
        logger.info("Successfully retrieved last {} students", students.size());

        return ResponseEntity.ok(students);
    }

    @GetMapping("/print-parallel")
    public ResponseEntity<String> printStudentsParallel() {
        logger.info("Was invoked GET endpoint for PRINT students names in parallel mode");

        List<String> studentNames = studentService.getAllStudentNames();
        logger.debug("Retrieved {} student names for parallel printing", studentNames.size());

        if (studentNames.size() < COUNT_NAMES) {
            logger.warn("Not enough student names for parallel printing. Required: {}, found: {}",
                    COUNT_NAMES, studentNames.size());
            throw new InsufficientStudentsException(COUNT_NAMES, studentNames.size());
        }

        logger.info("Starting parallel printing for {} student names", COUNT_NAMES);

        System.out.println("Main thread: " + studentNames.get(0));
        System.out.println("Main thread: " + studentNames.get(1));
        logger.debug("Main thread printed first two names");

        Thread thread1 = new Thread(() -> {
            logger.debug("Parallel Thread 1 started");
            System.out.println("Parallel Thread 1: " + studentNames.get(2));
            System.out.println("Parallel Thread 1: " + studentNames.get(3));
            logger.debug("Parallel Thread 1 completed");
        });

        Thread thread2 = new Thread(() -> {
            logger.debug("Parallel Thread 2 started");
            System.out.println("Parallel Thread 2: " + studentNames.get(4));
            System.out.println("Parallel Thread 2: " + studentNames.get(5));
            logger.debug("Parallel Thread 2 completed");
        });

        thread1.start();
        thread2.start();
        logger.debug("Parallel Threads 1 and 2 started");

        try {
            thread1.join();
            thread2.join();
            logger.debug("Parallel Threads 1 and 2 successfully completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread execution was interrupted during parallel printing", e);
            throw new ThreadExecutionException("parallel printing", e);
        }

        return ResponseEntity.ok("Students printed in parallel mode");
    }

    @GetMapping("/print-synchronized")
    public ResponseEntity<String> printStudentsSynchronized() {
        logger.info("Was invoked GET endpoint for PRINT student names in synchronized mode");

        List<String> studentNames = studentService.getAllStudentNames();
        logger.debug("Retrieved {} student names for synchronized printing", studentNames.size());

        if (studentNames.size() < COUNT_NAMES) {
            logger.warn("Not enough student names for synchronized printing. Required: {}, found: {}",
                    COUNT_NAMES, studentNames.size());
            throw new InsufficientStudentsException(COUNT_NAMES, studentNames.size());
        }

        logger.info("Starting synchronized printing of {} student names", studentNames.size());

        printNameSynchronized(studentNames.get(0), "Main Thread");
        printNameSynchronized(studentNames.get(1), "Main Thread");
        logger.debug("Main Thread printed first two names using synchronized method");

        Thread thread1 = new Thread(() -> {
            logger.debug("Parallel Thread 1 started for synchronized printing");
            printNameSynchronized(studentNames.get(2), "Parallel Thread 1");
            printNameSynchronized(studentNames.get(3), "Parallel Thread 1");
            logger.debug("Parallel Thread 1 completed synchronized printing");
        });

        Thread thread2 = new Thread(() -> {
            logger.debug("Parallel Thread 2 started for synchronized printing");
            printNameSynchronized(studentNames.get(4), "Parallel Thread 2");
            printNameSynchronized(studentNames.get(5), "Parallel Thread 2");
            logger.debug("Parallel Thread 2 completed synchronized printing");
        });

        thread1.start();
        thread2.start();
        logger.debug("Parallel synchronized Threads 1 and 2 started");

        try {
            thread1.join();
            thread2.join();
            logger.debug("Parallel synchronized Threads 1 and 2 successfully completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread execution was interrupted during synchronized printing", e);
            throw new ThreadExecutionException("synchronized printing", e);
        }

        return ResponseEntity.ok("Students printed in synchronized mode");
    }

    private synchronized void printNameSynchronized(String name, String threadName) {
        logger.trace("Entering synchronized print method for Thread: {}", threadName);
        System.out.println(threadName + ": " + name);
        logger.trace("Exiting synchronized print method for Thread: {}", threadName);
    }
}