package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.models.Student;
import cn.edu.sdu.sms.server.models.Teacher;
import cn.edu.sdu.sms.server.models.User;
import cn.edu.sdu.sms.server.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员接口，提供用户、课程、教师、学生的管理功能（增删改查）。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 分页获取用户列表（教师）
     */
    @GetMapping("/user/teacher/list")
    public ResponseEntity<Result> getTeacherUserList(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = adminService.getUserListByRole("TEACHER", page, pageSize);
        return Result.success(result, "Teacher user list retrieved");
    }

    /**
     * 分页获取用户列表（学生）
     */
    @GetMapping("/user/student/list")
    public ResponseEntity<Result> getStudentUserList(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = adminService.getUserListByRole("STUDENT", page, pageSize);
        return Result.success(result, "Student user list retrieved");
    }

    /**
     * 分页获取用户列表（管理员）
     */
    @GetMapping("/user/admin/list")
    public ResponseEntity<Result> getAdminUserList(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = adminService.getUserListByRole("ADMIN", page, pageSize);
        return Result.success(result, "Admin user list retrieved");
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<Result> getUserById(@PathVariable Long id) {
        User user = adminService.getUserById(id);
        if (user == null) {
            return Result.error(404, "User not found");
        }
        return Result.success(user, "User retrieved");
    }

    /**
     * 添加用户
     */
    @PostMapping("/user/add")
    public ResponseEntity<Result> addUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String name = request.get("name");
        String role = request.get("role");
        String schId = request.get("sch_id");

        if (username == null || password == null || name == null || role == null || schId == null) {
            return Result.error(400, "Username, password, name, and role are required");
        }

        if (adminService.getUserByUsername(username) != null) {
            return Result.error(409, "Username already exists");
        }

        User user = adminService.addUser(request);
        return Result.success(user, "User added successfully");
    }

    /**
     * 修改用户
     */
    @PostMapping("/user/update")
    public ResponseEntity<Result> updateUser(@RequestBody Map<String, Object> request) {
        Object idObj = request.get("id");
        if (idObj == null || idObj.toString().trim().isEmpty()) {
            return Result.error(400, "User ID is required");
        }

        User user = adminService.updateUser(request);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        return Result.success(user, "User updated successfully");
    }

    /**
     * 删除用户
     */
    @PostMapping("/user/delete/{id}")
    public ResponseEntity<Result> deleteUser(@PathVariable Long id) {
        User user = adminService.getUserById(id);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        adminService.deleteUser(id);
        return Result.success(null, "User deleted successfully");
    }

    /**
     * 添加课程
     */
    @PostMapping("/course/add")
    public ResponseEntity<Result> addCourse(@RequestBody Map<String, Object> request) {
        String courseId = (String) request.get("courseId");
        if (courseId == null || courseId.trim().isEmpty()) {
            return Result.error(400, "Course ID is required");
        }

        if (adminService.getCourseById(Long.valueOf(courseId)) != null) {
            return Result.error(409, "Course ID already exists");
        }

        Course course = adminService.addCourse(request);
        if (course == null) {
            return Result.error(400, "Failed to add course");
        }
        return Result.success(course, "Course added successfully");
    }

    /**
     * 修改课程
     */
    @PostMapping("/course/update")
    public ResponseEntity<Result> updateCourse(@RequestBody Map<String, Object> request) {
        Object idObj = request.get("id");
        if (idObj == null || idObj.toString().trim().isEmpty()) {
            return Result.error(400, "Course ID is required");
        }

        Course course = adminService.updateCourse(request);
        if (course == null) {
            return Result.error(404, "Course not found");
        }

        return Result.success(course, "Course updated successfully");
    }

    /**
     * 删除课程
     */
    @PostMapping("/course/delete/{id}")
    public ResponseEntity<Result> deleteCourse(@PathVariable Long id) {
        Course course = adminService.getCourseById(id);
        if (course == null) {
            return Result.error(404, "Course not found");
        }

        adminService.deleteCourse(id);
        return Result.success(null, "Course deleted successfully");
    }

    /**
     * 获取所有教师
     */
    @GetMapping("/teacher/list")
    public ResponseEntity<Result> getTeacherList() {
        List<Teacher> teachers = adminService.getAllTeachers();
        return Result.success(teachers, "Teacher list retrieved");
    }

    /**
     * 添加教师
     */
    @PostMapping("/teacher/add")
    public ResponseEntity<Result> addTeacher(@RequestBody Map<String, String> request) {
        String schId = request.get("sch_id");
        String name = request.get("name");

        if (schId == null || schId.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            return Result.error(400, "sch_id and name are required");
        }

        if (adminService.getTeacherBySchId(schId) != null) {
            return Result.error(409, "Teacher already exists");
        }

        Teacher teacher = adminService.addTeacher(request);
        return Result.success(teacher, "Teacher added successfully");
    }

    /**
     * 修改教师
     */
    @PostMapping("/teacher/update")
    public ResponseEntity<Result> updateTeacher(@RequestBody Map<String, Object> request) {
        String schId = (String) request.get("sch_id");
        if (schId == null || schId.trim().isEmpty()) {
            return Result.error(400, "sch_id is required");
        }

        Teacher teacher = adminService.updateTeacher(request);
        if (teacher == null) {
            return Result.error(404, "Teacher not found");
        }

        return Result.success(teacher, "Teacher updated successfully");
    }

    /**
     * 删除教师
     */
    @PostMapping("/teacher/delete/{schId}")
    public ResponseEntity<Result> deleteTeacher(@PathVariable String schId) {
        Teacher teacher = adminService.getTeacherBySchId(schId);
        if (teacher == null) {
            return Result.error(404, "Teacher not found");
        }

        adminService.deleteTeacher(schId);
        return Result.success(null, "Teacher deleted successfully");
    }

    /**
     * 添加学生
     */
    @PostMapping("/student/add")
    public ResponseEntity<Result> addStudent(@RequestBody Map<String, String> request) {
        String sid = request.get("sid");
        String name = request.get("name");

        if (sid == null || sid.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            return Result.error(400, "sid and name are required");
        }

        if (adminService.getStudentBySid(sid) != null) {
            return Result.error(409, "Student already exists");
        }

        Student student = adminService.addStudent(request);
        return Result.success(student, "Student added successfully");
    }

    /**
     * 修改学生
     */
    @PostMapping("/student/update")
    public ResponseEntity<Result> updateStudent(@RequestBody Map<String, Object> request) {
        String sid = (String) request.get("sid");
        if (sid == null || sid.trim().isEmpty()) {
            return Result.error(400, "sid is required");
        }

        Student student = adminService.updateStudent(request);
        if (student == null) {
            return Result.error(404, "Student not found");
        }

        return Result.success(student, "Student updated successfully");
    }

    /**
     * 删除学生
     */
    @PostMapping("/student/delete/{sid}")
    public ResponseEntity<Result> deleteStudent(@PathVariable String sid) {
        Student student = adminService.getStudentBySid(sid);
        if (student == null) {
            return Result.error(404, "Student not found");
        }

        adminService.deleteStudent(sid);
        return Result.success(null, "Student deleted successfully");
    }

}