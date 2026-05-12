package com.ande.pubquizzz.mapper;

import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.ResultDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    @Mapping(target = "resultsId", source = "resultsId")
    @Mapping(target = "teamId", source = "team.teamsId")
    @Mapping(target = "teamName", source = "team.teamName")
    @Mapping(target = "quizId", source = "quiz.quizId")
    @Mapping(target = "quizDate", source = "quiz.pubDate")
    @Mapping(target = "totalPoints", expression = "java(result.calculateTotalPoints())")
    ResultDTO toDTO(Result result);

    @Mapping(target = "questionNumber", source = "questionNumber")
    @Mapping(target = "points", source = "points")
    AnswerScoreDTO toAnswerScoreDTO(ResultAnswer resultAnswer);
}
