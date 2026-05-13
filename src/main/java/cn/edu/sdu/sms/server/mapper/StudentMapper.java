package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper {

    @Select("select * from student")
    public List<Student> getAllStudents();
}
