package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Homework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface HomeworkMapper {

    @Select("select * from homework")
    List<Homework> getAllHomework();

    @Select("select * from homework where id = #{id}")
    Homework getHomeworkById(Long id);

    @Select("select * from homework where course_id = #{courseId}")
    List<Homework> getHomeworkByCourseId(Long courseId);

    @Select("select h.id, h.course_id as courseId, h.title, h.deadline, t.name as teacherName " +
            "from homework h left join teacher t on h.teacher_id = t.sch_id " +
            "order by h.create_time desc LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getHomeworkWithTeacherPaginated(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from homework")
    int countAllHomework();

    @Select("select h.id, h.course_id as courseId, h.title, h.deadline, t.name as teacherName " +
            "from homework h left join teacher t on h.teacher_id = t.sch_id " +
            "where h.teacher_id = #{teacherId} " +
            "order by h.create_time desc LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getHomeworkByTeacherPaginated(@Param("teacherId") String teacherId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from homework where teacher_id = #{teacherId}")
    int countHomeworkByTeacherId(@Param("teacherId") String teacherId);

    @Select("select h.id, h.title, h.deadline, c.course_name as courseName, u.name as teacherName, hs.score as score, " +
            "case when hs.id is null then 'UNSUBMIT' " +
            "when hs.status = 'GRADED' then 'GRADED' " +
            "when h.deadline < now() then 'LATE' " +
            "else 'SUBMITTED' end as status " +
            "from homework h left join course c on h.course_id = c.id " +
            "left join user u on h.teacher_id = u.id " +
            "left join homework_submit hs on h.id = hs.homework_id and hs.sid = #{sid} " +
            "where h.course_id = #{courseId} " +
            "order by h.create_time desc LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getHomeworkByCourseIdWithDetailsPaginated(@Param("courseId") Long courseId, @Param("sid") String sid, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select h.id, h.title, h.content, h.deadline, h.attachments as attachments, " +
            "c.course_name as courseName, u.name as teacherName, " +
            "hs.score as score, hs.status as status " +
            "from homework h " +
            "left join course c on h.course_id = c.id " +
            "left join user u on h.teacher_id = u.id " +
            "left join homework_submit hs on h.id = hs.homework_id and hs.sid = #{sid} " +
            "where h.id = #{id}")
    Map<String, Object> getHomeworkContentWithDetails(@Param("id") Long id, @Param("sid") String sid);

    @Select("select count(*) from homework where course_id = #{courseId}")
    int countHomeworkByCourseId(@Param("courseId") Long courseId);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into homework(course_id, title, content, deadline, teacher_id, create_time) values(#{courseId}, #{title}, #{content}, #{deadline}, #{teacherId}, #{createTime})")
    int insertHomework(Homework homework);

    @Update("<script>" +
            "update homework " +
            "<set>" +
            "<if test='title != null'>title = #{title}, </if>" +
            "<if test='content != null'>content = #{content}, </if>" +
            "<if test='deadline != null'>deadline = #{deadline}, </if>" +
            "</set>" +
            "where id = #{id}" +
            "</script>")
    int updateHomeworkSelective(Homework homework);

    @Update("update homework set course_id = #{courseId}, title = #{title}, content = #{content}, deadline = #{deadline}, teacher_id = #{teacherId}, create_time = #{createTime} where id = #{id}")
    int updateHomework(Homework homework);

    @Update("update homework set attachments = #{attachments} where id = #{id}")
    int updateAttachments(@Param("id") Long id, @Param("attachments") String attachments);

    @Select("select attachments from homework where id = #{id}")
    String getAttachments(@Param("id") Long id);

    @Delete("delete from homework where id = #{id}")
    int deleteHomework(Long id);

    @Delete("delete from homework where course_id = #{courseId}")
    int deleteByCourseId(@Param("courseId") Long courseId);
}

