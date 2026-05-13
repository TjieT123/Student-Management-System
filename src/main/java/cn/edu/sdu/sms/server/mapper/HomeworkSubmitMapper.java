package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

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

    @Insert("insert into homework_submit(homework_id, sid, content, score, comment, status, submit_time) values(#{homeworkId}, #{sid}, #{content}, #{score}, #{comment}, #{status}, #{submitTime})")
    int insertSubmission(HomeworkSubmit submission);

    @Update("update homework_submit set homework_id = #{homeworkId}, sid = #{sid}, content = #{content}, score = #{score}, comment = #{comment}, status = #{status}, submit_time = #{submitTime} where id = #{id}")
    int updateSubmission(HomeworkSubmit submission);

    @Delete("delete from homework_submit where id = #{id}")
    int deleteSubmission(Long id);
}

