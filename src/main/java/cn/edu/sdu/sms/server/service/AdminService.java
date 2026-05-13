package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.CourseMapper;
import cn.edu.sdu.sms.server.mapper.UserMapper;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CourseMapper courseMapper;

    // 获取全部用户
    public List<User> getUserList() {
        return userMapper.getAllUsers();
    }

    // 根据ID获取用户
    public User getUserById(Long id) {
        return userMapper.getUserById(id);
    }

    // 根据用户名查询用户（校验重复）
    public User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    // 根据id获取课程
    public Course getCourseById(Long id) {
        return courseMapper.getCourseById(id);
    }

    // 新增用户
    public User addUser(Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String name = request.get("name");
        String role = request.get("role");
        String phone = request.get("phone");
        String schId = request.get("sch_id");

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(role);
        user.setPhone(phone);
        user.setSchId(schId);

        userMapper.insertUser(user);
        return user;
    }

    // 修改用户
    public User updateUser(Map<String, Object> request) {
        Object idObj = request.get("id");
        Long id = Long.parseLong(idObj.toString().trim());

        String name = (String) request.get("name");
        String phone = (String) request.get("phone");
        String role = (String) request.get("role");

        User user = userMapper.getUserById(id);
        if (user == null) {
            return null;
        }

        if (name != null) {
            user.setName(name);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (role != null && !role.isBlank()) {
            if (role.equals("ADMIN") || role.equals("TEACHER") || role.equals("STUDENT")) {
                user.setRole(role);
            }
        }

        userMapper.updateUser(user);
        return user;
    }

    // 删除用户
    public void deleteUser(Long id) {
        userMapper.deleteUser(id);
    }

    // 添加课程
    public Course addCourse(Map<String, Object> request) {
        String courseId = (String) request.get("courseId");
        String courseName = (String) request.get("courseName");

        Course course = new Course();
        course.setId(Long.parseLong(courseId));
        course.setCourseName(courseName);
        courseMapper.insertCourse(course);
        return course;
    }
}