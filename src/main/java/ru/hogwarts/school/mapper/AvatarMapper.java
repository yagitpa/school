package ru.hogwarts.school.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.hogwarts.school.dto.AvatarDataDto;
import ru.hogwarts.school.dto.AvatarInfoDto;
import ru.hogwarts.school.model.Avatar;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AvatarMapper {

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "data", ignore = true)
    Avatar toEntity(AvatarInfoDto avatarInfoDto);

    @Mapping(target = "studentId", source = "student.id")
    AvatarInfoDto toInfoDto(Avatar avatar);

    @Mapping(target = "data", source = "data")
    @Mapping(target = "mediaType", source = "mediaType")
    AvatarDataDto toDataDto(Avatar avatar);

    List<AvatarInfoDto> toInfoDtoList(List<Avatar> avatars);
}