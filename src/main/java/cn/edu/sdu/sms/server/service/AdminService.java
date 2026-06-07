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
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private cn.edu.sdu.sms.server.mapper.HomeworkMapper homeworkMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.HomeworkSubmitMapper submitMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.HonorMapper honorMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.InnovationPracticeMapper practiceMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.LeaveRequestMapper leaveMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.ActivityMapper activityMapper;

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

    public User getUserBySchId(String schId) {
        return userMapper.getUserBySchId(schId);
    }

    /**
     * 分页获取用户列表（按角色过滤，不含密码），支持按schId和name搜索
     */
    public Map<String, Object> getUserListByRole(String role, int page, int pageSize, String schId, String name) {
        int offset = (page - 1) * pageSize;
        int total;
        List<Map<String, Object>> list;

        if ("STUDENT".equals(role)) {
            total = userMapper.countStudentUsers(schId, name);
            list = userMapper.getStudentUsersPaginated(schId, name, offset, pageSize);
        } else {
            total = userMapper.countByRole(role, schId, name);
            list = userMapper.getUsersByRolePaginated(role, schId, name, offset, pageSize);
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

    // 新增用户（自动创建关联的 teacher/student 记录）
    @Transactional
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

        if ("TEACHER".equals(role)) {
            if (teacherMapper.getTeacherBySchId(schId) == null) {
                Teacher teacher = new Teacher();
                teacher.setSchId(schId);
                teacher.setName(name);
                teacherMapper.insertTeacher(teacher);
            }
        } else if ("STUDENT".equals(role)) {
            if (studentMapper.getStudentBySid(schId) == null) {
                Student student = new Student();
                student.setSid(schId);
                student.setName(name);
                studentMapper.insertStudent(student);
            }
        }

        return user;
    }

    // 修改用户（同步更新关联的 teacher/student 记录）
    @Transactional
    public User updateUser(Map<String, Object> request) {
        Object idObj = request.get("id");
        Long id = Long.parseLong(idObj.toString().trim());

        String name = (String) request.get("name");
        String phone = (String) request.get("phone");
        String role = (String) request.get("role");
        String password = (String) request.get("password");

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
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userMapper.updateUser(user);

        // 同步更新关联表
        String schId = user.getSchId();
        if ("TEACHER".equals(user.getRole())) {
            Teacher teacher = teacherMapper.getTeacherBySchId(schId);
            if (teacher != null && name != null) {
                teacher.setName(name);
                teacherMapper.updateTeacher(teacher);
            }
        } else if ("STUDENT".equals(user.getRole())) {
            Student student = studentMapper.getStudentBySid(schId);
            if (student != null) {
                if (name != null) student.setName(name);
                String major = (String) request.get("major");
                String gender = (String) request.get("gender");
                Object sClassObj = request.get("s_class");
                if (major != null) student.setMajor(major);
                if (gender != null) student.setGender(gender);
                if (sClassObj != null) student.setSClass(Integer.parseInt(sClassObj.toString().trim()));
                // 扩展字段
                if (request.containsKey("birthDate") && request.get("birthDate") != null && !request.get("birthDate").toString().isEmpty()) {
                    try { student.setBirthDate(java.sql.Date.valueOf((String) request.get("birthDate"))); } catch (Exception ignored) {}
                }
                if (request.containsKey("enrollmentYear") && request.get("enrollmentYear") != null)
                    student.setEnrollmentYear(Integer.parseInt(request.get("enrollmentYear").toString().trim()));
                if (request.containsKey("idCard")) student.setIdCard((String) request.get("idCard"));
                if (request.containsKey("nativePlace")) student.setNativePlace((String) request.get("nativePlace"));
                if (request.containsKey("politicalStatus")) student.setPoliticalStatus((String) request.get("politicalStatus"));
                if (request.containsKey("address")) student.setAddress((String) request.get("address"));
                if (request.containsKey("contactName")) student.setContactName((String) request.get("contactName"));
                if (request.containsKey("contactPhone")) student.setContactPhone((String) request.get("contactPhone"));
                if (request.containsKey("socialRelations")) student.setSocialRelations((String) request.get("socialRelations"));
                if (request.containsKey("grade") && request.get("grade") != null)
                    student.setGrade(Integer.parseInt(request.get("grade").toString().trim()));
                studentMapper.updateStudent(student);
            }
        }

        return user;
    }

    // 删除用户（级联删除关联的 teacher/student 记录及相关数据）
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.getUserById(id);
        if (user != null) {
            String schId = user.getSchId();
            if ("TEACHER".equals(user.getRole())) {
                int courseCount = courseMapper.countTeacherCourses(schId);
                if (courseCount > 0) {
                    throw new RuntimeException("该教师有 " + courseCount + " 门正在教授的课程，无法删除。请先删除相关课程或更换任课教师");
                }
                teacherMapper.deleteTeacher(schId);
            } else if ("STUDENT".equals(user.getRole())) {
                submitMapper.deleteBySid(schId);
                courseMapper.deleteStudentCourseBySid(schId);
                honorMapper.deleteBySid(schId);
                practiceMapper.deleteBySid(schId);
                leaveMapper.deleteBySid(schId);
                activityMapper.cancelAllRegistrationsBySid(schId);
                studentMapper.deleteStudent(schId);
            }
        }
        userMapper.deleteUser(id);
    }

    // 添加课程
    public Course addCourse(Map<String, Object> request) {
        String courseName = (String) request.get("courseName");
        String detail = (String) request.get("detail");
        String address = (String) request.get("address");
        String teacherId = (String) request.get("teacherId");
        String type = (String) request.get("type");
        Object creditsObj = request.get("credits");

        if (courseName == null || courseName.trim().isEmpty()) {
            throw new RuntimeException("课程名不能为空");
        }
        if (teacherId == null || teacherId.trim().isEmpty()) {
            throw new RuntimeException("请选择任课教师");
        }

        Course course = new Course();
        // course ID is auto-generated by database
        course.setCourseName(courseName);
        course.setDetail(detail);
        course.setAddress(address);
        course.setTeacherId(teacherId);
        if (type != null) course.setType(type);
        if (creditsObj != null) course.setCredits(((Number) creditsObj).doubleValue());
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
        String type = (String) request.get("type");
        if (type != null) {
            course.setType(type);
        }
        Object creditsObj = request.get("credits");
        if (creditsObj != null) {
            course.setCredits(((Number) creditsObj).doubleValue());
        }

        courseMapper.updateCourse(course);
        return course;
    }

    // 删除课程（级联删除关联的作业、提交记录和选课记录）
    @Transactional
    public void deleteCourse(Long id) {
        // 删除该课程下所有作业的提交记录
        java.util.List<cn.edu.sdu.sms.server.models.Homework> homeworks = homeworkMapper.getHomeworkByCourseId(id);
        for (cn.edu.sdu.sms.server.models.Homework hw : homeworks) {
            submitMapper.deleteByHomeworkId(hw.getId());
        }
        // 删除该课程下的所有作业
        homeworkMapper.deleteByCourseId(id);
        // 删除该课程的所有选课记录
        courseMapper.deleteStudentCourseByCourseId(id);
        // 删除课程
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

    // 获取系统统计信息
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", userMapper.countTotalUsers());
        result.put("adminCount", userMapper.countByRole("ADMIN", null, null));
        result.put("teacherCount", userMapper.countByRole("TEACHER", null, null));
        result.put("studentCount", userMapper.countByRole("STUDENT", null, null));
        return result;
    }

    // 获取全部教师（支持按schId和name搜索）
    public List<Teacher> getAllTeachers(String schId, String name) {
        return teacherMapper.getAllTeachers(schId, name);
    }

    // 分页获取教师列表（支持按schId和name搜索）
    public Map<String, Object> getTeachersPaginated(int page, int pageSize, String schId, String name) {
        int offset = (page - 1) * pageSize;
        int total = teacherMapper.countTeachers(schId, name);
        List<Teacher> list = teacherMapper.getTeachersPaginated(schId, name, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
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
    @Transactional
    public void deleteTeacherCascade(String schId) {
        int courseCount = courseMapper.countTeacherCourses(schId);
        if (courseCount > 0) {
            throw new RuntimeException("该教师有 " + courseCount + " 门正在教授的课程，无法删除。请先删除相关课程或更换任课教师");
        }
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
        String gradeStr = request.get("grade");
        if (gradeStr != null && !gradeStr.trim().isEmpty()) {
            student.setGrade(Integer.parseInt(gradeStr.trim()));
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

        if (name != null) student.setName(name);
        if (major != null) student.setMajor(major);
        if (gender != null) student.setGender(gender);
        if (sClassObj != null) student.setSClass(Integer.parseInt(sClassObj.toString().trim()));

        // 扩展字段
        if (request.containsKey("birthDate") && request.get("birthDate") != null) {
            try { student.setBirthDate(java.text.SimpleDateFormat.getDateInstance().parse((String) request.get("birthDate"))); } catch (Exception ignored) {}
        }
        if (request.containsKey("enrollmentYear") && request.get("enrollmentYear") != null)
            student.setEnrollmentYear(Integer.parseInt(request.get("enrollmentYear").toString().trim()));
        if (request.containsKey("idCard")) student.setIdCard((String) request.get("idCard"));
        if (request.containsKey("nativePlace")) student.setNativePlace((String) request.get("nativePlace"));
        if (request.containsKey("politicalStatus")) student.setPoliticalStatus((String) request.get("politicalStatus"));
        if (request.containsKey("address")) student.setAddress((String) request.get("address"));
        if (request.containsKey("contactName")) student.setContactName((String) request.get("contactName"));
        if (request.containsKey("contactPhone")) student.setContactPhone((String) request.get("contactPhone"));
        if (request.containsKey("socialRelations")) student.setSocialRelations((String) request.get("socialRelations"));
        if (request.containsKey("grade") && request.get("grade") != null)
            student.setGrade(Integer.parseInt(request.get("grade").toString().trim()));

        studentMapper.updateStudent(student);
        return student;
    }

    // 删除学生
    public void deleteStudent(String sid) {
        studentMapper.deleteStudent(sid);
    }

    // 删除学生（级联删除关联的 user 记录及相关数据）
    @Transactional
    public void deleteStudentCascade(String sid) {
        submitMapper.deleteBySid(sid);
        courseMapper.deleteStudentCourseBySid(sid);
        honorMapper.deleteBySid(sid);
        practiceMapper.deleteBySid(sid);
        leaveMapper.deleteBySid(sid);
        activityMapper.cancelAllRegistrationsBySid(sid);
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
        String gradeStr = request.get("grade");
        if (gradeStr != null && !gradeStr.trim().isEmpty()) {
            student.setGrade(Integer.parseInt(gradeStr.trim()));
        }
        studentMapper.insertStudent(student);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("student", student);
        return result;
    }
}