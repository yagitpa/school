package ru.hogwarts.school.testconfig;

public class TestConstants {

    // ========== COMMON CONSTANTS ==========
    public static final String EMPTY_STRING = "";
    public static final Long EXISTING_ID = 1L;
    public static final String BASE_URL = "http://localhost:";
    public static final Long NON_EXISTENT_ID = 99999L;

    // ========== VALIDATION CONSTANTS ==========
    public static class Validation {
        public static final String VALID_HEX_COLOR = "#FF5733";
        public static final String TOO_SHORT_NAME = "Ab";
        public static final String INVALID_HEX_COLOR = "Red";

        public static final int TOO_YOUNG_AGE = 10;
    }

    // ========== FACULTY TEST CONSTANTS ==========
    public static class FacultyConst {
        public static final String ENDPOINT = "/faculty";
        public static final String COLOR_ENDPOINT = "/color";
        public static final String SEARCH_ENDPOINT = "/search";
        public static final String STUDENTS_ENDPOINT = "/students";

        public static final String TEST_NAME = "Gryffindor House";
        public static final String TEST_COLOR = "#B22222";
        public static final String GREEN_NAME = "Slytherin House";
        public static final String GREEN_COLOR = "#228B22";
        public static final String BLUE_COLOR = "#1E90FF";

        public static final String NON_EXISTENT_COLOR = "#FF00FF";
        public static final String SEARCH_QUERY = "Gryffindor";

        public static final String UPDATED_NAME = "Gryffindor House Updated";
        public static final String UPDATED_COLOR = "#0000FF";

        public static final String NON_EXISTENT_NAME = "Non-Existent Faculty";
    }

    // ========== STUDENT TEST CONSTANTS ==========
    public static class StudentConst {
        public static final String ENDPOINT = "/student";
        public static final String AGE_ENDPOINT = "/age";
        public static final String AGE_BETWEEN_ENDPOINT = "/age-between";
        public static final String FACULTY_ENDPOINT = "/faculty";

        public static final String TEST_NAME = "Harry James Potter";
        public static final String UPDATED_NAME = "Harry James Potter Updated";
        public static final String NON_EXISTENT_NAME = "Non-Existent Student";

        public static final int TEST_AGE = 17;
        public static final int UPDATED_AGE = 18;
        public static final int AGE_FILTER_AGE = 16;
        public static final int MIN_AGE = 15;
        public static final int MAX_AGE = 20;
        public static final int NON_EXISTENT_AGE = 999;

        public static final int INVALID_MIN_AGE = 25;
        public static final int INVALID_MAX_AGE = 10;

        public static final Long EXISTING_FACULTY_ID = 1L;
        public static final Long NON_EXISTENT_FACULTY_ID = 99999L;
    }
}