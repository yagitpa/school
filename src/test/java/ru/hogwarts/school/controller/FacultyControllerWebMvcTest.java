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
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
    private Student testStudent;

    @BeforeEach
    void setUp() {
        testFacultyDto = new FacultyDto();
        testFacultyDto.setId(EXISTING_ID);
        testFacultyDto.setName(FacultyConst.TEST_NAME);
        testFacultyDto.setColor(FacultyConst.TEST_COLOR);
        testFacultyDto.setStudentIds(Collections.emptyList());

        testStudent = new Student("Test Student", 17);
        testStudent.setId(1L);
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should create Faculty successfully")
    void createFaculty_validData_shouldReturnFaculty() throws Exception {
        // Given
        String facultyJson = createValidFacultyJson(FacultyConst.TEST_NAME, FacultyConst.TEST_COLOR);
        when(facultyService.createFaculty(any(FacultyDto.class))).thenReturn(testFacultyDto);

        // When & Then
        mockMvc.perform(post(FacultyConst.ENDPOINT)
                       .content(facultyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
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
        FacultyDto updatedFacultyDto = new FacultyDto();
        updatedFacultyDto.setId(EXISTING_ID);
        updatedFacultyDto.setName(FacultyConst.UPDATED_WEB_NAME);
        updatedFacultyDto.setColor(FacultyConst.UPDATED_WEB_COLOR);
        updatedFacultyDto.setStudentIds(List.of(1L, 2L));

        when(facultyService.updateFaculty(any(FacultyDto.class))).thenReturn(updatedFacultyDto);

        String updateFacultyJson = createValidFacultyJson(FacultyConst.UPDATED_WEB_NAME, FacultyConst.UPDATED_WEB_COLOR, EXISTING_ID);

        // When & Then
        mockMvc.perform(put(FacultyConst.ENDPOINT)
                       .content(updateFacultyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(FacultyConst.UPDATED_WEB_NAME))
               .andExpect(jsonPath("$.color").value(FacultyConst.UPDATED_WEB_COLOR))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Delete Faculty successfully")
    void deleteFaculty_existingFaculty_shouldReturnOk() throws Exception {
        // When & Then
        mockMvc.perform(delete(FacultyConst.ENDPOINT + "/{id}", EXISTING_ID))
               .andExpect(status().isOk())
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
        FacultyDto greenFacultyDto = new FacultyDto();
        greenFacultyDto.setId(2L);
        greenFacultyDto.setName(FacultyConst.GREEN_NAME);
        greenFacultyDto.setColor(FacultyConst.GREEN_COLOR);
        greenFacultyDto.setStudentIds(Collections.emptyList());

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
        List<Student> students = Collections.singletonList(testStudent);
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
    @DisplayName("Positive. Should create faculty with student IDs")
    void createFaculty_withStudentIds_shouldReturnFacultyWithStudentIds() throws Exception {
        // Given
        FacultyDto facultyWithStudentsDto = new FacultyDto();
        facultyWithStudentsDto.setId(EXISTING_ID);
        facultyWithStudentsDto.setName("Faculty with Students");
        facultyWithStudentsDto.setColor("Purple");
        facultyWithStudentsDto.setStudentIds(List.of(1L, 2L, 3L));

        when(facultyService.createFaculty(any(FacultyDto.class))).thenReturn(facultyWithStudentsDto);

        String facultyJson = """
                {
                    "name": "Faculty with Students",
                    "color": "Purple",
                    "studentIds": [1, 2, 3]
                }""";

        // When & Then
        mockMvc.perform(post(FacultyConst.ENDPOINT)
                       .content(facultyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Faculty with Students"))
               .andExpect(jsonPath("$.color").value("Purple"))
               .andExpect(jsonPath("$.studentIds").isArray())
               .andExpect(jsonPath("$.studentIds.length()").value(3))
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
        when(facultyService.updateFaculty(any(FacultyDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not Found"));

        String updateJson = createValidFacultyJson(FacultyConst.NON_EXISTENT_WEB_NAME, FacultyConst.NON_EXISTENT_COLOR, NON_EXISTENT_ID);

        // When & Then
        mockMvc.perform(put(FacultyConst.ENDPOINT)
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
    @DisplayName("Negative. Should handle empty faculty data gracefully")
    void createFaculty_emptyData_shouldHandleGracefully() throws Exception {
        // Given
        FacultyDto emptyFacultyDto = new FacultyDto();
        emptyFacultyDto.setId(EXISTING_ID);
        emptyFacultyDto.setName(EMPTY_STRING);
        emptyFacultyDto.setColor(EMPTY_STRING);
        emptyFacultyDto.setStudentIds(Collections.emptyList());

        when(facultyService.createFaculty(any(FacultyDto.class))).thenReturn(emptyFacultyDto);

        String emptyJson = createValidFacultyJson(EMPTY_STRING, EMPTY_STRING);

        // When & Then
        mockMvc.perform(post(FacultyConst.ENDPOINT)
                       .content(emptyJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(EMPTY_STRING))
               .andExpect(jsonPath("$.color").value(EMPTY_STRING))
               .andExpect(jsonPath("$.studentIds").isArray())
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