package ru.hogwarts.school.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.hogwarts.school.dto.StudentCreateDto;
import ru.hogwarts.school.dto.StudentUpdateDto;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.dto.StudentDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "facultyId", source = "faculty.id")
    StudentDto toDto(Student student);

    @Mapping(target = "faculty", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    Student toEntity(StudentDto studentDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "faculty", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    Student toEntity(StudentCreateDto studentCreateDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "faculty", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    Student toEntity(StudentUpdateDto studentUpdateDto);

    List<StudentDto> toDtoList(List<Student> students);
}