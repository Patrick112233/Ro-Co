package de.th_rosenheim.ro_co.restapi.DTO;

import de.th_rosenheim.ro_co.restapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class );

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "firstName", source = "entity.firstName")
    @Mapping(target = "lastName", source = "entity.lastName")
    UserDTO userToUserDto(User entity);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "firstName", source = "dto.firstName")
    @Mapping(target = "lastName", source = "dto.lastName")
    User userDtotoUser(UserDTO dto);


    @Mapping(target = "id", source = "dto.id", ignore = true)
    @Mapping(target = "firstName", source = "dto.firstName")
    @Mapping(target = "lastName", source = "dto.lastName")
    User inUserDtotoUser(UserDTO dto);

}
