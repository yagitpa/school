package ru.hogwarts.school.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.hogwarts.school.dto.FacultyCreateDto;
import ru.hogwarts.school.dto.FacultyDto;
import ru.hogwarts.school.dto.FacultyUpdateDto;
import ru.hogwarts.school.model.Faculty;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FacultyMapper {

    @Mapping(target = "studentIds", expression = "java(faculty.getStudents().stream().map(student -> student.getId())" +
            ".collect(java.util.stream.Collectors.toList()))")
    FacultyDto toDto(Faculty faculty);

    @Mapping(target = "students", ignore = true)
    Faculty toEntity(FacultyDto facultyDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "students", ignore = true)
    Faculty toEntity(FacultyCreateDto facultyCreateDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "students", ignore = true)
    Faculty toEntity(FacultyUpdateDto facultyUpdateDto);

    List<FacultyDto> toDtoList(List<Faculty> faculties);
}
