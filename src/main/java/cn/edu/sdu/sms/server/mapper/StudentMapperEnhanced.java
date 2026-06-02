package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentMapperEnhanced {

    @Select("select * from student")
    List<Student> getAllStudents();

    @Select("select * from student where sid = #{sid}")
    Student getStudentBySid(String sid);

    @Select("<script>" +
            "select * from student where 1=1 " +
            "<if test='sid != null and sid != \"\"'>and sid like concat('%', #{sid}, '%') </if>" +
            "<if test='name != null and name != \"\"'>and name like concat('%', #{name}, '%') </if>" +
            "order by sid LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Student> getAllStudentsPaginated(@Param("sid") String sid, @Param("name") String name, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "select count(*) from student where 1=1 " +
            "<if test='sid != null and sid != \"\"'>and sid like concat('%', #{sid}, '%') </if>" +
            "<if test='name != null and name != \"\"'>and name like concat('%', #{name}, '%') </if>" +
            "</script>")
    int countStudents(@Param("sid") String sid, @Param("name") String name);

    @Insert("insert into student(sid, name, major, gender, s_class) values(#{sid}, #{name}, #{major}, #{gender}, #{sClass})")
    int insertStudent(Student student);

    @Update("update student set name = #{name}, major = #{major}, gender = #{gender}, s_class = #{sClass} where sid = #{sid}")
    int updateStudent(Student student);

    @Delete("delete from student where sid = #{sid}")
    int deleteStudent(String sid);
}

