package de.th_rosenheim.ro_co.restapi.dto;

import de.th_rosenheim.ro_co.restapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class );

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "username", source = "entity.displayName")
    @Mapping(target = "email", source = "entity.email")
    @Mapping(target = "role", source = "entity.role")
    @Mapping(target = "verified", source = "entity.verified")
    OutUserDto userToOutUserDto(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "displayName", source = "dto.username")
    @Mapping(target = "email", source = "dto.email")
    @Mapping(target = "role", source = "dto.role")
    @Mapping(target = "verified", source = "dto.verified")
    User outUserDTOtoUser(OutUserDto dto);


    @Mapping(target = "displayName", source = "dto.username")
    User inUserDtotoUser(InUserDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", source = "dto.username")
    @Mapping(target = "email", source = "dto.email")
    LoginResponseDto registerUserDtoToLoginResponse(RegisterUserDto dto);

}


