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
            "<if test='teacherId != null and teacherId != \"\"'>and c.teacher_id = #{teacherId}</if>" +
            "</where>" +
            "order by c.id LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Map<String, Object>> getCoursesWithTeacherFiltered(@Param("id") Long id, @Param("courseName") String courseName, @Param("teacherId") String teacherId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "select count(*) from course c " +
            "<where>" +
            "<if test='id != null'>and c.id = #{id}</if>" +
            "<if test='courseName != null and courseName != \"\"'>and c.course_name like concat('%', #{courseName}, '%')</if>" +
            "<if test='teacherId != null and teacherId != \"\"'>and c.teacher_id = #{teacherId}</if>" +
            "</where>" +
            "</script>")
    int countCoursesFiltered(@Param("id") Long id, @Param("courseName") String courseName, @Param("teacherId") String teacherId);

    @Delete("delete from course where id = #{id}")
    int deleteCourse(Long id);

    @Select("select count(*) from student_course where sid = #{sid} and course_id = #{courseId}")
    int countStudentCourse(@Param("sid") String sid, @Param("courseId") Long courseId);

    @Select("select count(*) from student_course where course_id = #{courseId}")
    int countStudentsByCourseId(@Param("courseId") Long courseId);

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

    @Select("select c.id, c.course_name as courseName, c.detail as detail, c.address as address, t.name as teacherName " +
            "from course c left join teacher t on c.teacher_id = t.sch_id " +
            "where c.teacher_id = #{teacherId} " +
            "order by c.id LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getTeacherCourses(@Param("teacherId") String teacherId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from course where teacher_id = #{teacherId}")
    int countTeacherCourses(@Param("teacherId") String teacherId);

    @Select("select s.sid, s.name, s.major, s.gender, s.s_class as sClass " +
            "from student_course sc join student s on sc.sid = s.sid " +
            "where sc.course_id = #{courseId} order by s.sid")
    List<Map<String, Object>> getEnrolledStudents(@Param("courseId") Long courseId);

    // -- Schedule queries (Feature 2) --
    @Select("select c.id, c.course_name as courseName, c.type, c.start_week as startWeek, c.end_week as endWeek, c.schedule " +
            "from student_course sc join course c on sc.course_id = c.id " +
            "where sc.sid = #{sid} and c.start_week <= #{week} and c.end_week >= #{week}")
    List<Map<String, Object>> getStudentSchedule(@Param("sid") String sid, @Param("week") int week);

    @Select("select c.id, c.course_name as courseName, c.start_week as startWeek, c.end_week as endWeek, c.schedule " +
            "from course c where c.teacher_id = #{teacherId} and c.start_week <= #{week} and c.end_week >= #{week}")
    List<Map<String, Object>> getTeacherSchedule(@Param("teacherId") String teacherId, @Param("week") int week);

    @Select("select c.id, c.course_name as courseName, c.type, c.start_week as startWeek, " +
            "c.end_week as endWeek, c.schedule from student_course sc join course c on sc.course_id = c.id " +
            "where sc.sid = #{sid}")
    List<Map<String, Object>> getStudentEnrolledCoursesWithSchedule(@Param("sid") String sid);

    // -- Materials queries (Feature 3) --
    @Select("select materials from course where id = #{id}")
    String getMaterials(@Param("id") Long id);

    @Update("update course set materials = #{materials} where id = #{id}")
    int updateMaterials(@Param("id") Long id, @Param("materials") String materials);

    // -- Updated insert/update with new fields --
    @Insert("insert into course(id, course_name, detail, address, teacher_id, type, start_week, end_week, schedule, materials, credits) " +
            "values(#{id}, #{courseName}, #{detail}, #{address}, #{teacherId}, #{type}, #{startWeek}, #{endWeek}, #{schedule}, #{materials}, #{credits})")
    int insertCourse(Course course);

    @Update("update course set course_name = #{courseName}, detail = #{detail}, address = #{address}, teacher_id = #{teacherId}, " +
            "type = #{type}, start_week = #{startWeek}, end_week = #{endWeek}, schedule = #{schedule}, materials = #{materials}, credits = #{credits} where id = #{id}")
    int updateCourse(Course course);
}

