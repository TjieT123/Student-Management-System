package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.CourseMapper;
import cn.edu.sdu.sms.server.mapper.StudentMapperEnhanced;
import cn.edu.sdu.sms.server.mapper.TeacherMapper;
import cn.edu.sdu.sms.server.mapper.UserMapper;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.models.Student;
import cn.edu.sdu.sms.server.models.Teacher;
import cn.edu.sdu.sms.server.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapperEnhanced studentMapper;

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

    /**
     * 分页获取用户列表（按角色过滤，不含密码）
     */
    public Map<String, Object> getUserListByRole(String role, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        int total = userMapper.countByRole(role);
        List<Map<String, Object>> list;

        if ("STUDENT".equals(role)) {
            list = userMapper.getStudentUsersPaginated(offset, pageSize);
            total = userMapper.countStudentUsers();
        } else {
            list = userMapper.getUsersByRolePaginated(role, offset, pageSize);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
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
        String detail = (String) request.get("detail");
        String address = (String) request.get("address");
        String teacherId = (String) request.get("teacherId");

        Course course = new Course();
        course.setId(Long.parseLong(courseId));
        course.setCourseName(courseName);
        course.setDetail(detail);
        course.setAddress(address);
        course.setTeacherId(teacherId);
        courseMapper.insertCourse(course);
        return course;
    }

    // 修改课程
    public Course updateCourse(Map<String, Object> request) {
        Object idObj = request.get("id");
        Long id = Long.parseLong(idObj.toString().trim());

        String courseName = (String) request.get("courseName");
        String detail = (String) request.get("detail");
        String address = (String) request.get("address");
        String teacherId = (String) request.get("teacherId");

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

    // 删除课程
    public void deleteCourse(Long id) {
        courseMapper.deleteCourse(id);
    }

    // 分页获取指定教师的课程列表（供管理员使用）
    public Map<String, Object> getTeacherCoursesByTeacherId(String teacherId, int page, int pageSize) {
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

    // 获取全部教师
    public List<Teacher> getAllTeachers() {
        return teacherMapper.getAllTeachers();
    }

    // 根据schId获取教师
    public Teacher getTeacherBySchId(String schId) {
        return teacherMapper.getTeacherBySchId(schId);
    }

    // 添加教师
    public Teacher addTeacher(Map<String, String> request) {
        String schId = request.get("sch_id");
        String name = request.get("name");

        Teacher teacher = new Teacher();
        teacher.setSchId(schId);
        teacher.setName(name);
        teacherMapper.insertTeacher(teacher);
        return teacher;
    }

    // 修改教师
    public Teacher updateTeacher(Map<String, Object> request) {
        String schId = (String) request.get("sch_id");
        String name = (String) request.get("name");

        Teacher teacher = teacherMapper.getTeacherBySchId(schId);
        if (teacher == null) {
            return null;
        }

        if (name != null) {
            teacher.setName(name);
        }

        teacherMapper.updateTeacher(teacher);
        return teacher;
    }

    // 删除教师
    public void deleteTeacher(String schId) {
        teacherMapper.deleteTeacher(schId);
    }

    // 删除教师（级联删除关联的 user 记录）
    public void deleteTeacherCascade(String schId) {
        teacherMapper.deleteTeacher(schId);
        userMapper.deleteUserBySchId(schId);
    }

    // 获取全部学生
    public List<Student> getAllStudents() {
        return studentMapper.getAllStudents();
    }

    // 根据sid获取学生
    public Student getStudentBySid(String sid) {
        return studentMapper.getStudentBySid(sid);
    }

    // 添加学生
    public Student addStudent(Map<String, String> request) {
        String sid = request.get("sid");
        String name = request.get("name");
        String major = request.get("major");
        String gender = request.get("gender");
        String sClassStr = request.get("s_class");

        Student student = new Student();
        student.setSid(sid);
        student.setName(name);
        student.setMajor(major);
        student.setGender(gender);
        if (sClassStr != null && !sClassStr.trim().isEmpty()) {
            student.setSClass(Integer.parseInt(sClassStr.trim()));
        }
        studentMapper.insertStudent(student);
        return student;
    }

    // 修改学生
    public Student updateStudent(Map<String, Object> request) {
        String sid = (String) request.get("sid");
        String name = (String) request.get("name");
        String major = (String) request.get("major");
        String gender = (String) request.get("gender");
        Object sClassObj = request.get("s_class");

        Student student = studentMapper.getStudentBySid(sid);
        if (student == null) {
            return null;
        }

        if (name != null) {
            student.setName(name);
        }
        if (major != null) {
            student.setMajor(major);
        }
        if (gender != null) {
            student.setGender(gender);
        }
        if (sClassObj != null) {
            student.setSClass(Integer.parseInt(sClassObj.toString().trim()));
        }

        studentMapper.updateStudent(student);
        return student;
    }

    // 删除学生
    public void deleteStudent(String sid) {
        studentMapper.deleteStudent(sid);
    }

    // 删除学生（级联删除关联的 user 记录）
    public void deleteStudentCascade(String sid) {
        studentMapper.deleteStudent(sid);
        userMapper.deleteUserBySchId(sid);
    }

    // 原子操作：同时创建学生用户（user + student）
    public Map<String, Object> addStudentUser(Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String name = request.get("name");
        String phone = request.get("phone");
        String sid = request.get("sid");
        String major = request.get("major");
        String gender = request.get("gender");
        String sClassStr = request.get("s_class");

        // 创建 user 记录
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole("STUDENT");
        user.setPhone(phone);
        user.setSchId(sid);
        userMapper.insertUser(user);

        // 创建 student 记录
        Student student = new Student();
        student.setSid(sid);
        student.setName(name);
        student.setMajor(major);
        student.setGender(gender);
        if (sClassStr != null && !sClassStr.trim().isEmpty()) {
            student.setSClass(Integer.parseInt(sClassStr.trim()));
        }
        studentMapper.insertStudent(student);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("student", student);
        return result;
    }
}