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

    @Select("select c.id, c.course_name as courseName, c.address as address, t.name as teacherName " +
            "from course c left join teacher t on c.teacher_id = t.sch_id " +
            "order by c.id LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getCoursesWithTeacher(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from course")
    int countCourses();

    @Select("<script>" +
            "select c.id, c.course_name as courseName, c.address as address, t.name as teacherName " +
            "from course c left join teacher t on c.teacher_id = t.sch_id " +
            "<where>" +
            "<if test='id != null'>and c.id = #{id}</if>" +
            "<if test='courseName != null and courseName != \"\"'>and c.course_name like concat('%', #{courseName}, '%')</if>" +
            "</where>" +
            "order by c.id LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Map<String, Object>> getCoursesWithTeacherFiltered(@Param("id") Long id, @Param("courseName") String courseName, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "select count(*) from course c " +
            "<where>" +
            "<if test='id != null'>and c.id = #{id}</if>" +
            "<if test='courseName != null and courseName != \"\"'>and c.course_name like concat('%', #{courseName}, '%')</if>" +
            "</where>" +
            "</script>")
    int countCoursesFiltered(@Param("id") Long id, @Param("courseName") String courseName);

    @Insert("insert into course(course_name, id, detail, address, teacher_id) " +
            "values(#{courseName}, #{id}, #{detail}, #{address}, #{teacherId})")
    int insertCourse(Course course);

    @Update("update course set course_name = #{courseName}, detail = #{detail}, address = #{address}, teacher_id = #{teacherId} where id = #{id}")
    int updateCourse(Course course);

    @Delete("delete from course where id = #{id}")
    int deleteCourse(Long id);

    @Select("select count(*) from student_course where sid = #{sid} and course_id = #{courseId}")
    int countStudentCourse(@Param("sid") String sid, @Param("courseId") Long courseId);

    @Insert("insert into student_course(sid, course_id) values(#{sid}, #{courseId})")
    int insertStudentCourse(@Param("sid") String sid, @Param("courseId") Long courseId);

    @Delete("delete from student_course where sid = #{sid} and course_id = #{courseId}")
    int deleteStudentCourse(@Param("sid") String sid, @Param("courseId") Long courseId);

    @Select("select c.id, c.course_name as courseName, c.address as address, t.name as teacherName " +
            "from student_course sc " +
            "join course c on sc.course_id = c.id " +
            "left join teacher t on c.teacher_id = t.sch_id " +
            "where sc.sid = #{sid} " +
            "order by c.id LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getStudentCourses(@Param("sid") String sid, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from student_course where sid = #{sid}")
    int countStudentCoursesBySid(@Param("sid") String sid);

    @Select("select c.id, c.course_name as courseName, c.address as address, t.name as teacherName " +
            "from course c left join teacher t on c.teacher_id = t.sch_id " +
            "where c.teacher_id = #{teacherId} " +
            "order by c.id LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getTeacherCourses(@Param("teacherId") String teacherId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from course where teacher_id = #{teacherId}")
    int countTeacherCourses(@Param("teacherId") String teacherId);
}

