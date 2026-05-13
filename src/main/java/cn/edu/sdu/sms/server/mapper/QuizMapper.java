package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Quiz;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuizMapper {

    @Select("select * from quiz")
    List<Quiz> getAllQuizzes();

    @Select("select * from quiz where id = #{id}")
    Quiz getQuizById(Long id);

    @Select("select * from quiz where course_id = #{courseId}")
    List<Quiz> getQuizzesByCourseId(Long courseId);

    @Select("select * from quiz where teacher_id = #{teacherId}")
    List<Quiz> getQuizzesByTeacherId(String teacherId);

    @Insert("insert into quiz(course_id, title, description, duration, total_questions, teacher_id, start_time, end_time, create_time) " +
            "values(#{courseId}, #{title}, #{description}, #{duration}, #{totalQuestions}, #{teacherId}, #{startTime}, #{endTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertQuiz(Quiz quiz);

    @Update("update quiz set course_id = #{courseId}, title = #{title}, description = #{description}, " +
            "duration = #{duration}, total_questions = #{totalQuestions}, teacher_id = #{teacherId}, " +
            "start_time = #{startTime}, end_time = #{endTime}, create_time = #{createTime} where id = #{id}")
    int updateQuiz(Quiz quiz);

    @Delete("delete from quiz where id = #{id}")
    int deleteQuiz(Long id);
}

