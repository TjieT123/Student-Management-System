package cn.edu.sdu.sms.server.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface CourseScoreMapper {

    @Select("select * from course_score where course_id = #{courseId} and sid = #{sid}")
    Map<String, Object> getOne(@Param("courseId") Long courseId, @Param("sid") String sid);

    @Insert("insert into course_score(course_id, sid, final_score, grade_point, update_time) " +
            "values(#{courseId}, #{sid}, #{finalScore}, #{gradePoint}, now()) " +
            "on duplicate key update final_score = #{finalScore}, grade_point = #{gradePoint}, update_time = now()")
    int upsertScore(@Param("courseId") Long courseId, @Param("sid") String sid,
                    @Param("finalScore") Double finalScore, @Param("gradePoint") Double gradePoint);

    @Select("select cs.*, c.course_name as courseName, c.type as courseType, c.credits as credits " +
            "from course_score cs join course c on cs.course_id = c.id where cs.sid = #{sid}")
    List<Map<String, Object>> getStudentAllScores(@Param("sid") String sid);

    @Select("select cs.*, s.name as studentName from course_score cs join student s on cs.sid = s.sid " +
            "where cs.course_id = #{courseId}")
    List<Map<String, Object>> getCourseAllScores(@Param("courseId") Long courseId);
}
