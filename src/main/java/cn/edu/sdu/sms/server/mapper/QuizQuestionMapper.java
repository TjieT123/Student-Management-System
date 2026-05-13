package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.QuizQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuizQuestionMapper {

    @Select("select * from quiz_question where quiz_id = #{quizId} order by question_order asc")
    List<QuizQuestion> getQuestionsByQuizId(Long quizId);

    @Select("select * from quiz_question where id = #{id}")
    QuizQuestion getQuestionById(Long id);

    @Insert("insert into quiz_question(quiz_id, question_text, question_type, option_a, option_b, option_c, option_d, correct_answer, points, question_order) " +
            "values(#{quizId}, #{questionText}, #{questionType}, #{optionA}, #{optionB}, #{optionC}, #{optionD}, #{correctAnswer}, #{points}, #{questionOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertQuestion(QuizQuestion question);

    @Update("update quiz_question set quiz_id = #{quizId}, question_text = #{questionText}, question_type = #{questionType}, " +
            "option_a = #{optionA}, option_b = #{optionB}, option_c = #{optionC}, option_d = #{optionD}, " +
            "correct_answer = #{correctAnswer}, points = #{points}, question_order = #{questionOrder} where id = #{id}")
    int updateQuestion(QuizQuestion question);

    @Delete("delete from quiz_question where id = #{id}")
    int deleteQuestion(Long id);

    @Delete("delete from quiz_question where quiz_id = #{quizId}")
    int deleteQuestionsByQuizId(Long quizId);
}

