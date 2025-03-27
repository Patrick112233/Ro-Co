package de.th_rosenheim.ro_co.restapi.DTO;

import de.th_rosenheim.ro_co.restapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class );

    UserDTO userToUserDto(User entity);

    User userDtotoUser(UserDTO dto);

}
