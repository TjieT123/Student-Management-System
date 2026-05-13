package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.models.User;
import cn.edu.sdu.sms.server.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取所有用户
     */
    @GetMapping("/user/list")
    public ResponseEntity<Result> getUserList() {
        List<User> users = adminService.getUserList();
        return Result.success(users, "User list retrieved");
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
}