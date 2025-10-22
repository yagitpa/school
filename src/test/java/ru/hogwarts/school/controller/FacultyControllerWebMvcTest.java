package ru.hogwarts.school.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FacultyController.class)
public class FacultyControllerWebMvcTest {

    // ========== CONSTANTS ==========

    private static final String BASE_URL = "/faculty";
    private static final String COLOR_ENDPOINT = "/color";
    private static final String SEARCH_ENDPOINT = "/search";
    private static final String STUDENTS_ENDPOINT = "/students";

    private static final String TEST_FACULTY_NAME = "Gryffindor";
    private static final String TEST_FACULTY_COLOR = "Red";
    private static final String UPDATED_FACULTY_NAME = "Gryffindor Updated";
    private static final String UPDATED_FACULTY_COLOR = "Blue";
    private static final String GREEN_FACULTY_NAME = "Slytherin";
    private static final String GREEN_FACULTY_COLOR = "Green";
    private static final String NON_EXISTENT_FACULTY_NAME = "NonExistentFacultyName";

    private static final Long EXISTING_ID = 1L;
    private static final Long NON_EXISTENT_ID = 999999L;
    private static final String NON_EXISTENT_COLOR = "NonExistentColor";
    private static final String SEARCH_QUERY = "Gryffindor";
    private static final String EMPTY_STRING = "";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacultyService facultyService;

    private Faculty testFaculty;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        testFaculty = new Faculty(TEST_FACULTY_NAME, TEST_FACULTY_COLOR);
        testFaculty.setId(EXISTING_ID);

        testStudent = new Student("Test Student", 17);
        testStudent.setId(1L);
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should create Faculty successfully")
    void createFaculty_validData_shouldReturnFaculty() throws Exception {
        // Given
        String facultyJson = createValidFacultyJson(TEST_FACULTY_NAME, TEST_FACULTY_COLOR);
        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(testFaculty);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                       .content(facultyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(TEST_FACULTY_NAME))
               .andExpect(jsonPath("$.color").value(TEST_FACULTY_COLOR))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should find existing Faculty by ID")
    void findFaculty_existingId_shouldReturnFaculty() throws Exception {
        // Given
        when(facultyService.findFaculty(EXISTING_ID)).thenReturn(testFaculty);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(TEST_FACULTY_NAME))
               .andExpect(jsonPath("$.color").value(TEST_FACULTY_COLOR))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Update Faculty successfully")
    void updateFaculty_existingFaculty_shouldReturnUpdatedFaculty() throws Exception {
        // Given
        Faculty updatedFaculty = new Faculty(UPDATED_FACULTY_NAME, UPDATED_FACULTY_COLOR);
        updatedFaculty.setId(EXISTING_ID);
        when(facultyService.updateFaculty(any(Faculty.class))).thenReturn(updatedFaculty);

        String updateFacultyJson = createValidFacultyJson(UPDATED_FACULTY_NAME, UPDATED_FACULTY_COLOR, EXISTING_ID);

        // When & Then
        mockMvc.perform(put(BASE_URL)
                       .content(updateFacultyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(UPDATED_FACULTY_NAME))
               .andExpect(jsonPath("$.color").value(UPDATED_FACULTY_COLOR))
               .andDo(print());
    }

    // FacultyService.deleteFaculty() returns nothing (void), but test requires the Faculty object to be return.
    // Therefore, a simplified version of deleteFaculty() test is used
    @Test
    @DisplayName("Positive. Should Delete Faculty successfully")
    void deleteFaculty_existingFaculty_shouldReturnOk() throws Exception {
        // When & Then
        mockMvc.perform(delete(BASE_URL + "/{id}", EXISTING_ID))
               .andExpect(status().isOk())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return all faculties")
    void getAllFaculties_shouldReturnFacultyList() throws Exception {
        // Given
        List<Faculty> faculties = Collections.singletonList(testFaculty);
        when(facultyService.getAllFaculties()).thenReturn(faculties);

        // When & Then
        mockMvc.perform(get(BASE_URL))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].name").value(TEST_FACULTY_NAME))
               .andExpect(jsonPath("$[0].color").value(TEST_FACULTY_COLOR))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return faculties filtered by Color")
    void getFacultiesByColor_existingColor_shoulrReturnFilteredFaculties() throws Exception {
        // Given
        Faculty greenFaculty = new Faculty(GREEN_FACULTY_NAME, GREEN_FACULTY_COLOR);
        greenFaculty.setId(2L);

        List<Faculty> faculties = Collections.singletonList(greenFaculty);
        when(facultyService.getFacultiesByColor(GREEN_FACULTY_COLOR)).thenReturn(faculties);

        // When & Then
        mockMvc.perform(get(BASE_URL + COLOR_ENDPOINT + "/{color}", GREEN_FACULTY_COLOR))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].color").value(GREEN_FACULTY_COLOR))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should search faculties by Name or Color")
    void getFacultiesByNameOrColor_existingValue_shouldReturnMatchingFaculties() throws Exception {
        // Given
        List<Faculty> faculties = Collections.singletonList(testFaculty);
        when(facultyService.getFacultiesByNameOrColor(SEARCH_QUERY)).thenReturn(faculties);

        // When & Then
        mockMvc.perform(get(BASE_URL + SEARCH_ENDPOINT)
                       .param("nameOrColor", SEARCH_QUERY))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].name").value(TEST_FACULTY_NAME))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return faculty students")
    void getFacultyStudents_existingFaculty_shouldReturnStudentsList() throws Exception {
        // Given
        List<Student> students = Collections.singletonList(testStudent);
        when(facultyService.getFacultyStudents(EXISTING_ID)).thenReturn(students);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}" + STUDENTS_ENDPOINT, EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].name").value("Test Student"))
               .andDo(print());
    }

    // ========== NEGATIVE TESTS ==========

    @Test
    @DisplayName("Negative. Should return 404 when Faculty not Found")
    void getFaculty_nonExistentId_shouldReturn404() throws Exception {
        // Given
        when(facultyService.findFaculty(NON_EXISTENT_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not Found"));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", NON_EXISTENT_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 404 when updating non-existent Faculty")
    void updateFaculty_nonExistentId_shouldReturn404() throws Exception {
        // Given
        when(facultyService.updateFaculty(any(Faculty.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not Found"));

        String updateJson = createValidFacultyJson(NON_EXISTENT_FACULTY_NAME, NON_EXISTENT_COLOR, NON_EXISTENT_ID);

        // When & Then
        mockMvc.perform(put(BASE_URL)
                       .content(updateJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 404 when deleting non-existent Faculty")
    void deleteFaculty_nonExistentId_shouldReturn404() throws Exception {
        // Given
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not Found"))
                .when(facultyService)
                .deleteFaculty(NON_EXISTENT_ID);

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/{id}", NON_EXISTENT_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should Return empty Array when no Faculties match Color")
    void getFacultyByColor_nonExistentColor_shouldReturnEmptyArray() throws Exception {
        // Given
        when(facultyService.getFacultiesByColor(NON_EXISTENT_COLOR)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get(BASE_URL + COLOR_ENDPOINT + "/{color}", NON_EXISTENT_COLOR))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(0))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should Return empty Array when no Faculties match Search")
    void getFacultyByNameOrColor_nonExistentValue_shouldReturnEmptyArray() throws Exception {
        // Given
        when(facultyService.getFacultiesByNameOrColor(NON_EXISTENT_COLOR)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get(BASE_URL + SEARCH_ENDPOINT)
                       .param("nameOrColor", NON_EXISTENT_COLOR))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(0))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 404 when Faculty has no students")
    void getFacultyStudents_nonExistentFaculty_shouldReturn404() throws Exception {
        // Given
        when(facultyService.getFacultyStudents(NON_EXISTENT_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not Found"));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{Id}" + STUDENTS_ENDPOINT, NON_EXISTENT_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should handle empty faculty data gracefully")
    void createFaculty_emptyData_shouldHandleGracefully() throws Exception {
        // Given
        Faculty emptyFaculty = new Faculty(EMPTY_STRING, EMPTY_STRING);
        emptyFaculty.setId(EXISTING_ID);
        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(emptyFaculty);

        String emptyJson = createValidFacultyJson(EMPTY_STRING, EMPTY_STRING);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                       .content(emptyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(EMPTY_STRING))
               .andExpect(jsonPath("$.color").value(EMPTY_STRING))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should handle malformed JSON")
    void createFaculty_malformedJson_shouldReturnBadRequest() throws Exception {
        // Given
        String malformedJson = createMalformedFacultyJson_missingBrace();

        // When & Then
        mockMvc.perform(post(BASE_URL)
                       .content(malformedJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should handle missing Search parameter")
    void getFacultiesByNameOrColor_missingParameter_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get(BASE_URL + SEARCH_ENDPOINT))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    // ========== HELPER METHODS ==========

    private String createValidFacultyJson(String name, String color) {
        return String.format("""
                {
                    "name": "%s",
                    "color": "%s"
                }""", name, color);
    }

    private String createValidFacultyJson(String name, String color, Long id) {
        return String.format("""
                {
                    "id": %d,
                    "name": "%s",
                    "color": "%s"
                }""", id, name, color);
    }

    private String createMalformedFacultyJson_missingBrace() {
        return """
            {
                "name": "Test Faculty",
                "color": "Red"
            """;
    }
}
