package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private static final int LIMIT = 1_000_000;

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public AnalyticsService(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    public List<String> getStudentNamesStartingWith(String startingLetter) {
        logger.info("Was invoked method for GET student names starting with '{}'", startingLetter);

        if (startingLetter == null || startingLetter.trim().isEmpty()) {
            logger.warn("Starting letter is null or empty");
            return List.of();
        }

        String normalizedLetter = startingLetter.trim().toUpperCase();
        logger.debug("Normalized starting letter: {}", normalizedLetter);

        List<String> names = studentRepository.findAll().stream()
                                              .map(Student::getName)
                                              .map(String::toUpperCase)
                                              .filter(upperCase -> upperCase.startsWith(normalizedLetter))
                                              .sorted()
                                              .toList();

        logger.debug("Found {} student names starting with '{}'", names.size(), normalizedLetter);
        return names;
    }

    public Double getAverageAgeOfStudents() {
        logger.info("Was invoked method to GET average Age of students");

        Double averageAge = studentRepository.findAll().stream()
                                             .mapToInt(Student::getAge)
                                             .average()
                                             .orElse(0.0);

        logger.debug("Calculated average Age: {}", averageAge);
        return averageAge;
    }

    public String getLongestFacultyName() {
        logger.info("Was invoked method for GET longest faculty name");

        String longestName = facultyRepository.findAll().stream()
                                              .map(Faculty::getName)
                                              .max(Comparator.comparingInt(String::length))
                                              .orElse("");

        logger.debug("Longest faculty name: {} ({} characters)", longestName, longestName.length());
        return longestName;
    }

    public Integer calculateOriginalSum() {
        logger.info("Was invoked method for CALCULATE original sum from 1 to {}", LIMIT);

        long startTime = System.currentTimeMillis();
        int sum = Stream.iterate(1, a -> a + 1)
                        .limit(LIMIT)
                        .reduce(0, (a, b) -> a + b);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        logger.info("Original sum calculation completed in {} ms. Result = {}", duration, sum);
        return sum;
    }

    public Long calculateOptimizedSum() {
        logger.info("Was invoked method for CALCULATE optimized sum from 1 to {}", LIMIT);

        long startTime = System.currentTimeMillis();
        long sum = LongStream.rangeClosed(1, LIMIT)
                                    .parallel()
                                    .sum();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        logger.info("Optimized sum calculation comleted in {} ms. Result = {}", duration, sum);
        return sum;
    }
}
