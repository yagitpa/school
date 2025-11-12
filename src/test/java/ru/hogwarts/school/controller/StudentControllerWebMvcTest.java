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
import ru.hogwarts.school.dto.StudentCreateDto;
import ru.hogwarts.school.dto.StudentDto;
import ru.hogwarts.school.dto.StudentUpdateDto;
import ru.hogwarts.school.service.StudentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
    private FacultyDto testFacultyDto;

    @BeforeEach
    void setUp() {
        testStudentDto = new StudentDto(EXISTING_ID, StudentConst.TEST_NAME, StudentConst.TEST_AGE, null);
        testFacultyDto = new FacultyDto(1L, "Gryffindor", "#FF0000", Collections.emptyList());
    }

    // ========== POSITIVE TESTS ==========

    @Test
    @DisplayName("Positive. Should Create Student successfully")
    void createStudent_validData_shouldReturnStudent() throws Exception {
        // Given
        String studentJson = createValidStudentCreateJson(StudentConst.TEST_NAME, StudentConst.TEST_AGE);
        when(studentService.createStudent(any(StudentCreateDto.class))).thenReturn(testStudentDto);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
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
        StudentDto studentWithFacultyDto = new StudentDto(EXISTING_ID, StudentConst.TEST_NAME, StudentConst.TEST_AGE, StudentConst.EXISTING_FACULTY_ID);
        String studentJson = String.format("""
            {
                "name": "%s",
                "age": %d,
                "facultyId": %d
            }""", StudentConst.TEST_NAME, StudentConst.TEST_AGE, StudentConst.EXISTING_FACULTY_ID);

        when(studentService.createStudent(any(StudentCreateDto.class))).thenReturn(studentWithFacultyDto);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(EXISTING_ID))
               .andExpect(jsonPath("$.name").value(StudentConst.TEST_NAME))
               .andExpect(jsonPath("$.age").value(StudentConst.TEST_AGE))
               .andExpect(jsonPath("$.facultyId").value(StudentConst.EXISTING_FACULTY_ID))
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
        StudentDto updatedStudentDto = new StudentDto(EXISTING_ID, StudentConst.UPDATED_NAME, StudentConst.UPDATED_AGE, 1L);
        when(studentService.updateStudent(anyLong(), any(StudentUpdateDto.class))).thenReturn(updatedStudentDto);

        String updateJson = createValidStudentUpdateJson(StudentConst.UPDATED_NAME, StudentConst.UPDATED_AGE);

        // When & Then
        mockMvc.perform(put(StudentConst.ENDPOINT + "/{id}", EXISTING_ID)
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
        StudentDto ageFilterStudentDto = new StudentDto(2L, "Age Filter Student", StudentConst.AGE_FILTER_AGE, null);
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
        StudentDto student1Dto = new StudentDto(2L, "Student 15 years old", 15, 1L);
        StudentDto student2Dto = new StudentDto(3L, "Student 18 years old", 18, null);
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
        when(studentService.getStudentFacultyDto(EXISTING_ID)).thenReturn(testFacultyDto);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/{id}" + StudentConst.FACULTY_ENDPOINT, EXISTING_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Gryffindor"))
               .andExpect(jsonPath("$.color").value("#FF0000"))
               .andExpect(jsonPath("$.studentIds").isArray())
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should print students in parallel mode successfully")
    void printStudentsParallel_sufficientStudents_shouldReturnOk() throws Exception {
        // Given
        List<String> studentNames = Arrays.asList(
                "Harry Potter", "Hermione Granger", "Ron Weasley",
                "Draco Malfoy", "Luna Lovegood", "Neville Longbottom"
        );
        when(studentService.getAllStudentNames()).thenReturn(studentNames);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/print-parallel"))
               .andExpect(status().isOk())
               .andExpect(content().string("Students printed in parallel mode"))
               .andDo(print());
    }

    @Test
    @DisplayName("Positive. Should print students in synchronized mode successfully")
    void printStudentsSynchronized_sufficientStudents_shouldReturnOk() throws Exception {
        // Given
        List<String> studentNames = Arrays.asList(
                "Harry Potter", "Hermione Granger", "Ron Weasley",
                "Draco Malfoy", "Luna Lovegood", "Neville Longbottom"
        );
        when(studentService.getAllStudentNames()).thenReturn(studentNames);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/print-synchronized"))
               .andExpect(status().isOk())
               .andExpect(content().string("Students printed in synchronized mode"))
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
        when(studentService.updateStudent(anyLong(), any(StudentUpdateDto.class))).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not Found")
        );

        String updateJson = createValidStudentUpdateJson(StudentConst.NON_EXISTENT_NAME, 20);

        // When & Then
        mockMvc.perform(put(StudentConst.ENDPOINT + "/{id}", NON_EXISTENT_ID)
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
        when(studentService.getStudentFacultyDto(EXISTING_ID)).thenThrow(
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
    @DisplayName("Negative. Should return 400 for empty Name in Student creation")
    void createdStudent_emptyName_shouldReturnBadRequest() throws Exception {
        // Given
        String studentJson = createValidStudentCreateJson(EMPTY_STRING, StudentConst.TEST_AGE);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 400 for invalid age in Student creation")
    void createStudent_invalidAge_shouldReturnBadRequest() throws Exception {
        // Given
        String studentJson = createValidStudentCreateJson("Test Student", Validation.TOO_YOUNG_AGE);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should handle non-existent faculty ID gracefully")
    void createStudent_nonExistentFacultyId_shouldHandle() throws Exception {
        // Given
        String studentJson = String.format("""
            {
                "name": "Student with Invalid Faculty",
                "age": %d,
                "facultyId": %d
            }""", StudentConst.TEST_AGE, StudentConst.NON_EXISTENT_FACULTY_ID);

        StudentDto studentDto = new StudentDto(EXISTING_ID, "Student with Invalid Faculty", StudentConst.TEST_AGE, StudentConst.NON_EXISTENT_FACULTY_ID);
        when(studentService.createStudent(any(StudentCreateDto.class))).thenReturn(studentDto);

        // When & Then
        mockMvc.perform(post(StudentConst.ENDPOINT)
                       .content(studentJson)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.name").value("Student with Invalid Faculty"))
               .andExpect(jsonPath("$.facultyId").value(StudentConst.NON_EXISTENT_FACULTY_ID))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 400 when insufficient students for parallel printing")
    void printStudentsParallel_insufficientStudents_shouldReturnBadRequest() throws Exception {
        // Given
        List<String> studentNames = Arrays.asList("Harry Potter", "Hermione Granger", "Ron Weasley");
        when(studentService.getAllStudentNames()).thenReturn(studentNames);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/print-parallel"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Insufficient students for operation. Required: 6, found: 3"))
               .andExpect(jsonPath("$.details[0]").value("INSUFFICIENT_STUDENTS"))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 400 when insufficient students for synchronized printing")
    void printStudentsSynchronized_insufficientStudents_shouldReturnBadRequest() throws Exception {
        // Given
        List<String> studentNames = Arrays.asList("Harry Potter", "Hermione Granger");
        when(studentService.getAllStudentNames()).thenReturn(studentNames);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/print-synchronized"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Insufficient students for operation. Required: 6, found: 2"))
               .andExpect(jsonPath("$.details[0]").value("INSUFFICIENT_STUDENTS"))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 400 when no students for parallel printing")
    void printStudentsParallel_noStudents_shouldReturnBadRequest() throws Exception {
        // Given
        List<String> studentNames = Collections.emptyList();
        when(studentService.getAllStudentNames()).thenReturn(studentNames);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/print-parallel"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Insufficient students for operation. Required: 6, found: 0"))
               .andExpect(jsonPath("$.details[0]").value("INSUFFICIENT_STUDENTS"))
               .andDo(print());
    }

    @Test
    @DisplayName("Negative. Should return 400 when no students for synchronized printing")
    void printStudentsSynchronized_noStudents_shouldReturnBadRequest() throws Exception {
        // Given
        List<String> studentNames = Collections.emptyList();
        when(studentService.getAllStudentNames()).thenReturn(studentNames);

        // When & Then
        mockMvc.perform(get(StudentConst.ENDPOINT + "/print-synchronized"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Insufficient students for operation. Required: 6, found: 0"))
               .andExpect(jsonPath("$.details[0]").value("INSUFFICIENT_STUDENTS"))
               .andDo(print());
    }

    // ========== HELPER METHODS ==========

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
}