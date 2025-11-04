package ru.hogwarts.school.dto;

import java.util.List;

public class FacultyDto {
    private Long id;
    private String name;
    private String color;
    private List<Long> studentIds;

    public FacultyDto () {}

    public FacultyDto(Long id, String name, String color, List<Long> studentIds) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.studentIds = studentIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<Long> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<Long> studentIds) {
        this.studentIds = studentIds;
    }
}


