package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hogwarts.school.service.AnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/students/names-starting-with")
    public List<String> getStudentNamesStartingWith(@RequestParam(value = "letter",
            defaultValue = "A") String startingLetter) {
        logger.info("Was invoked GET endpoint for student names starting with {}", startingLetter);
        return analyticsService.getStudentNamesStartingWith(startingLetter);
    }

    @GetMapping("/students/average-age-students")
    public Double getAverageAgeOfStudents() {
        logger.info("Was invoked GET endpoint for average age of students");
        return analyticsService.getAverageAgeOfStudents();
    }

    @GetMapping("/faculties/longest-name-faculty")
    public String getLongestFacultyName() {
        logger.info("Was invoked GET endpoint for longest faculty name");
        return analyticsService.getLongestFacultyName();
    }

    @GetMapping("/compute/original-sum")
    public Integer calculateOriginalSum() {
        logger.info("Was invoked GET endpoint for original sum calculation");
        return analyticsService.calculateOriginalSum();
    }

    @GetMapping("/compute/optimized-sum")
    public Long calculateOptimizedSum() {
        logger.info("Was invoked GET endpoint for optimized sum calculation");
        return analyticsService.calculateOptimizedSum();
    }

    @GetMapping("/compute/math-sum")
    public Long calculateMathSum() {
        logger.info("Was invoked GET endpoint for mathematical sum calculation");
        return analyticsService.calculateMathSum();
    }
}
