package com.ande.pubquizzz.mapper;

import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.LeaderboardEntry;
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
    @Mapping(target = "totalPoints", expression = "java(result.getAnswers().stream().mapToInt(com.ande.pubquizzz.database.entities.ResultAnswer::getPoints).sum())")
    ResultDTO toDTO(Result result);

    @Mapping(target = "questionNumber", source = "questionNumber")
    @Mapping(target = "points", source = "points")
    @Mapping(target = "changed", source = "changed")
    AnswerScoreDTO toAnswerScoreDTO(ResultAnswer resultAnswer);

    @Mapping(target = "rank", source = "rank")
    @Mapping(target = "teamName", source = "result.team.teamName")
    @Mapping(target = "teamId", source = "result.team.teamsId")
    @Mapping(target = "quizId", source = "result.quiz.quizId")
    @Mapping(target = "quizDate", expression = "java(result.getQuiz().getPubDate().toString())")
    @Mapping(target = "totalPoints", expression = "java(result.getAnswers().stream().mapToInt(com.ande.pubquizzz.database.entities.ResultAnswer::getPoints).sum())")
    LeaderboardEntry toLeaderboardEntry(Result result, int rank);
}
