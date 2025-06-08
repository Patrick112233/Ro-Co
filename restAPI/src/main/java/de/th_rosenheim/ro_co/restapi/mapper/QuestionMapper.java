package de.th_rosenheim.ro_co.restapi.mapper;

import de.th_rosenheim.ro_co.restapi.dto.InQuestionDto;
import de.th_rosenheim.ro_co.restapi.dto.OutQuestionDto;
import de.th_rosenheim.ro_co.restapi.dto.OutUserDto;
import de.th_rosenheim.ro_co.restapi.dto.RegisterUserDto;
import de.th_rosenheim.ro_co.restapi.model.Question;
import de.th_rosenheim.ro_co.restapi.model.User;
import de.th_rosenheim.ro_co.restapi.repository.UserRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface QuestionMapper {

    QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class );

    @Mapping(target = "id", source = "q.id")
    @Mapping(target = "title", source = "q.title")
    @Mapping(target = "description", source = "q.description")
    @Mapping(target = "createdAt", source = "q.createdAt")
    @Mapping(target = "answered", source = "q.answered")
    @Mapping(target = "author", ignore = true)
    @Named("questionToOutQuestionDto")
    OutQuestionDto questionToOutQuestionDto(Question q);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", source = "q.title")
    @Mapping(target = "description", source = "q.description")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "answered", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "answers", ignore = true)

    @Named("inQuestionDtoToQuestion")
    Question inQuestionDtoToQuestion(InQuestionDto q);




}


