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
import static ru.hogwarts.school.testconfig.TestConstants.*;

@WebMvcTest(StudentController.class)
public class StudentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    private Student testStudent;
    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        testStudent = new Student(StudentConst.TEST_NAME, StudentConst.TEST_AGE);
        testStudent.setId(EXISTING_ID);

        testFaculty = new Faculty("Gryffindor", "Red");
        testFaculty.setId(1L);
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should Create Student successfully")
    void createStudent_validData_shouldReturnStudent() throws Exception {
        // Given
        String studentJson = createValidStudentJson(StudentConst.TEST_NAME, StudentConst.TEST_AGE);
        when(studentService.createStudent(any(Student.class))).thenReturn(testStudent);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(StudentConst.TEST_NAME))
               .andExpect(jsonPath("$.age").value(StudentConst.TEST_AGE))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Find existing Student by ID")
    void findStudent_existingId_shouldReturnStudent() throws Exception {
        // Given
        when(studentService.findStudent(EXISTING_ID)).thenReturn(testStudent);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/{id}", EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(StudentConst.TEST_NAME))
               .andExpect(jsonPath("$.age").value(StudentConst.TEST_AGE))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Update Student successfully")
    void updateStudent_existingStudent_shouldReturnUpdatedStudent() throws Exception {
        // Given
        Student updatedStudent = new Student(StudentConst.UPDATED_NAME, StudentConst.UPDATED_AGE);
        updatedStudent.setId(EXISTING_ID);
        when(studentService.updateStudent(any(Student.class))).thenReturn(updatedStudent);

        String updateJson = createValidStudentJson(StudentConst.UPDATED_NAME, StudentConst.UPDATED_AGE, EXISTING_ID);

        // When & Then
        mockMvc.perform(put(StudentConst.ENDPOINT)
                       .content(updateJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(StudentConst.UPDATED_NAME))
               .andExpect(jsonPath("$.age").value(StudentConst.UPDATED_AGE))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should Delete Student successfully")
    void deleteStudent_existingStudent_shouldReturnStudent() throws Exception {
        // Given
        when(studentService.deleteStudent(EXISTING_ID)).thenReturn(testStudent);

        // When & Then
        mockMvc.perform(delete(StudentConst.ENDPOINT + "/{id}", EXISTING_ID))
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
        mockMvc.perform(get(StudentConst.ENDPOINT))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].id").value(EXISTING_ID))
               .andExpect(jsonPath("$[0].name").value(StudentConst.TEST_NAME))
               .andExpect(jsonPath("$[0].age").value(StudentConst.TEST_AGE))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should filter students by Age")
    void getStudents_existingAge_shouldReturnFilteredStudents() throws Exception {
        // Given
        Student ageFilterStudent = new Student("Age Filter Student", StudentConst.AGE_FILTER_AGE);
        ageFilterStudent.setId(2L);

        List<Student> students = Collections.singletonList(ageFilterStudent);
        when(studentService.getStudentsByAge(StudentConst.AGE_FILTER_AGE)).thenReturn(students);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + StudentConst.AGE_ENDPOINT + "/{age}", StudentConst.AGE_FILTER_AGE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].age").value(StudentConst.AGE_FILTER_AGE))
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
        when(studentService.getStudentsByAgeBetween(StudentConst.MIN_AGE, StudentConst.MAX_AGE)).thenReturn(students);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + StudentConst.AGE_BETWEEN_ENDPOINT)
                       .param("minAge", String.valueOf(StudentConst.MIN_AGE))
                       .param("maxAge", String.valueOf(StudentConst.MAX_AGE)))
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
        mockMvc.perform(get(StudentConst.ENDPOINT + "/{id}", NON_EXISTENT_ID))
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
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not Founf")
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

    // This test expects validation that is not currently implemented.
    // Currently, returns 200 OK as validation is disabled.
    // This test will fail when validation is enabled in the future
    @Test
    @DisplayName("Negative. Should handle empty Name in Student creation")
    void createdStudent_emptyName_shouldHandle() throws Exception {
        // Given
        String studentJson = createValidStudentJson(EMPTY_STRING, StudentConst.TEST_AGE);

        Student studentWithEmptyName = new Student(EMPTY_STRING, StudentConst.TEST_AGE);
        studentWithEmptyName.setId(EXISTING_ID);
        when(studentService.createStudent(any(Student.class))).thenReturn(studentWithEmptyName);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
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