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

    @Mapping(target = "questionCount", expression = "java(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)")
    QuizDTO toDTO(Quiz quiz);

    @Mapping(target = "questions", source = "questions")
    QuizDetailDTO toDetailDTO(Quiz quiz);

    @Mapping(target = "number", source = "id.questionNumber")
    QuizDetailDTO.QuestionDetailDTO toQuestionDetailDTO(Question question);

    QuizDetailDTO.HintDetailDTO toHintDetailDTO(Hint hint);
}
