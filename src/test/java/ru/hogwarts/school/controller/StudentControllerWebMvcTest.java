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
import ru.hogwarts.school.dto.StudentDto;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.service.StudentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.hogwarts.school.testconfig.TestConstants.*;

@WebMvcTest(StudentController.class)
public class StudentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    private StudentDto testStudentDto;
    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        testStudentDto = new StudentDto();
        testStudentDto.setId(EXISTING_ID);
        testStudentDto.setName(StudentConst.TEST_NAME);
        testStudentDto.setAge(StudentConst.TEST_AGE);
        testStudentDto.setFacultyId(null);

        testFaculty = new Faculty("Gryffindor", "Red");
        testFaculty.setId(1L);
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should Create Student successfully")
    void createStudent_validData_shouldReturnStudent() throws Exception {
        // Given
        String studentJson = createValidStudentJson(StudentConst.TEST_NAME, StudentConst.TEST_AGE);
        when(studentService.createStudent(any(StudentDto.class))).thenReturn(testStudentDto);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(StudentConst.TEST_NAME))
               .andExpect(jsonPath("$.age").value(StudentConst.TEST_AGE))
               .andExpect(jsonPath("$.facultyId").isEmpty())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Create Student with faculty ID")
    void createStudent_withFacultyId_shouldReturnStudent() throws Exception {
        // Given
        StudentDto studentWithFacultyDto = new StudentDto();
        studentWithFacultyDto.setId(EXISTING_ID);
        studentWithFacultyDto.setName(StudentConst.TEST_NAME);
        studentWithFacultyDto.setAge(StudentConst.TEST_AGE);
        studentWithFacultyDto.setFacultyId(1L);

        String studentJson = """
                {
                    "name": "Harry Potter",
                    "age": 17,
                    "facultyId": 1
                }""";

        when(studentService.createStudent(any(StudentDto.class))).thenReturn(studentWithFacultyDto);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(StudentConst.TEST_NAME))
               .andExpect(jsonPath("$.age").value(StudentConst.TEST_AGE))
               .andExpect(jsonPath("$.facultyId").value(1L))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Find existing Student by ID")
    void findStudent_existingId_shouldReturnStudent() throws Exception {
        // Given
        when(studentService.findStudent(EXISTING_ID)).thenReturn(testStudentDto);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/{id}", EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(StudentConst.TEST_NAME))
               .andExpect(jsonPath("$.age").value(StudentConst.TEST_AGE))
               .andExpect(jsonPath("$.facultyId").isEmpty())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Update Student successfully")
    void updateStudent_existingStudent_shouldReturnUpdatedStudent() throws Exception {
        // Given
        StudentDto updatedStudentDto = new StudentDto();
        updatedStudentDto.setId(EXISTING_ID);
        updatedStudentDto.setName(StudentConst.UPDATED_NAME);
        updatedStudentDto.setAge(StudentConst.UPDATED_AGE);
        updatedStudentDto.setFacultyId(1L);

        when(studentService.updateStudent(any(StudentDto.class))).thenReturn(updatedStudentDto);

        String updateJson = createValidStudentJson(StudentConst.UPDATED_NAME, StudentConst.UPDATED_AGE, EXISTING_ID);

        // When & Then
        mockMvc.perform(put(StudentConst.ENDPOINT)
                       .content(updateJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(StudentConst.UPDATED_NAME))
               .andExpect(jsonPath("$.age").value(StudentConst.UPDATED_AGE))
               .andExpect(jsonPath("$.facultyId").value(1L))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Delete Student successfully")
    void deleteStudent_existingStudent_shouldReturnStudent() throws Exception {
        // Given
        when(studentService.deleteStudent(EXISTING_ID)).thenReturn(testStudentDto);

        // When & Then
        mockMvc.perform(delete(StudentConst.ENDPOINT + "/{id}", EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(StudentConst.TEST_NAME))
               .andExpect(jsonPath("$.age").value(StudentConst.TEST_AGE))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return all students")
    void getAllStudents_shouldReturnStudentsList() throws Exception {
        // Given
        List<StudentDto> students = Collections.singletonList(testStudentDto);
        when(studentService.getAllStudents()).thenReturn(students);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].id").value(EXISTING_ID))
               .andExpect(jsonPath("$[0].name").value(StudentConst.TEST_NAME))
               .andExpect(jsonPath("$[0].age").value(StudentConst.TEST_AGE))
               .andExpect(jsonPath("$[0].facultyId").isEmpty())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should filter students by Age")
    void getStudents_existingAge_shouldReturnFilteredStudents() throws Exception {
        // Given
        StudentDto ageFilterStudentDto = new StudentDto();
        ageFilterStudentDto.setId(2L);
        ageFilterStudentDto.setName("Age Filter Student");
        ageFilterStudentDto.setAge(StudentConst.AGE_FILTER_AGE);
        ageFilterStudentDto.setFacultyId(null);

        List<StudentDto> students = Collections.singletonList(ageFilterStudentDto);
        when(studentService.getStudentsByAge(StudentConst.AGE_FILTER_AGE)).thenReturn(students);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + StudentConst.AGE_ENDPOINT + "/{age}", StudentConst.AGE_FILTER_AGE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].age").value(StudentConst.AGE_FILTER_AGE))
               .andExpect(jsonPath("$[0].name").value("Age Filter Student"))
               .andExpect(jsonPath("$[0].facultyId").isEmpty())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return students by Age Between range")
    void getStudentsByAgeBetween_validRange_shouldReturnFilteredStudents() throws Exception {
        // Given
        StudentDto student1Dto = new StudentDto();
        student1Dto.setId(2L);
        student1Dto.setName("Student 15 years old");
        student1Dto.setAge(15);
        student1Dto.setFacultyId(1L);

        StudentDto student2Dto = new StudentDto();
        student2Dto.setId(3L);
        student2Dto.setName("Student 18 years old");
        student2Dto.setAge(18);
        student2Dto.setFacultyId(null);

        List<StudentDto> students = Arrays.asList(student1Dto, student2Dto);
        when(studentService.getStudentsByAgeBetween(StudentConst.MIN_AGE, StudentConst.MAX_AGE)).thenReturn(students);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + StudentConst.AGE_BETWEEN_ENDPOINT)
                       .param("minAge", String.valueOf(StudentConst.MIN_AGE))
                       .param("maxAge", String.valueOf(StudentConst.MAX_AGE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0].age").value(15))
               .andExpect(jsonPath("$[0].facultyId").value(1L))
               .andExpect(jsonPath("$[1].age").value(18))
               .andExpect(jsonPath("$[1].facultyId").isEmpty())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return student faculty")
    void getStudentFaculty_withFaculty_shouldReturnFaculty() throws Exception {
        // Given
        when(studentService.getStudentFaculty(EXISTING_ID)).thenReturn(testFaculty);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/{id}" + StudentConst.FACULTY_ENDPOINT, EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Gryffindor"))
               .andExpect(jsonPath("$.color").value("Red"))
               .andDo(print());
    }

    // ========== NEGATIVE TESTS ==========

    @Test
    @DisplayName("Negative. Should return 404 when Student not found")
    void findStudent_nonExistentId_shouldReturn404() throws Exception {
        // Given
        when(studentService.findStudent(NON_EXISTENT_ID)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not Found")
        );

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/{id}", NON_EXISTENT_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 404 when updating non-existent Student")
    void updateStudent_nonExistentId_shouldReturn404() throws Exception {
        // Given
        when(studentService.updateStudent(any(StudentDto.class))).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not Found")
        );

        String updateJson = createValidStudentJson(StudentConst.NON_EXISTENT_WEB_NAME, 20, NON_EXISTENT_ID);

        // When & Then
        mockMvc.perform(put(StudentConst.ENDPOINT)
                       .content(updateJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return empty List when no students match Age")
    void getStudentsByAge_nonExistentAge_shouldReturnEmptyList() throws Exception {
        // Given
        when(studentService.getStudentsByAge(StudentConst.NON_EXISTENT_AGE)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + StudentConst.AGE_ENDPOINT + "/{age}", StudentConst.NON_EXISTENT_AGE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(0))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 404 when Student has no Faculty")
    void getStudentFaculty_noFaculty_shouldReturn404() throws Exception {
        // Given
        when(studentService.getStudentFaculty(EXISTING_ID)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not Found")
        );

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/{id}" + StudentConst.FACULTY_ENDPOINT, EXISTING_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return empty List when no students in Age Between range or min > max")
    void getStudentsByAgeBetween_noStudents_shouldReturnEmptyList() throws Exception {
        // Given
        when(studentService.getStudentsByAgeBetween(StudentConst.INVALID_MIN_AGE, StudentConst.INVALID_MAX_AGE)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + StudentConst.AGE_BETWEEN_ENDPOINT)
                       .param("minAge", String.valueOf(StudentConst.INVALID_MIN_AGE))
                       .param("maxAge", String.valueOf(StudentConst.INVALID_MAX_AGE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(0))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should handle empty Name in Student creation")
    void createdStudent_emptyName_shouldHandle() throws Exception {
        // Given
        String studentJson = createValidStudentJson(EMPTY_STRING, StudentConst.TEST_AGE);

        StudentDto studentWithEmptyNameDto = new StudentDto();
        studentWithEmptyNameDto.setId(EXISTING_ID);
        studentWithEmptyNameDto.setName(EMPTY_STRING);
        studentWithEmptyNameDto.setAge(StudentConst.TEST_AGE);
        studentWithEmptyNameDto.setFacultyId(null);

        when(studentService.createStudent(any(StudentDto.class))).thenReturn(studentWithEmptyNameDto);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(EMPTY_STRING))
               .andExpect(jsonPath("$.facultyId").isEmpty())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should handle non-existent faculty ID")
    void createStudent_nonExistentFacultyId_shouldHandle() throws Exception {
        // Given
        String studentJson = """
                {
                    "name": "Student with Invalid Faculty",
                    "age": 17,
                    "facultyId": 999
                }""";

        StudentDto studentDto = new StudentDto();
        studentDto.setId(EXISTING_ID);
        studentDto.setName("Student with Invalid Faculty");
        studentDto.setAge(17);
        studentDto.setFacultyId(999L);

        when(studentService.createStudent(any(StudentDto.class))).thenReturn(studentDto);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Student with Invalid Faculty"))
               .andExpect(jsonPath("$.facultyId").value(999L))
               .andDo(print());
    }

    // ========== HELPER METHODS ==========

    private String createValidStudentJson(String name, int age) {
        return String.format("""
                {
                    "name": "%s",
                    "age": %d
                }""", name, age);
    }

    private String createValidStudentJson(String name, int age, Long id) {
        return String.format("""
                {
                    "id": %d,
                    "name": "%s",
                    "age": %d
                }""", id, name, age);
    }
}