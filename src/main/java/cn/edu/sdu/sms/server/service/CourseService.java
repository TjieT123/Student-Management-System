package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.CourseMapper;
import cn.edu.sdu.sms.server.mapper.UserMapper;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

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
     * 分页获取课程列表（不含detail和teacherId，含teacherName），支持按id和courseName筛选
     */
    public Map<String, Object> getCoursesPaginated(int page, int pageSize, Long id, String courseName) {
        int offset = (page - 1) * pageSize;
        int total;
        List<Map<String, Object>> list;

        boolean hasFilter = id != null || (courseName != null && !courseName.isEmpty());

        if (hasFilter) {
            total = courseMapper.countCoursesFiltered(id, courseName);
            if (total == 0) {
                list = new ArrayList<>();
            } else {
                list = courseMapper.getCoursesWithTeacherFiltered(id, courseName, offset, pageSize);
            }
        } else {
            total = courseMapper.countCourses();
            if (total == 0) {
                List<Course> predefined = getPredefinedCourses();
                total = predefined.size();
                int toIndex = Math.min(offset + pageSize, total);
                list = new ArrayList<>();
                for (int i = offset; i < toIndex; i++) {
                    Course c = predefined.get(i);
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", c.getId());
                    item.put("courseName", c.getCourseName());
                    item.put("address", c.getAddress());
                    item.put("teacherName", null);
                    list.add(item);
                }
            } else {
                list = courseMapper.getCoursesWithTeacher(offset, pageSize);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    /**
     * 根据ID获取课程
     */
    public Course getCourseById(Long id) {
        return courseMapper.getCourseById(id);
    }

    /**
     * 修改课程（只更新传入的非空字段）
     */
    public Course updateCourse(Long id, String courseName, String detail, String address, String teacherId) {
        Course course = courseMapper.getCourseById(id);
        if (course == null) {
            return null;
        }
        if (courseName != null) {
            course.setCourseName(courseName);
        }
        if (detail != null) {
            course.setDetail(detail);
        }
        if (address != null) {
            course.setAddress(address);
        }
        if (teacherId != null) {
            course.setTeacherId(teacherId);
        }
        courseMapper.updateCourse(course);
        return course;
    }

    /**
     * 分页获取当前学生的选课列表（课程id、courseName、teacherName）
     */
    public Map<String, Object> getStudentCoursesPaginated(Long userId, int page, int pageSize) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String sid = user.getSchId();
        int offset = (page - 1) * pageSize;
        int total = courseMapper.countStudentCoursesBySid(sid);
        List<Map<String, Object>> list = courseMapper.getStudentCourses(sid, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    /**
     * 分页获取教师所教课程列表
     */
    public Map<String, Object> getTeacherCoursesPaginated(Long userId, int page, int pageSize) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String teacherId = user.getSchId();
        int offset = (page - 1) * pageSize;
        int total = courseMapper.countTeacherCourses(teacherId);
        List<Map<String, Object>> list = courseMapper.getTeacherCourses(teacherId, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    /**
     * 选课
     */
    public void enrollCourse(Long userId, Long courseId) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String sid = user.getSchId();
        if (courseMapper.countStudentCourse(sid, courseId) > 0) {
            throw new RuntimeException("Already enrolled in this course");
        }
        courseMapper.insertStudentCourse(sid, courseId);
    }

    /**
     * 取消选课
     */
    public void cancelCourse(Long userId, Long courseId) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String sid = user.getSchId();
        if (courseMapper.countStudentCourse(sid, courseId) == 0) {
            throw new RuntimeException("Not enrolled in this course");
        }
        courseMapper.deleteStudentCourse(sid, courseId);
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