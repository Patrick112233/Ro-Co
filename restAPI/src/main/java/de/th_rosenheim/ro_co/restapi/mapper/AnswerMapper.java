package de.th_rosenheim.ro_co.restapi.mapper;

import de.th_rosenheim.ro_co.restapi.dto.InAnswerDto;
import de.th_rosenheim.ro_co.restapi.dto.OutAnswerDto;
import de.th_rosenheim.ro_co.restapi.model.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    AnswerMapper INSTANCE = Mappers.getMapper(AnswerMapper.class );

    @Mapping(target = "id", source = "a.id")
    @Mapping(target = "description", source = "a.description")
    @Mapping(target = "createdAt", source = "a.createdAt")
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "questionID", ignore = true)
    @Named("answerToOutAnswerDto")
    OutAnswerDto answerToOutAnswerDto(Answer a);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "a.description")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "question", ignore = true)
    @Named("inAnswerDtoToAnswer")
    Answer inAnswerDtoToAnswer(InAnswerDto a);




}


