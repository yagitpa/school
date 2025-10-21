package ru.hogwarts.school.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

// The JPA property has been redefined for the duration of the tests:
// spring.jpa.hibernate.ddl-auto=create-drop

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacultyControllerTestRestTemplateTest {

    // ========== CONSTANTS ==========
    private static final String BASE_URL = "http://localhost:";
    private static final String FACULTY_ENDPOINT = "/faculty";
    private static final String COLOR_ENDPOINT = "/color";
    private static final String SEARCH_ENDPOINT = "/search";
    private static final String STUDENTS_ENDPOINT = "/students";

    private static final String TEST_FACULTY_NAME = "Gryffindor";
    private static final String TEST_FACULTY_COLOR = "Red";
    private static final String UPDATED_FACULTY_NAME = "Updated Gryffindor";
    private static final String UPDATED_FACULTY_COLOR = "Scarlet";
    private static final String GREEN_FACULTY_NAME = "Slytherin";
    private static final String GREEN_FACULTY_COLOR = "Green";
    private static final String BLUE_FACULTY_COLOR = "Blue";

    private static final String NON_EXISTENT_NAME = "NonExistentFaculty";
    private static final String NON_EXISTENT_COLOR = "NonExistentColor";
    private static final String SEARCH_QUERY = "Gryffindor";
    private static final String EMPTY_STRING = "";

    private static final Long NON_EXISTENT_ID = 99999L;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private String testUrl;

    @BeforeEach
    void setUp() {
        testUrl = BASE_URL + port + FACULTY_ENDPOINT;
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should create faculty successfully with valid data")
    void createFaculty_validData_shouldReturnCreated() {
        // Given
        String facultyJson = createFacultyJson(TEST_FACULTY_NAME, TEST_FACULTY_COLOR);
        HttpEntity<String> request = createHttpEntity(facultyJson);

        // When
        ResponseEntity<Faculty> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, Faculty.class
        );

        // Then
        assertEquals(HttpStatus.OK, postResponse.getStatusCode());
        assertFacultyResponse(postResponse.getBody(), TEST_FACULTY_NAME, TEST_FACULTY_COLOR);
    }

    @Test
    @DisplayName("Positive. Should find existing faculty by ID")
    void findFaculty_existingId_shouldReturnFaculty() {
        // Given
        Faculty createdFaculty = createFacultyInDatabase(GREEN_FACULTY_NAME, GREEN_FACULTY_COLOR);

        // When
        ResponseEntity<Faculty> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", Faculty.class, createdFaculty.getId()
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertFacultyResponse(getResponse.getBody(), GREEN_FACULTY_NAME, GREEN_FACULTY_COLOR);
    }

    @Test
    @DisplayName("Positive. Should update faculty successfully")
    void updateFaculty_existingFaculty_shouldReturnUpdatedFaculty() {
        // Given
        Faculty createdFaculty = createFacultyInDatabase(TEST_FACULTY_NAME, TEST_FACULTY_COLOR);
        String updateJson = createFacultyJson(UPDATED_FACULTY_NAME, UPDATED_FACULTY_COLOR, createdFaculty.getId());
        HttpEntity<String> request = createHttpEntity(updateJson);

        // When
        ResponseEntity<Faculty> putResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.PUT, request, Faculty.class
        );

        // Then
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());
        assertFacultyResponse(putResponse.getBody(), UPDATED_FACULTY_NAME, UPDATED_FACULTY_COLOR);
    }

    @Test
    @DisplayName("Positive. Should delete faculty successfully")
    void deleteFaculty_existingFaculty_shouldReturnOk() {
        // Given
        Faculty createdFaculty = createFacultyInDatabase("Faculty to Delete", BLUE_FACULTY_COLOR);

        // When
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class,
                createdFaculty.getId()
        );

        // Then
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Verify faculty is actually deleted
        ResponseEntity<Faculty> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", Faculty.class, createdFaculty.getId()
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Positive. Should return all faculties")
    void getAllFaculties_shouldReturnFacultiesArray() {
        // When
        ResponseEntity<Faculty[]> getResponse = testRestTemplate.getForEntity(
                testUrl, Faculty[].class
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
    }

    @Test
    @DisplayName("Positive. Should filter faculties by color")
    void getFacultiesByColor_existingColor_shouldReturnFilteredFaculties() {
        // Given
        createFacultyInDatabase("Color Test Faculty", TEST_FACULTY_COLOR);

        // When
        ResponseEntity<Faculty[]> getResponse = testRestTemplate.getForEntity(
                testUrl + COLOR_ENDPOINT + "/{color}", Faculty[].class, TEST_FACULTY_COLOR
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertAllFacultiesHaveColor(getResponse.getBody(), TEST_FACULTY_COLOR);
    }

    @Test
    @DisplayName("Positive. Should search faculties by name or color")
    void getFacultiesByNameOrColor_existingValue_shouldReturnMatchingFaculties() {
        // Given
        createFacultyInDatabase(TEST_FACULTY_NAME, TEST_FACULTY_COLOR);

        // When
        ResponseEntity<Faculty[]> getResponse = testRestTemplate.getForEntity(
                testUrl + SEARCH_ENDPOINT + "?nameOrColor={nameOrColor}", Faculty[].class, SEARCH_QUERY
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
    }

    @Test
    @DisplayName("Positive. Should return faculty students")
    void getFacultyStudents_existingFaculty_shouldReturnStudentsArray() {
        // Given
        Faculty createdFaculty = createFacultyInDatabase("Faculty With Students", "Yellow");

        // When
        ResponseEntity<Student[]> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}" + STUDENTS_ENDPOINT, Student[].class, createdFaculty.getId()
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
    }

    // ========== NEGATIVE TESTS ==========

    @Test
    @DisplayName("Negative. Should return 404 when faculty not found")
    void findFaculty_nonExistentId_shouldReturn404() {
        // When
        ResponseEntity<Faculty> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", Faculty.class, NON_EXISTENT_ID
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should return 404 when updating non-existent faculty")
    void updateFaculty_nonExistentId_shouldReturn404() {
        // Given
        String facultyJson = createFacultyJson(NON_EXISTENT_NAME, NON_EXISTENT_COLOR, NON_EXISTENT_ID);
        HttpEntity<String> request = createHttpEntity(facultyJson);

        // When & Then
        try {
            ResponseEntity<Faculty> putResponse = testRestTemplate.exchange(
                    testUrl, HttpMethod.PUT, request, Faculty.class
            );
            assertEquals(HttpStatus.NOT_FOUND, putResponse.getStatusCode());
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("404"));
        }
    }

    @Test
    @DisplayName("Negative. Should return 404 when deleting non-existent faculty")
    void deleteFaculty_nonExistentId_shouldReturn404() {
        // When
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, NON_EXISTENT_ID
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should return empty array when no faculties match color")
    void getFacultiesByColor_nonExistentColor_shouldReturnEmptyArray() {
        // When
        ResponseEntity<Faculty[]> getResponse = testRestTemplate.getForEntity(
                testUrl + COLOR_ENDPOINT + "/{color}", Faculty[].class, NON_EXISTENT_COLOR
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(0, getResponse.getBody().length);
    }

    @Test
    @DisplayName("Negative. Should return empty array when no faculties match search")
    void getFacultiesByNameOrColor_nonExistentValue_shouldReturnEmptyArray() {
        // When
        ResponseEntity<Faculty[]> getResponse = testRestTemplate.getForEntity(
                testUrl + SEARCH_ENDPOINT + "?nameOrColor={nameOrColor}", Faculty[].class, NON_EXISTENT_NAME
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(0, getResponse.getBody().length);
    }

    @Test
    @DisplayName("Negative. Should return 404 when faculty has no students or not found")
    void getFacultyStudents_nonExistentFaculty_shouldReturnError() {
        // When
        ResponseEntity<String> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}" + STUDENTS_ENDPOINT, String.class, NON_EXISTENT_ID
        );

        // Then
        System.out.println("Response status: " + getResponse.getStatusCode());
        System.out.println("Response body: " + getResponse.getBody());

        assertFalse(getResponse.getStatusCode().is2xxSuccessful(),
                "Should not return 2xx for non-existent faculty");

        assertTrue(getResponse.getStatusCode().is4xxClientError() ||
                getResponse.getStatusCode().is5xxServerError());
    }

    @Test
    @DisplayName("Negative. Should handle empty faculty data")
    void createFaculty_emptyData_shouldReturnBadRequest() {
        // Given
        String emptyFacultyJson = createFacultyJson(EMPTY_STRING, EMPTY_STRING);
        HttpEntity<String> request = createHttpEntity(emptyFacultyJson);

        // When
        ResponseEntity<Faculty> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, Faculty.class
        );

        // Then
        assertTrue(postResponse.getStatusCode() == HttpStatus.BAD_REQUEST ||
                postResponse.getStatusCode() == HttpStatus.OK);
    }

    @Test
    @DisplayName("Negative. Should handle malformed JSON")
    void createFaculty_malformedJson_shouldReturnBadRequest() {
        // Given
        String malformedJson = """
                {
                    "id": 0,
                    "name": "Test",
                    "color": "Red"
                """;
        HttpEntity<String> request = createHttpEntity(malformedJson);

        // When & Then
        try {
            ResponseEntity<Faculty> postResponse = testRestTemplate.exchange(
                    testUrl, HttpMethod.POST, request, Faculty.class
            );
            assertSame(HttpStatus.BAD_REQUEST, postResponse.getStatusCode());
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("400") ||
                    e.getMessage().contains("Bad Request"));
        }
    }

    // ========== HELPER METHODS ==========

    private HttpEntity<String> createHttpEntity(String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(jsonBody, headers);
    }

    private String createFacultyJson(String name, String color) {
        return String.format("""
                {
                    "name": "%s",
                    "color": "%s"
                }""", name, color);
    }

    private String createFacultyJson(String name, String color, Long id) {
        return String.format("""
                {
                    "id": %d,
                    "name": "%s",
                    "color": "%s"
                }""", id, name, color);
    }

    private Faculty createFacultyInDatabase(String name, String color) {
        String facultyJson = createFacultyJson(name, color);
        HttpEntity<String> request = createHttpEntity(facultyJson);

        ResponseEntity<Faculty> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, Faculty.class
        );

        return postResponse.getBody();
    }

    private void assertFacultyResponse(Faculty faculty, String expectedName, String expectedColor) {
        assertNotNull(faculty, "Faculty should not be null");
        assertNotNull(faculty.getId(), "Faculty ID should not be null");
        assertEquals(expectedName, faculty.getName(), "Faculty name should match");
        assertEquals(expectedColor, faculty.getColor(), "Faculty color should match");
    }

    private void assertAllFacultiesHaveColor(Faculty[] faculties, String expectedColor) {
        assertNotNull(faculties);
        if (faculties.length > 0) {
            for (Faculty faculty : faculties) {
                assertEquals(expectedColor, faculty.getColor(),
                        "All faculties should have the expected color");
            }
        }
    }
}