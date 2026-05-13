package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Homework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface HomeworkMapper {

    @Select("select * from homework")
    List<Homework> getAllHomework();

    @Select("select * from homework where id = #{id}")
    Homework getHomeworkById(Long id);

    @Select("select * from homework where course_id = #{courseId}")
    List<Homework> getHomeworkByCourseId(Long courseId);

    @Insert("insert into homework(course_id, title, content, deadline, teacher_id, create_time) values(#{courseId}, #{title}, #{content}, #{deadline}, #{teacherId}, #{createTime})")
    int insertHomework(Homework homework);

    @Update("update homework set course_id = #{courseId}, title = #{title}, content = #{content}, deadline = #{deadline}, teacher_id = #{teacherId}, create_time = #{createTime} where id = #{id}")
    int updateHomework(Homework homework);

    @Delete("delete from homework where id = #{id}")
    int deleteHomework(Long id);
}

