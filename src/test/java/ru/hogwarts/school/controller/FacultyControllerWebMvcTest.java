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
import ru.hogwarts.school.dto.FacultyCreateDto;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.dto.FacultyUpdateDto;
import ru.hogwarts.school.dto.StudentDto;
import ru.hogwarts.school.service.FacultyService;
import ru.hogwarts.school.service.UniversityManagementService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.hogwarts.school.testconfig.TestConstants.*;

@WebMvcTest(FacultyController.class)
public class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacultyService facultyService;

    private FacultyDto testFacultyDto;
    private StudentDto testStudentDto;

    @MockitoBean
    private UniversityManagementService universityManagementService;

    @BeforeEach
    void setUp() {
        testFacultyDto = new FacultyDto(EXISTING_ID, FacultyConst.TEST_NAME, FacultyConst.TEST_COLOR, Collections.emptyList());
        testStudentDto = new StudentDto(1L, "Test Student", 17, null);
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should create Faculty successfully")
    void createFaculty_validData_shouldReturnFaculty() throws Exception {
        // Given
        String facultyJson = createValidFacultyCreateJson(FacultyConst.TEST_NAME, FacultyConst.TEST_COLOR);
        when(facultyService.createFaculty(any(FacultyCreateDto.class))).thenReturn(testFacultyDto);

        // When & Then
        mockMvc.perform(post(FacultyConst.ENDPOINT)
                       .content(facultyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(FacultyConst.TEST_NAME))
               .andExpect(jsonPath("$.color").value(FacultyConst.TEST_COLOR))
               .andExpect(jsonPath("$.studentIds").isArray())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should find existing Faculty by ID")
    void findFaculty_existingId_shouldReturnFaculty() throws Exception {
        // Given
        when(facultyService.findFaculty(EXISTING_ID)).thenReturn(testFacultyDto);

        // When & Then
        mockMvc.perform(get(FacultyConst.ENDPOINT + "/{id}", EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(FacultyConst.TEST_NAME))
               .andExpect(jsonPath("$.color").value(FacultyConst.TEST_COLOR))
               .andExpect(jsonPath("$.studentIds").isArray())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Update Faculty successfully")
    void updateFaculty_existingFaculty_shouldReturnUpdatedFaculty() throws Exception {
        // Given
        FacultyDto updatedFacultyDto = new FacultyDto(EXISTING_ID, FacultyConst.UPDATED_NAME, FacultyConst.UPDATED_COLOR, List.of(1L, 2L));

        // Используем eq() для точного соответствия ID и any() для DTO
        when(facultyService.updateFaculty(eq(EXISTING_ID), any(FacultyUpdateDto.class))).thenReturn(updatedFacultyDto);

        String updateFacultyJson = createValidFacultyUpdateJson(FacultyConst.UPDATED_NAME, FacultyConst.UPDATED_COLOR);

        // When & Then
        mockMvc.perform(put(FacultyConst.ENDPOINT + "/{id}", EXISTING_ID)
                       .content(updateFacultyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(FacultyConst.UPDATED_NAME))
               .andExpect(jsonPath("$.color").value(FacultyConst.UPDATED_COLOR))
               .andExpect(jsonPath("$.studentIds").isArray())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Delete Faculty successfully")
    void deleteFaculty_existingFaculty_shouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete(FacultyConst.ENDPOINT + "/{id}", EXISTING_ID))
               .andExpect(status().isNoContent())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return all faculties")
    void getAllFaculties_shouldReturnFacultyList() throws Exception {
        // Given
        List<FacultyDto> faculties = Collections.singletonList(testFacultyDto);
        when(facultyService.getAllFaculties()).thenReturn(faculties);

        // When & Then
        mockMvc.perform(get(FacultyConst.ENDPOINT))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].name").value(FacultyConst.TEST_NAME))
               .andExpect(jsonPath("$[0].color").value(FacultyConst.TEST_COLOR))
               .andExpect(jsonPath("$[0].studentIds").isArray())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return faculties filtered by Color")
    void getFacultiesByColor_existingColor_shouldReturnFilteredFaculties() throws Exception {
        // Given
        FacultyDto greenFacultyDto = new FacultyDto(2L, FacultyConst.GREEN_NAME, FacultyConst.GREEN_COLOR, Collections.emptyList());
        List<FacultyDto> faculties = Collections.singletonList(greenFacultyDto);
        when(facultyService.getFacultiesByColor(FacultyConst.GREEN_COLOR)).thenReturn(faculties);

        // When & Then
        mockMvc.perform(get(FacultyConst.ENDPOINT + FacultyConst.COLOR_ENDPOINT + "/{color}", FacultyConst.GREEN_COLOR))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].color").value(FacultyConst.GREEN_COLOR))
               .andExpect(jsonPath("$[0].studentIds").isArray())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should search faculties by Name or Color")
    void getFacultiesByNameOrColor_existingValue_shouldReturnMatchingFaculties() throws Exception {
        // Given
        List<FacultyDto> faculties = Collections.singletonList(testFacultyDto);
        when(facultyService.getFacultiesByNameOrColor(FacultyConst.SEARCH_QUERY)).thenReturn(faculties);

        // When & Then
        mockMvc.perform(get(FacultyConst.ENDPOINT + FacultyConst.SEARCH_ENDPOINT)
                       .param("nameOrColor", FacultyConst.SEARCH_QUERY))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].name").value(FacultyConst.TEST_NAME))
               .andExpect(jsonPath("$[0].studentIds").isArray())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return faculty students")
    void getFacultyStudents_existingFaculty_shouldReturnStudentsList() throws Exception {
        // Given
        List<StudentDto> students = Collections.singletonList(testStudentDto);
        when(facultyService.getFacultyStudents(EXISTING_ID)).thenReturn(students);

        // When & Then
        mockMvc.perform(get(FacultyConst.ENDPOINT + "/{id}" + FacultyConst.STUDENTS_ENDPOINT, EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].name").value("Test Student"))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should create faculty with valid hex color")
    void createFaculty_withValidHexColor_shouldReturnFaculty() throws Exception {
        // Given
        FacultyDto facultyWithValidColorDto = new FacultyDto(EXISTING_ID, "Faculty with Valid Color", "#FF5733", Collections.emptyList());
        when(facultyService.createFaculty(any(FacultyCreateDto.class))).thenReturn(facultyWithValidColorDto);

        String facultyJson = """
                {
                    "name": "Faculty with Valid Color",
                    "color": "#FF5733"
                }""";

        // When & Then
        mockMvc.perform(post(FacultyConst.ENDPOINT)
                       .content(facultyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.name").value("Faculty with Valid Color"))
               .andExpect(jsonPath("$.color").value("#FF5733"))
               .andExpect(jsonPath("$.studentIds").isArray())
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
        mockMvc.perform(get(FacultyConst.ENDPOINT + "/{id}", NON_EXISTENT_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 404 when updating non-existent Faculty")
    void updateFaculty_nonExistentId_shouldReturn404() throws Exception {
        // Given
        when(facultyService.updateFaculty(eq(NON_EXISTENT_ID), any(FacultyUpdateDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not Found"));

        String updateJson = createValidFacultyUpdateJson(FacultyConst.NON_EXISTENT_NAME, FacultyConst.NON_EXISTENT_COLOR);

        // When & Then
        mockMvc.perform(put(FacultyConst.ENDPOINT + "/{id}", NON_EXISTENT_ID)
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
        mockMvc.perform(delete(FacultyConst.ENDPOINT + "/{id}", NON_EXISTENT_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should Return empty Array when no Faculties match Color")
    void getFacultyByColor_nonExistentColor_shouldReturnEmptyArray() throws Exception {
        // Given
        when(facultyService.getFacultiesByColor(FacultyConst.NON_EXISTENT_COLOR)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get(FacultyConst.ENDPOINT + FacultyConst.COLOR_ENDPOINT + "/{color}", FacultyConst.NON_EXISTENT_COLOR))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(0))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should Return empty Array when no Faculties match Search")
    void getFacultyByNameOrColor_nonExistentValue_shouldReturnEmptyArray() throws Exception {
        // Given
        when(facultyService.getFacultiesByNameOrColor(FacultyConst.NON_EXISTENT_COLOR)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get(FacultyConst.ENDPOINT + FacultyConst.SEARCH_ENDPOINT)
                       .param("nameOrColor", FacultyConst.NON_EXISTENT_COLOR))
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
        mockMvc.perform(get(FacultyConst.ENDPOINT + "/{Id}" + FacultyConst.STUDENTS_ENDPOINT, NON_EXISTENT_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 400 for invalid color format")
    void createFaculty_invalidColorFormat_shouldReturnBadRequest() throws Exception {
        // Given
        String invalidColorJson = createValidFacultyCreateJson("Test Faculty", Validation.INVALID_HEX_COLOR);

        // When & Then
        mockMvc.perform(post(FacultyConst.ENDPOINT)
                       .content(invalidColorJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 400 for empty name")
    void createFaculty_emptyName_shouldReturnBadRequest() throws Exception {
        // Given
        String emptyNameJson = createValidFacultyCreateJson("", "#FFFFFF");

        // When & Then
        mockMvc.perform(post(FacultyConst.ENDPOINT)
                       .content(emptyNameJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 400 for too short name")
    void createFaculty_tooShortName_shouldReturnBadRequest() throws Exception {
        // Given
        String shortNameJson = createValidFacultyCreateJson(Validation.TOO_SHORT_NAME, Validation.VALID_HEX_COLOR);

        // When & Then
        mockMvc.perform(post(FacultyConst.ENDPOINT)
                       .content(shortNameJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should handle malformed JSON")
    void createFaculty_malformedJson_shouldReturnBadRequest() throws Exception {
        // Given
        String malformedJson = createMalformedFacultyJson_missingBrace();

        // When & Then
        mockMvc.perform(post(FacultyConst.ENDPOINT)
                       .content(malformedJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should handle missing Search parameter")
    void getFacultiesByNameOrColor_missingParameter_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get(FacultyConst.ENDPOINT + FacultyConst.SEARCH_ENDPOINT))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    // ========== HELPER METHODS ==========

    private String createValidFacultyCreateJson(String name, String color) {
        return String.format("""
                {
                    "name": "%s",
                    "color": "%s"
                }""", name, color);
    }

    private String createValidFacultyUpdateJson(String name, String color) {
        return String.format("""
                {
                    "name": "%s",
                    "color": "%s"
                }""", name, color);
    }

    private String createMalformedFacultyJson_missingBrace() {
        return """
            {
                "name": "Test Faculty",
                "color": "Red"
            """;
    }
}