package ru.hogwarts.school.testconfig;

public class TestConstants {

    // ========== COMMON CONSTANTS ==========
    public static final String BASE_URL = "http://localhost:";
    public static final String EMPTY_STRING = "";
    public static final Long NON_EXISTENT_ID = 99999L;

    // ========== FACULTY TEST CONSTANTS ==========
    public static class FacultyConst {
        public static final String ENDPOINT = "/faculty";
        public static final String COLOR_ENDPOINT = "/color";
        public static final String SEARCH_ENDPOINT = "/search";
        public static final String STUDENTS_ENDPOINT = "/students";

        public static final String TEST_NAME = "Gryffindor";
        public static final String TEST_COLOR = "Red";
        public static final String UPDATED_NAME = "Updated Gryffindor";
        public static final String UPDATED_COLOR = "Scarlet";
        public static final String GREEN_NAME = "Slytherin";
        public static final String GREEN_COLOR = "Green";
        public static final String BLUE_COLOR = "Blue";

        public static final String NON_EXISTENT_NAME = "NonExistentFaculty";
        public static final String NON_EXISTENT_COLOR = "NonExistentColor";
        public static final String SEARCH_QUERY = "Gryffindor";
    }

    // ========== STUDENT TEST CONSTANTS ==========
    public static class StudentConst {
        public static final String ENDPOINT = "/student";
        public static final String AGE_ENDPOINT = "/age";
        public static final String AGE_BETWEEN_ENDPOINT = "/age-between";
        public static final String FACULTY_ENDPOINT = "/faculty";

        public static final String TEST_NAME = "Harry Potter";
        public static final String UPDATED_NAME = "Harry Potter Updated";
        public static final String WITH_FACULTY_NAME = "Hermione Granger";
        public static final String NON_EXISTENT_NAME = "NonExistentStudent";

        public static final int TEST_AGE = 17;
        public static final int UPDATED_AGE = 18;
        public static final int AGE_FILTER_AGE = 16;
        public static final int MIN_AGE = 15;
        public static final int MAX_AGE = 20;
        public static final int YOUNG_AGE = 10;
        public static final int NON_EXISTENT_AGE = 999;
    }
}