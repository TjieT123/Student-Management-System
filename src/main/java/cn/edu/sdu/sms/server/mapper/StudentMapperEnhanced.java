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

    @Select("select * from student order by sid LIMIT #{limit} OFFSET #{offset}")
    List<Student> getAllStudentsPaginated(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from student")
    int countStudents();

    @Insert("insert into student(sid, name, major, gender, s_class) values(#{sid}, #{name}, #{major}, #{gender}, #{sClass})")
    int insertStudent(Student student);

    @Update("update student set name = #{name}, major = #{major}, gender = #{gender}, s_class = #{sClass} where sid = #{sid}")
    int updateStudent(Student student);

    @Delete("delete from student where sid = #{sid}")
    int deleteStudent(String sid);
}

