package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.CourseMapper;
import cn.edu.sdu.sms.server.models.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 获取所有课程（空则返回默认9门课）
     */
    public List<Course> getAllCourses() {
        List<Course> courses = courseMapper.getAllCourses();
        if (courses.isEmpty()) {
            return getPredefinedCourses();
        }
        return courses;
    }

    /**
     * 根据ID获取课程
     */
    public Course getCourseById(Long id) {
        return courseMapper.getCourseById(id);
    }

    /**
     * 固定9门课程
     */
    private List<Course> getPredefinedCourses() {
        List<Course> courses = new ArrayList<>();
        String[] courseNames = {"语文", "数学", "英语", "物理", "化学", "生物", "政治", "历史", "地理"};

        for (int i = 0; i < courseNames.length; i++) {
            Course course = new Course();
            course.setId((long) (i + 1));
            course.setCourseName(courseNames[i]);
            courses.add(course);
        }
        return courses;
    }
}