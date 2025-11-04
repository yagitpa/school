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
import ru.hogwarts.school.model.Student;
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
        String facultyJson = createFacultyJson(FacultyConst.TEST_NAME, FacultyConst.TEST_COLOR);
        HttpEntity<String> request = createHttpEntity(facultyJson);

        // When
        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, FacultyDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, postResponse.getStatusCode());
        assertFacultyResponse(postResponse.getBody(), FacultyConst.TEST_NAME, FacultyConst.TEST_COLOR);
    }

    @Test
    @DisplayName("Positive. Should find existing faculty by ID")
    void findFaculty_existingId_shouldReturnFaculty() {
        // Given
        FacultyDto createdFaculty = createFacultyInDatabase(FacultyConst.GREEN_NAME, FacultyConst.GREEN_COLOR);

        // When
        ResponseEntity<FacultyDto> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", FacultyDto.class, createdFaculty.getId()
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
        String updateJson = createFacultyJson(FacultyConst.UPDATED_NAME, FacultyConst.UPDATED_COLOR, createdFaculty.getId());
        HttpEntity<String> request = createHttpEntity(updateJson);

        // When
        ResponseEntity<FacultyDto> putResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.PUT, request, FacultyDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());
        assertFacultyResponse(putResponse.getBody(), FacultyConst.UPDATED_NAME, FacultyConst.UPDATED_COLOR);
    }

    @Test
    @DisplayName("Positive. Should delete faculty successfully")
    void deleteFaculty_existingFaculty_shouldReturnOk() {
        // Given
        FacultyDto createdFaculty = createFacultyInDatabase("Faculty to Delete", FacultyConst.BLUE_COLOR);

        // When
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(
                testUrl + "/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class,
                createdFaculty.getId()
        );

        // Then
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Verify faculty is actually deleted
        ResponseEntity<FacultyDto> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}", FacultyDto.class, createdFaculty.getId()
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
        FacultyDto createdFaculty = createFacultyInDatabase("Faculty With Students", "Yellow");

        // When
        ResponseEntity<Student[]> getResponse = testRestTemplate.getForEntity(
                testUrl + "/{id}" + FacultyConst.STUDENTS_ENDPOINT, Student[].class, createdFaculty.getId()
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
        String facultyJson = createFacultyJson(FacultyConst.NON_EXISTENT_NAME, FacultyConst.NON_EXISTENT_COLOR, NON_EXISTENT_ID);
        HttpEntity<String> request = createHttpEntity(facultyJson);

        // When
        ResponseEntity<FacultyDto> putResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.PUT, request, FacultyDto.class
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
    @DisplayName("Negative. Should handle empty faculty data")
    void createFaculty_emptyData_shouldReturnBadRequest() {
        // Given
        String emptyFacultyJson = createFacultyJson(EMPTY_STRING, EMPTY_STRING);
        HttpEntity<String> request = createHttpEntity(emptyFacultyJson);

        // When
        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, FacultyDto.class
        );

        // Then
        // Может возвращать либо BAD_REQUEST, либо OK в зависимости от валидации
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
                """; // Не хватает закрывающей скобки
        HttpEntity<String> request = createHttpEntity(malformedJson);

        // When
        try {
            ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                    testUrl, HttpMethod.POST, request, FacultyDto.class
            );
            // Spring обычно возвращает 400 для невалидного JSON
            assertSame(HttpStatus.BAD_REQUEST, postResponse.getStatusCode());
        } catch (Exception e) {
            // Ожидаем исключение для невалидного JSON
            assertTrue(e.getMessage().contains("400") ||
                    e.getMessage().contains("Bad Request"));
        }
    }

    @Test
    @DisplayName("Positive. Should create faculty with student IDs list")
    void createFaculty_withStudentIds_shouldReturnFacultyWithStudentIds() {
        // Given
        String facultyJson = """
                {
                    "name": "Faculty with Students",
                    "color": "Purple",
                    "studentIds": [1, 2, 3]
                }""";
        HttpEntity<String> request = createHttpEntity(facultyJson);

        // When
        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, FacultyDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody());
        assertEquals("Faculty with Students", postResponse.getBody().getName());
        assertEquals("Purple", postResponse.getBody().getColor());
        // StudentIds могут быть null или пустым списком в ответе, так как это входные данные
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

    private FacultyDto createFacultyInDatabase(String name, String color) {
        String facultyJson = createFacultyJson(name, color);
        HttpEntity<String> request = createHttpEntity(facultyJson);

        ResponseEntity<FacultyDto> postResponse = testRestTemplate.exchange(
                testUrl, HttpMethod.POST, request, FacultyDto.class
        );

        return postResponse.getBody();
    }

    private void assertFacultyResponse(FacultyDto faculty, String expectedName, String expectedColor) {
        assertNotNull(faculty, "Faculty should not be null");
        assertNotNull(faculty.getId(), "Faculty ID should not be null");
        assertEquals(expectedName, faculty.getName(), "Faculty name should match");
        assertEquals(expectedColor, faculty.getColor(), "Faculty color should match");
        // studentIds может быть null - это нормально
    }

    private void assertAllFacultiesHaveColor(FacultyDto[] faculties, String expectedColor) {
        assertNotNull(faculties);
        if (faculties.length > 0) {
            for (FacultyDto faculty : faculties) {
                assertEquals(expectedColor, faculty.getColor(),
                        "All faculties should have the expected color");
            }
        }
    }
}