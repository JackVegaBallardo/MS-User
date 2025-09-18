package com.example.user_ms.mapper;


import com.example.user_ms.model.dto.CustomUserPrincipal;
import com.example.user_ms.model.dto.UserMeResponseDto;
import com.example.user_ms.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserMapper {

    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "userId", target = "id")
    @Mapping(target = "name", ignore = true)
    UserMeResponseDto toUserMeResponseDto(CustomUserPrincipal principal);

    @Mapping(source = "name", target = "name")
    void updateNameFromUser(User user, @MappingTarget UserMeResponseDto dto);

    List<UserMeResponseDto> toUserMeResponseDtoList(List<User> users);

}
