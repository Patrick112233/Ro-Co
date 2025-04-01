package de.th_rosenheim.ro_co.restapi.dto;

import de.th_rosenheim.ro_co.restapi.ApplicationConfiguration;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.security.SecurityConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class );

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "firstName", source = "entity.firstName")
    @Mapping(target = "lastName", source = "entity.lastName")
    @Mapping(target = "email", source = "entity.email")
    @Mapping(target = "role", source = "entity.role")
    @Mapping(target = "verified", source = "entity.verified")
    UserDTO userToUserDto(User entity);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "firstName", source = "dto.firstName")
    @Mapping(target = "lastName", source = "dto.lastName")
    @Mapping(target = "email", source = "dto.email")
    @Mapping(target = "role", source = "dto.role")
    @Mapping(target = "verified", source = "dto.verified")
    User userDtotoUser(UserDTO dto);


    @Mapping(target = "id", source = "dto.id", ignore = true)
    @Mapping(target = "firstName", source = "dto.firstName")
    @Mapping(target = "lastName", source = "dto.lastName")
    @Mapping(target = "email", source = "dto.email")
    @Mapping(target = "role", source = "dto.role")
    @Mapping(target = "verified", source = "dto.verified")
    User inUserDtotoUser(UserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", source = "dto.firstName")
    @Mapping(target = "lastName", source = "dto.lastName")
    @Mapping(target = "email", source = "dto.email")
    LoginResponse registerUserDtoToLoginResponse(RegisterUserDto dto);



}


