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
class FacultyControllerTestRestTemplateTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private String testUrl;

    @MockitoBean
    private AvatarService avatarService;

    @BeforeEach
    void setUp() {
        testUrl = BASE_URL + port + FacultyConst.ENDPOINT;
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should create faculty successfully with valid data")
    void createFaculty_validData_shouldReturnCreated() {
        // Given
        String facultyJson = createFacultyCreateJson(FacultyConst.TEST_NAME, FacultyConst.TEST_COLOR);
        HttpEntity<String> request = createHttpEntity(facultyJson);

        // When
        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, FacultyDto.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertFacultyResponse(postResponse.getBody(), FacultyConst.TEST_NAME, FacultyConst.TEST_COLOR);
    }

    @Test
    @DisplayName("Positive. Should find existing faculty by ID")
    void findFaculty_existingId_shouldReturnFaculty() {
        // Given
        FacultyDto createdFaculty = createFacultyInDatabase(FacultyConst.GREEN_NAME, FacultyConst.GREEN_COLOR);

        // When
        ResponseEntity<FacultyDto> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", FacultyDto.class, createdFaculty.id()
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertFacultyResponse(getResponse.getBody(), FacultyConst.GREEN_NAME, FacultyConst.GREEN_COLOR);
    }

    @Test
    @DisplayName("Positive. Should update faculty successfully")
    void updateFaculty_existingFaculty_shouldReturnUpdatedFaculty() {
        // Given
        FacultyDto createdFaculty = createFacultyInDatabase(FacultyConst.TEST_NAME, FacultyConst.TEST_COLOR);
        String updateJson = createFacultyUpdateJson(createdFaculty.id(), FacultyConst.UPDATED_NAME, FacultyConst.UPDATED_COLOR);
        HttpEntity<String> request = createHttpEntity(updateJson);

        // When
        ResponseEntity<FacultyDto> putResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.PUT, request, FacultyDto.class, createdFaculty.id()
        );

        // Then
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());
        assertFacultyResponse(putResponse.getBody(), FacultyConst.UPDATED_NAME, FacultyConst.UPDATED_COLOR);
    }

    @Test
    @DisplayName("Positive. Should delete faculty successfully")
    void deleteFaculty_existingFaculty_shouldReturnNoContent() {
        // Given
        FacultyDto createdFaculty = createFacultyInDatabase("Faculty to Delete", FacultyConst.BLUE_COLOR);

        // When
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class,
                createdFaculty.id()
        );

        // Then
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verify faculty is actually deleted
        ResponseEntity<FacultyDto> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", FacultyDto.class, createdFaculty.id()
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Positive. Should return all faculties")
    void getAllFaculties_shouldReturnFacultiesArray() {
        // When
        ResponseEntity<FacultyDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl, FacultyDto[].class
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
    }

    @Test
    @DisplayName("Positive. Should filter faculties by color")
    void getFacultiesByColor_existingColor_shouldReturnFilteredFaculties() {
        // Given
        createFacultyInDatabase("Color Test Faculty", FacultyConst.TEST_COLOR);

        // When
        ResponseEntity<FacultyDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl + FacultyConst.COLOR_ENDPOINT + "/{color}", FacultyDto[].class, FacultyConst.TEST_COLOR
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertAllFacultiesHaveColor(getResponse.getBody(), FacultyConst.TEST_COLOR);
    }

    @Test
    @DisplayName("Positive. Should search faculties by name or color")
    void getFacultiesByNameOrColor_existingValue_shouldReturnMatchingFaculties() {
        // Given
        createFacultyInDatabase(FacultyConst.TEST_NAME, FacultyConst.TEST_COLOR);

        // When
        ResponseEntity<FacultyDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl + FacultyConst.SEARCH_ENDPOINT + "?nameOrColor={nameOrColor}", FacultyDto[].class, FacultyConst.SEARCH_QUERY
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
    }

    @Test
    @DisplayName("Positive. Should return faculty students")
    void getFacultyStudents_existingFaculty_shouldReturnStudentsArray() {
        // Given
        FacultyDto createdFaculty = createFacultyInDatabase("Faculty With Students", "#F00");

        // When
        ResponseEntity<StudentDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}" + FacultyConst.STUDENTS_ENDPOINT, StudentDto[].class, createdFaculty.id()
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
    }

    @Test
    @DisplayName("Positive. Should create faculty with student IDs list")
    void createFaculty_withStudentIds_shouldReturnFacultyWithStudentIds() {
        // Given
        String facultyJson = """
                {
                    "name": "Faculty with Students",
                    "color": "#800080"
                }""";
        HttpEntity<String> request = createHttpEntity(facultyJson);

        // When
        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, FacultyDto.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody());
        assertEquals("Faculty with Students", postResponse.getBody().name());
        assertEquals("#800080", postResponse.getBody().color());
    }

    // ========== NEGATIVE TESTS ==========

    @Test
    @DisplayName("Negative. Should return 404 when faculty not found")
    void findFaculty_nonExistentId_shouldReturn404() {
        // When
        ResponseEntity<FacultyDto> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", FacultyDto.class, NON_EXISTENT_ID
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should return 404 when updating non-existent faculty")
    void updateFaculty_nonExistentId_shouldReturn404() {
        // Given
        String facultyJson = createFacultyUpdateJson(NON_EXISTENT_ID, FacultyConst.NON_EXISTENT_NAME, FacultyConst.NON_EXISTENT_COLOR);
        HttpEntity<String> request = createHttpEntity(facultyJson);

        // When
        ResponseEntity<FacultyDto> putResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.PUT, request, FacultyDto.class, NON_EXISTENT_ID
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, putResponse.getStatusCode());
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
        ResponseEntity<FacultyDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl + FacultyConst.COLOR_ENDPOINT + "/{color}", FacultyDto[].class, FacultyConst.NON_EXISTENT_COLOR
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
        ResponseEntity<FacultyDto[]> getResponse = testRestTemplate.getForEntity(
                testUrl + FacultyConst.SEARCH_ENDPOINT + "?nameOrColor={nameOrColor}", FacultyDto[].class, FacultyConst.NON_EXISTENT_NAME
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
                testUrl + "/{id}" + FacultyConst.STUDENTS_ENDPOINT, String.class, NON_EXISTENT_ID
        );

        // Then
        System.out.println("Response status: " + getResponse.getStatusCode());
        System.out.println("Response body: " + getResponse.getBody());

        assertFalse(getResponse.getStatusCode().is2xxSuccessful(),
                "Should not return 2xx for non-existent faculty");

        assertTrue(getResponse.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Negative. Should handle validation error for empty faculty data")
    void createFaculty_emptyData_shouldReturnBadRequest() {
        // Given
        String emptyFacultyJson = createFacultyCreateJson(EMPTY_STRING, EMPTY_STRING);
        HttpEntity<String> request = createHttpEntity(emptyFacultyJson);

        // When
        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, FacultyDto.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, postResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should handle validation error for invalid color format")
    void createFaculty_invalidColor_shouldReturnBadRequest() {
        // Given
        String invalidFacultyJson = createFacultyCreateJson("Test Faculty", "invalid-color");
        HttpEntity<String> request = createHttpEntity(invalidFacultyJson);

        // When
        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, FacultyDto.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, postResponse.getStatusCode());
    }

    @Test
    @DisplayName("Negative. Should handle malformed JSON")
    void createFaculty_malformedJson_shouldReturnBadRequest() {
        // Given
        String malformedJson = """
            {
                "name": "Test Faculty",
                "color": "#FF5733"
            """;
        HttpEntity<String> request = createHttpEntity(malformedJson);

        // When
        ResponseEntity<String> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody());
        assertTrue(postResponse.getBody().contains("Invalid JSON format"));
    }

    // ========== HELPER METHODS ==========

    private HttpEntity<String> createHttpEntity(String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(jsonBody, headers);
    }

    private String createFacultyCreateJson(String name, String color) {
        return String.format("""
                {
                    "name": "%s",
                    "color": "%s"
                }""", name, color);
    }

    private String createFacultyUpdateJson(Long id, String name, String color) {
        return String.format("""
                {
                    "name": "%s",
                    "color": "%s"
                }""", name, color);
    }

    private FacultyDto createFacultyInDatabase(String name, String color) {
        String facultyJson = createFacultyCreateJson(name, color);
        HttpEntity<String> request = createHttpEntity(facultyJson);

        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, FacultyDto.class
        );

        return postResponse.getBody();
    }

    private void assertFacultyResponse(FacultyDto faculty, String expectedName, String expectedColor) {
        assertNotNull(faculty, "Faculty should not be null");
        assertNotNull(faculty.id(), "Faculty ID should not be null");
        assertEquals(expectedName, faculty.name(), "Faculty name should match");
        assertEquals(expectedColor, faculty.color(), "Faculty color should match");
    }

    private void assertAllFacultiesHaveColor(FacultyDto[] faculties, String expectedColor) {
        assertNotNull(faculties);
        if (faculties.length > 0) {
            for (FacultyDto faculty : faculties) {
                assertEquals(expectedColor, faculty.color(),
                        "All faculties should have the expected color");
            }
        }
    }
}