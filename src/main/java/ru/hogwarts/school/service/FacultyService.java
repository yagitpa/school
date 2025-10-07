package ru.hogwarts.school.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Faculty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FacultyService {
    private final Map<Long, Faculty> faculties = new HashMap<>();
    private long counter = 0;

    public Faculty createFaculty(Faculty faculty) {
        faculty.setId(++counter);
        faculties.put(faculty.getId(), faculty);
        return faculty;
    }

    public Faculty findFaculty(long id) {
        Faculty faculty = faculties.get(id);
        if (faculty == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no faculty with ID " + id
            );
        }
        return faculty;
    }

    public Faculty updateFaculty(Faculty faculty) {
        if (!faculties.containsKey(faculty.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no faculty with ID " + faculty.getId()
            );
        }
        faculties.put(faculty.getId(), faculty);
        return faculty;
    }

    public Faculty deleteFaculty(long id) {
        if (!faculties.containsKey(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "There is no faculty with ID " + id
            );
        }
        return faculties.remove(id);
    }

    public List<Faculty> getFacultiesByColor(String color) {
        return faculties.values().stream()
                .filter(faculty -> faculty.getColor().equalsIgnoreCase(color))
                .collect(Collectors.toList());
    }

    public List<Faculty> getAllFaculties() {
        return List.copyOf(faculties.values());
    }
}
