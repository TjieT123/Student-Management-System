package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper {

    @Select("select * from course")
    List<Course> getAllCourses();

    @Select("select * from course where id = #{id}")
    Course getCourseById(Long id);

    @Select("select c.id, c.course_name as courseName, t.name as teacherName " +
            "from course c left join teacher t on c.teacher_id = t.sch_id " +
            "order by c.id LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getCoursesWithTeacher(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from course")
    int countCourses();

    @Insert("insert into course(course_name, id, detail, address, teacher_id) " +
            "values(#{courseName}, #{id}, #{detail}, #{address}, #{teacherId})")
    int insertCourse(Course course);

    @Update("update course set course_name = #{courseName}, detail = #{detail}, address = #{address}, teacher_id = #{teacherId} where id = #{id}")
    int updateCourse(Course course);

    @Delete("delete from course where id = #{id}")
    int deleteCourse(Long id);
}

