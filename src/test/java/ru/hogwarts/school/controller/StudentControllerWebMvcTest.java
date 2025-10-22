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

@WebMvcTest(StudentController.class)
public class StudentControllerWebMvcTest {

    // ========== CONSTANTS ==========

    private static final String BASE_URL = "/student";
    private static final String AGE_ENDPOINT = "/age";
    private static final String AGE_BETWEEN = "/age-between";
    private static final String FACULTY_ENDPOINT = "/faculty";

    private static final String TEST_STUDENT_NAME = "Harry Potter";
    private static final String UPDATED_STUDENT_NAME = "Harry Potter Updated";
    private static final String NON_EXISTENT_STUDENT_NAME = "NonExistentStudent";

    private static final int TEST_STUDENT_AGE = 17;
    private static final int UPDATED_STUDENT_AGE = 18;
    private static final int AGE_FILTER_STUDENT_AGE = 16;
    private static final int MIN_AGE = 15;
    private static final int MAX_AGE = 20;
    private static final int INVALID_MAX_AGE = 10;
    private static final int INVALID_MIN_AGE = 25;

    private static final Long EXISTING_ID = 1L;
    private static final Long NON_EXISTENT_ID = 999999L;
    private static final int NON_EXISTENT_AGE = 999;
    private static final String EMPTY_STRING = "";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    private Student testStudent;
    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        testStudent = new Student(TEST_STUDENT_NAME, TEST_STUDENT_AGE);
        testStudent.setId(EXISTING_ID);

        testFaculty = new Faculty("Gryffindor", "Red");
        testFaculty.setId(1L);
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should Create Student successfully")
    void createStudent_validData_shouldReturnStudent() throws Exception {
        // Given
        String studentJson = createValidStudentJson(TEST_STUDENT_NAME, TEST_STUDENT_AGE);
        when(studentService.createStudent(any(Student.class))).thenReturn(testStudent);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(TEST_STUDENT_NAME))
               .andExpect(jsonPath("$.age").value(TEST_STUDENT_AGE))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Find existing Student by ID")
    void findStudent_existingId_shouldReturnStudent() throws Exception {
        // Given
        when(studentService.findStudent(EXISTING_ID)).thenReturn(testStudent);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(TEST_STUDENT_NAME))
               .andExpect(jsonPath("$.age").value(TEST_STUDENT_AGE))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Update Student successfully")
    void updateStudent_existingStudent_shouldReturnUpdatedStudent() throws Exception {
        // Given
        Student updatedStudent = new Student(UPDATED_STUDENT_NAME, UPDATED_STUDENT_AGE);
        updatedStudent.setId(EXISTING_ID);
        when(studentService.updateStudent(any(Student.class))).thenReturn(updatedStudent);

        String updateJson = createValidStudentJson(UPDATED_STUDENT_NAME, UPDATED_STUDENT_AGE, EXISTING_ID);

        // When & Then
        mockMvc.perform(put(BASE_URL)
                       .content(updateJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(UPDATED_STUDENT_NAME))
               .andExpect(jsonPath("$.age").value(UPDATED_STUDENT_AGE))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Delete Student successfully")
    void deleteStudent_existingStudent_shouldReturnStudent() throws Exception {
        // Given
        when(studentService.deleteStudent(EXISTING_ID)).thenReturn(testStudent);

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/{id}", EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return all students")
    void getAllStudents_shouldReturnStudentsList() throws Exception {
        // Given
        List<Student> students = Collections.singletonList(testStudent);
        when(studentService.getAllStudents()).thenReturn(students);

        // When & Then
        mockMvc.perform(get(BASE_URL))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].id").value(EXISTING_ID))
               .andExpect(jsonPath("$[0].name").value(TEST_STUDENT_NAME))
               .andExpect(jsonPath("$[0].age").value(TEST_STUDENT_AGE))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should filter students by Age")
    void getStudents_existingAge_shouldReturnFilteredStudents() throws Exception {
        // Given
        Student ageFilterStudent = new Student("Age Filter Student", AGE_FILTER_STUDENT_AGE);
        ageFilterStudent.setId(2L);

        List<Student> students = Collections.singletonList(ageFilterStudent);
        when(studentService.getStudentsByAge(AGE_FILTER_STUDENT_AGE)).thenReturn(students);

        // When & Then
        mockMvc.perform(get(BASE_URL + AGE_ENDPOINT + "/{age}", AGE_FILTER_STUDENT_AGE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].age").value(AGE_FILTER_STUDENT_AGE))
               .andExpect(jsonPath("$[0].name").value("Age Filter Student"))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should return students by Age Between range")
    void getStudentsByAgeBetween_validRange_shouldReturnFilteredStudents() throws Exception {
        // Given
        Student student1 = new Student("Student 15 years old", 15);
        student1.setId(2L);
        Student student2 = new Student("Student 18 years old", 18);
        student2.setId(3L);

        List<Student> students = Arrays.asList(student1, student2);
        when(studentService.getStudentsByAgeBetween(MIN_AGE, MAX_AGE)).thenReturn(students);

        // When & Then
        mockMvc.perform(get(BASE_URL + AGE_BETWEEN)
                       .param("minAge", String.valueOf(MIN_AGE))
                       .param("maxAge", String.valueOf(MAX_AGE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0].age").value(15))
               .andExpect(jsonPath("$[1].age").value(18))
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
        mockMvc.perform(get(BASE_URL + "/{id}", NON_EXISTENT_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 404 when updating non-existent Student")
    void updateStudent_nonExistentId_shouldReturn404() throws Exception {
        // Given
        when(studentService.updateStudent(any(Student.class))).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not Found")
        );

        String updateJson = createValidStudentJson(NON_EXISTENT_STUDENT_NAME, 20, NON_EXISTENT_ID);

        // When & Then
        mockMvc.perform(put(BASE_URL)
                       .content(updateJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return empty List when no students match Age")
    void getStudentsByAge_nonExistentAge_shouldReturnEmptyList() throws Exception {
        // Given
        when(studentService.getStudentsByAge(NON_EXISTENT_AGE)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get(BASE_URL + AGE_ENDPOINT + "/{age}", NON_EXISTENT_AGE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(0))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 404 when Student has no Faculty")
    void getStudentFaculty_noFaculty_shouldReturn404() throws Exception {
        // Given
        when(studentService.getStudentFaculty(EXISTING_ID)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not Founf")
        );

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}" + FACULTY_ENDPOINT, EXISTING_ID))
               .andExpect(status().isNotFound())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return empty List when no students in Age Between range or min > max")
    void getStudentsByAgeBetween_noStudents_shouldReturnEmptyList() throws Exception {
        // Given
        when(studentService.getStudentsByAgeBetween(INVALID_MIN_AGE, INVALID_MAX_AGE)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get(BASE_URL + AGE_BETWEEN)
                       .param("minAge", String.valueOf(INVALID_MIN_AGE))
                       .param("maxAge", String.valueOf(INVALID_MAX_AGE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(0))
               .andDo(print());
    }

    // This test expects validation that is not currently implemented.
    // Currently, returns 200 OK as validation is disabled.
    // This test will fail when validation is enabled in the future
    @Test
    @DisplayName("Negative. Should handle empty Name in Student creation")
    void createdStudent_emptyName_shouldHandle() throws Exception {
        // Given
        String studentJson = createValidStudentJson(EMPTY_STRING, TEST_STUDENT_AGE);

        Student studentWithEmptyName = new Student(EMPTY_STRING, TEST_STUDENT_AGE);
        studentWithEmptyName.setId(EXISTING_ID);
        when(studentService.createStudent(any(Student.class))).thenReturn(studentWithEmptyName);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(EMPTY_STRING))
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
