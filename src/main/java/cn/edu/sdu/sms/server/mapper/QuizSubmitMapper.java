package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.QuizSubmit;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuizSubmitMapper {

    @Select("select * from quiz_submit where id = #{id}")
    QuizSubmit getSubmitById(Long id);

    @Select("select * from quiz_submit where quiz_id = #{quizId}")
    List<QuizSubmit> getSubmitsByQuizId(Long quizId);

    @Select("select * from quiz_submit where quiz_id = #{quizId} and sid = #{sid}")
    QuizSubmit getSubmitByQuizIdAndSid(Long quizId, String sid);

    @Select("select * from quiz_submit where sid = #{sid}")
    List<QuizSubmit> getSubmitsBySid(String sid);

    @Insert("insert into quiz_submit(quiz_id, sid, answers, score, status, submit_time) " +
            "values(#{quizId}, #{sid}, #{answers}, #{score}, #{status}, #{submitTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSubmit(QuizSubmit submit);

    @Update("update quiz_submit set quiz_id = #{quizId}, sid = #{sid}, answers = #{answers}, " +
            "score = #{score}, status = #{status}, submit_time = #{submitTime} where id = #{id}")
    int updateSubmit(QuizSubmit submit);

    @Delete("delete from quiz_submit where id = #{id}")
    int deleteSubmit(Long id);

    @Delete("delete from quiz_submit where quiz_id = #{quizId}")
    int deleteSubmitsByQuizId(Long quizId);
}

