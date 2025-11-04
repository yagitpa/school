package ru.hogwarts.school.dto;

public class StudentDto {
    private Long id;
    private String name;
    private int age;
    private Long facultyId;

    public StudentDto() {
    }

    public StudentDto(Long id, String name, int age, Long facultyId) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.facultyId = facultyId;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }
}