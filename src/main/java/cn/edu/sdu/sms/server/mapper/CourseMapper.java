package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("select * from course")
    List<Course> getAllCourses();

    @Select("select * from course where id = #{id}")
    Course getCourseById(Long id);

    @Insert("insert into course(course_name, id) values(#{courseName}, #{id})")
    int insertCourse(Course course);

    @Update("update course set course_name = #{courseName} where id = #{id}")
    int updateCourse(Course course);

    @Delete("delete from course where id = #{id}")
    int deleteCourse(Long id);
}

