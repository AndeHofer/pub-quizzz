package com.ande.pubquizzz.mapper;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Question;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.dto.QuizDTO;
import com.ande.pubquizzz.dto.QuizDetailDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "finished", expression = "java(com.ande.pubquizzz.mapper.QuizFinishedChecker.isFinished(quiz))")
    QuizDTO toDTO(Quiz quiz);

    @Mapping(target = "questions", source = "questions")
    QuizDetailDTO toDetailDTO(Quiz quiz);

    @Mapping(target = "number", source = "id.questionNumber")
    QuizDetailDTO.QuestionDetailDTO toQuestionDetailDTO(Question question);

    QuizDetailDTO.HintDetailDTO toHintDetailDTO(Hint hint);
}
