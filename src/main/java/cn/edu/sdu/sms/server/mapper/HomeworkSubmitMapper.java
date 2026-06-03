package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface HomeworkSubmitMapper {

    @Select("select * from homework_submit")
    List<HomeworkSubmit> getAllSubmissions();

    @Select("select * from homework_submit where id = #{id}")
    HomeworkSubmit getSubmissionById(Long id);

    @Select("select * from homework_submit where homework_id = #{homeworkId}")
    List<HomeworkSubmit> getSubmissionsByHomeworkId(Long homeworkId);

    @Select("select * from homework_submit where sid = #{sid}")
    List<HomeworkSubmit> getSubmissionBySid(String sid);

    @Select("select * from homework_submit where homework_id = #{homeworkId} and sid = #{sid}")
    HomeworkSubmit getSubmissionByHomeworkIdAndSid(Long homeworkId, String sid);

    @Select("select hs.id, hs.homework_id as homeworkId, hs.score, hs.status, hs.submit_time as submitTime, s.name as studentName " +
            "from homework_submit hs left join student s on hs.sid = s.sid " +
            "where hs.homework_id = #{homeworkId} " +
            "order by hs.submit_time desc LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getSubmissionsWithStudent(@Param("homeworkId") Long homeworkId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from homework_submit where homework_id = #{homeworkId}")
    int countByHomeworkId(@Param("homeworkId") Long homeworkId);

    @Select("select score from homework_submit where homework_id = #{homeworkId}")
    List<Integer> getScoresByHomeworkId(@Param("homeworkId") Long homeworkId);

    @Insert("insert into homework_submit(homework_id, sid, content, score, comment, status, submit_time) values(#{homeworkId}, #{sid}, #{content}, #{score}, #{comment}, #{status}, #{submitTime})")
    int insertSubmission(HomeworkSubmit submission);

    @Update("update homework_submit set homework_id = #{homeworkId}, sid = #{sid}, content = #{content}, score = #{score}, comment = #{comment}, status = #{status}, submit_time = #{submitTime} where id = #{id}")
    int updateSubmission(HomeworkSubmit submission);

    @Delete("delete from homework_submit where id = #{id}")
    int deleteSubmission(Long id);

    @Delete("delete from homework_submit where homework_id = #{homeworkId}")
    int deleteByHomeworkId(@Param("homeworkId") Long homeworkId);
}

