package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherMapper {

    @Select("<script>" +
            "select * from teacher where 1=1 " +
            "<if test='schId != null and schId != \"\"'>and sch_id like concat('%', #{schId}, '%') </if>" +
            "<if test='name != null and name != \"\"'>and name like concat('%', #{name}, '%') </if>" +
            "</script>")
    List<Teacher> getAllTeachers(@Param("schId") String schId, @Param("name") String name);

    @Select("<script>" +
            "select sch_id as schId, name from teacher where 1=1 " +
            "<if test='schId != null and schId != \"\"'>and sch_id like concat('%', #{schId}, '%') </if>" +
            "<if test='name != null and name != \"\"'>and name like concat('%', #{name}, '%') </if>" +
            "order by sch_id LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Teacher> getTeachersPaginated(@Param("schId") String schId, @Param("name") String name, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "select count(*) from teacher where 1=1 " +
            "<if test='schId != null and schId != \"\"'>and sch_id like concat('%', #{schId}, '%') </if>" +
            "<if test='name != null and name != \"\"'>and name like concat('%', #{name}, '%') </if>" +
            "</script>")
    int countTeachers(@Param("schId") String schId, @Param("name") String name);

    @Select("select * from teacher where sch_id = #{schId}")
    Teacher getTeacherBySchId(String schId);

    @Insert("insert into teacher(sch_id, name) values(#{schId}, #{name})")
    int insertTeacher(Teacher teacher);

    @Update("update teacher set name = #{name} where sch_id = #{schId}")
    int updateTeacher(Teacher teacher);

    @Delete("delete from teacher where sch_id = #{schId}")
    int deleteTeacher(String schId);
}

