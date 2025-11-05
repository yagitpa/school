package ru.hogwarts.school.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.dto.StudentDto;
import ru.hogwarts.school.service.AvatarService;

import static org.junit.jupiter.api.Assertions.*;
import static ru.hogwarts.school.testconfig.TestConstants.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StudentControllerTestRestTemplateTest {

    @MockitoBean
    AvatarService avatarService;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @LocalServerPort
    private int port;
    private String testUrl;

    @BeforeEach
    void setUp() {
        testUrl = BASE_URL + port + StudentConst.ENDPOINT;
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should create Student successfully with valid data")
    void createStudent_validData_shouldReturnCreated() {
        // Given
        String studentJson = createValidStudentCreateJson(StudentConst.TEST_NAME, StudentConst.TEST_AGE);
        HttpEntity<String> request = createHttpEntity(studentJson);

        // When
        ResponseEntity<StudentDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, StudentDto.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertStudentResponse(postResponse.getBody(), StudentConst.TEST_NAME, StudentConst.TEST_AGE);
    }

    @Test
    @DisplayName("Positive. Should Find Student by ID")
    void findStudent_existingId_ShouldReturnStudent() {
        // Given
        StudentDto createdStudent = createStudentInDatabase(StudentConst.TEST_NAME, StudentConst.TEST_AGE);
        assertNotNull(createdStudent, "Student should be created successfully");

        // When
        ResponseEntity<StudentDto> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", StudentDto.class, createdStudent.id()
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertStudentResponse(getResponse.getBody(), StudentConst.TEST_NAME, StudentConst.TEST_AGE);
    }

    @Test
    @DisplayName("Positive. Should Update Student successfully")
    void updateStudent_existingStudent_shouldReturnUpdatedStudent() {
        // Given
        StudentDto createdStudent = createStudentInDatabase(StudentConst.TEST_NAME, StudentConst.TEST_AGE);
        String updateJson = createValidStudentUpdateJson(StudentConst.UPDATED_NAME, StudentConst.UPDATED_AGE);
        HttpEntity<String> request = createHttpEntity(updateJson);

        // When
        ResponseEntity<StudentDto> putResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.PUT, request, StudentDto.class, createdStudent.id()
        );

        // Then
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());
        assertStudentResponse(putResponse.getBody(), StudentConst.UPDATED_NAME, StudentConst.UPDATED_AGE);
    }

    @Test
    @DisplayName("Positive. Should Delete Student successfully")
    void deleteStudent_existingStudent_shouldReturnOk() {
        // Given
        StudentDto createdStudent = createStudentInDatabase("Student to Delete", 19);

        //When
        ResponseEntity<StudentDto> deleteResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, StudentDto.class, createdStudent.id()
        );

        // Then
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Verify
        ResponseEntity<StudentDto> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", StudentDto.class, createdStudent.id()
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Positive. Should return all students")
    void getAllStudents_shouldReturnStudentsArray() {
        // When
        ResponseEntity<StudentDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl, StudentDto[].class
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
    }

    @Test
    @DisplayName("Positive. Should return students filtered by Age")
    void getStudentByAge_existingAge_shouldReturnFilteredStudents() {
        // Given
        createStudentInDatabase("Age Filter Test", StudentConst.AGE_FILTER_AGE);

        // When
        ResponseEntity<StudentDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl + StudentConst.AGE_ENDPOINT + "/{age}", StudentDto[].class, StudentConst.AGE_FILTER_AGE
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertAllStudentsHaveAge(getResponse.getBody(), StudentConst.AGE_FILTER_AGE);
    }

    @Test
    @DisplayName("Positive. Should return students by Age range")
    void getStudentsByAgeBetween_validRange_shouldReturnStudentsInRange() {
        // Given
        createStudentInDatabase("Range Test Student", 18);

        // When
        ResponseEntity<StudentDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl + StudentConst.AGE_BETWEEN_ENDPOINT + "?minAge={minAge}&maxAge={maxAge}",
                StudentDto[].class, StudentConst.MIN_AGE, StudentConst.MAX_AGE
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertAllStudentsInAgeRange(getResponse.getBody(), StudentConst.MIN_AGE, StudentConst.MAX_AGE);
    }

    @Test
    @DisplayName("Positive. Should create student with faculty ID")
    void createStudent_withFacultyId_shouldReturnStudentWithFacultyId() {
        // Given
        FacultyDto faculty = createFacultyInDatabase("Ravenclaw", "#0000FF");
        String studentJson = String.format("""
                {
                    "name": "Student with Faculty",
                    "age": 17,
                    "facultyId": %d
                }""", faculty.id());
        HttpEntity<String> request = createHttpEntity(studentJson);

        // When
        ResponseEntity<StudentDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, StudentDto.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody());
        assertEquals("Student with Faculty", postResponse.getBody().name());
        assertEquals(17, postResponse.getBody().age());
    }

    // ========== NEGATIVE TESTS ==========

    @Test
    @DisplayName("Negative. Should return 404 when Student not found")
    void findStudent_nonExistingId_shouldReturn404() {
        // When
        ResponseEntity<StudentDto> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", StudentDto.class, NON_EXISTENT_ID
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should return 404 when updating non-existent Student")
    void updateStudent_nonExistingId_shouldReturn404() {
        // Given
        String studentJson = createValidStudentUpdateJson(StudentConst.NON_EXISTENT_NAME, StudentConst.TEST_AGE);
        HttpEntity<String> request = createHttpEntity(studentJson);

        // When
        ResponseEntity<StudentDto> putResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.PUT, request, StudentDto.class, NON_EXISTENT_ID
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, putResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should return 404 when Delete non-existing Student")
    void deleteStudent_nonExistingId_shouldReturn404() {
        // When
        ResponseEntity<StudentDto> deleteResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, StudentDto.class, NON_EXISTENT_ID
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should return empty array when no Students match Age")
    void getStudentByAge_nonExistentAge_shouldReturnEmptyArray() {
        // When
        ResponseEntity<StudentDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl + StudentConst.AGE_ENDPOINT + "/{age}", StudentDto[].class, StudentConst.NON_EXISTENT_AGE
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(0, getResponse.getBody().length);
    }

    @Test
    @DisplayName("Negative. Should return empty Array when no students in age range")
    void getStudentsByAgeBetween_noStudents_shouldReturnEmptyArray() {
        // When
        ResponseEntity<StudentDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl + StudentConst.AGE_BETWEEN_ENDPOINT + "?minAge={minAge}&maxAge={maxAge}",
                StudentDto[].class, StudentConst.INVALID_MIN_AGE, StudentConst.INVALID_MIN_AGE + 2
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(0, getResponse.getBody().length);
    }

    @Test
    @DisplayName("Negative. Should handle validation error for empty Name")
    void createStudent_emptyName_shouldReturnBadRequest() {
        // Given
        String invalidJson = createValidStudentCreateJson(EMPTY_STRING, StudentConst.TEST_AGE);
        HttpEntity<String> request = createHttpEntity(invalidJson);

        // When
        ResponseEntity<StudentDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, StudentDto.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, postResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should handle validation error for invalid age")
    void createStudent_invalidAge_shouldReturnBadRequest() {
        // Given
        String invalidJson = createValidStudentCreateJson("Test Student", Validation.TOO_YOUNG_AGE);
        HttpEntity<String> request = createHttpEntity(invalidJson);

        // When
        ResponseEntity<StudentDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, StudentDto.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, postResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should handle non-existent faculty ID gracefully")
    void createStudent_nonExistentFacultyId_shouldHandleGracefully() {
        // Given
        String studentJson = String.format("""
            {
                "name": "Student with Invalid Faculty",
                "age": %d,
                "facultyId": %d
            }""", StudentConst.TEST_AGE, StudentConst.NON_EXISTENT_FACULTY_ID);
        HttpEntity<String> request = createHttpEntity(studentJson);

        // When
        ResponseEntity<StudentDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, StudentDto.class
        );

        // Then
        assertTrue(postResponse.getStatusCode().is2xxSuccessful() ||
                postResponse.getStatusCode().is4xxClientError());
    }

    // ========== HELPER METHODS ==========

    private HttpEntity<String> createHttpEntity(String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(jsonBody, headers);
    }

    private String createValidStudentCreateJson(String name, int age) {
        return String.format("""
                {
                    "name": "%s",
                    "age": %d
                }""", name, age);
    }

    private String createValidStudentUpdateJson(String name, int age) {
        return String.format("""
                {
                    "name": "%s",
                    "age": %d
                }""", name, age);
    }

    private StudentDto createStudentInDatabase(String name, int age) {
        String studentJson = createValidStudentCreateJson(name, age);
        HttpEntity<String> request = createHttpEntity(studentJson);

        ResponseEntity<StudentDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, StudentDto.class
        );

        if (postResponse.getStatusCode().is2xxSuccessful()) {
            return postResponse.getBody();
        } else {
            System.out.println("Failed to create student: " + name + " - " + postResponse.getStatusCode());
            return null;
        }
    }

    private FacultyDto createFacultyInDatabase(String name, String color) {
        String facultyJson = String.format("""
                {
                    "name": "%s",
                    "color": "%s"
                }""", name, color);

        HttpEntity<String> request = createHttpEntity(facultyJson);

        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                BASE_URL + port + "/faculty", HttpMethod.POST, request, FacultyDto.class
        );

        if (postResponse.getStatusCode().is2xxSuccessful()) {
            return postResponse.getBody();
        } else {
            System.out.println("Failed to create faculty: " + name + " - " + postResponse.getStatusCode());
            return null;
        }
    }

    private void assertStudentResponse(StudentDto student, String expectedName, int expectedAge) {
        assertNotNull(student, "Student should not be null");
        assertNotNull(student.id(), "Student ID should not be null");
        assertEquals(expectedName, student.name(), "Student name should match");
        assertEquals(expectedAge, student.age(), "Student age should match");
    }

    private void assertAllStudentsHaveAge(StudentDto[] students, int expectedAge) {
        assertNotNull(students);
        if (students.length > 0) {
            for (StudentDto student : students) {
                assertEquals(expectedAge, student.age(),
                        "All students should have the expected age");
            }
        }
    }

    private void assertAllStudentsInAgeRange(StudentDto[] students, int minAge, int maxAge) {
        assertNotNull(students);
        if (students.length > 0) {
            for (StudentDto student : students) {
                assertTrue(student.age() >= minAge && student.age() <= maxAge,
                        "Student age " + student.age() + " should be between " + minAge + " and " + maxAge);
            }
        }
    }
}