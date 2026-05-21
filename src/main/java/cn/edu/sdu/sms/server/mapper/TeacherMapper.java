package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface TeacherMapper {

    @Select("select * from teacher")
    List<Teacher> getAllTeachers();

    @Select("select * from teacher where sch_id = #{schId}")
    Teacher getTeacherBySchId(String schId);

    @Insert("insert into teacher(sch_id, name) values(#{schId}, #{name})")
    int insertTeacher(Teacher teacher);

    @Update("update teacher set name = #{name} where sch_id = #{schId}")
    int updateTeacher(Teacher teacher);

    @Delete("delete from teacher where sch_id = #{schId}")
    int deleteTeacher(String schId);
}

