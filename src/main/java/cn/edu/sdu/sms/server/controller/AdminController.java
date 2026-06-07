package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.models.Student;
import cn.edu.sdu.sms.server.models.Teacher;
import cn.edu.sdu.sms.server.models.User;
import cn.edu.sdu.sms.server.models.*;
import cn.edu.sdu.sms.server.service.*;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 管理员接口，提供用户、课程、教师、学生的管理功能（增删改查）。
 * 所有接口需要 ADMIN 角色的 Token 认证。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private HonorService honorService;

    @Autowired
    private InnovationPracticeService practiceService;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.LeaveRequestMapper leaveMapper;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.UserMapper userMapper;

    /**
     * 校验请求是否为管理员身份，返回 userId 字符串；非管理员返回 null。
     */
    private String requireAdmin(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return null;
        if (!jwtTokenProvider.validateToken(token)) return null;
        String role = jwtTokenProvider.getRoleFromToken(token);
        if (!"ADMIN".equals(role)) return null;
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    /**
     * 分页获取用户列表（教师）
     */
    @GetMapping("/user/teacher/list")
    public ResponseEntity<Result> getTeacherUserList(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                     @RequestParam(required = false) String schId,
                                                     @RequestParam(required = false) String name,
                                                     HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        Map<String, Object> result = adminService.getUserListByRole("TEACHER", page, pageSize, schId, name);
        return Result.success(result, "Teacher user list retrieved");
    }

    /**
     * 分页获取用户列表（学生）
     */
    @GetMapping("/user/student/list")
    public ResponseEntity<Result> getStudentUserList(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                     @RequestParam(required = false) String schId,
                                                     @RequestParam(required = false) String name,
                                                     HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        Map<String, Object> result = adminService.getUserListByRole("STUDENT", page, pageSize, schId, name);
        return Result.success(result, "Student user list retrieved");
    }

    /**
     * 分页获取用户列表（管理员）
     */
    @GetMapping("/user/admin/list")
    public ResponseEntity<Result> getAdminUserList(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false) String schId,
                                                   @RequestParam(required = false) String name,
                                                   HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        Map<String, Object> result = adminService.getUserListByRole("ADMIN", page, pageSize, schId, name);
        return Result.success(result, "Admin user list retrieved");
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<Result> getUserById(@PathVariable Long id, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
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
    public ResponseEntity<Result> addUser(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        String username = request.get("username");
        String password = request.get("password");
        String name = request.get("name");
        String role = request.get("role");
        String schId = request.get("sch_id");

        if (username == null || password == null || name == null || role == null || schId == null) {
            return Result.error(400, "用户名、密码、姓名、身份和工号为必填项");
        }

        if (adminService.getUserByUsername(username) != null) {
            return Result.error(409, "用户名已存在，请更换用户名");
        }

        if (adminService.getUserBySchId(schId) != null) {
            return Result.error(409, "该工号已被使用，请更换工号");
        }

        User user = adminService.addUser(request);
        return Result.success(user, "添加成功");
    }

    /**
     * 修改用户
     */
    @PostMapping("/user/update")
    public ResponseEntity<Result> updateUser(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return Result.error(401, "Unauthorized");
        }

        Long currentUserId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        String role = jwtTokenProvider.getRoleFromToken(token);

        Object idObj = request.get("id");
        if (idObj == null || idObj.toString().trim().isEmpty()) {
            return Result.error(400, "User ID is required");
        }
        Long targetId = Long.parseLong(idObj.toString().trim());

        // 非 ADMIN 用户只能修改自己的信息
        if (!"ADMIN".equals(role) && !currentUserId.equals(targetId)) {
            return Result.error(403, "You can only update your own profile");
        }

        // 非 ADMIN 用户不允许修改 role 字段
        if (!"ADMIN".equals(role)) {
            request.remove("role");
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
    public ResponseEntity<Result> deleteUser(@PathVariable Long id, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        User user = adminService.getUserById(id);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        try {
            adminService.deleteUser(id);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
        return Result.success(null, "User deleted successfully");
    }

    /**
     * 添加课程
     */
    @PostMapping("/course/add")
    public ResponseEntity<Result> addCourse(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");

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
    public ResponseEntity<Result> updateCourse(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
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
    public ResponseEntity<Result> deleteCourse(@PathVariable Long id, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        Course course = adminService.getCourseById(id);
        if (course == null) {
            return Result.error(404, "Course not found");
        }

        adminService.deleteCourse(id);
        return Result.success(null, "Course deleted successfully");
    }

    /**
     * 分页获取指定教师的课程列表（供管理员查看教师所教课程）
     */
    @GetMapping("/course/teacher/{teacherId}")
    public ResponseEntity<Result> getTeacherCoursesByTeacherId(@PathVariable String teacherId,
                                                                @RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "10") int pageSize,
                                                                HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        Map<String, Object> result = adminService.getTeacherCoursesByTeacherId(teacherId, page, pageSize);
        return Result.success(result, "Teacher courses retrieved successfully");
    }

    /**
     * 系统统计（各角色用户数量）
     */
    @GetMapping("/statistics")
    public ResponseEntity<Result> getStatistics(HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        Map<String, Object> result = adminService.getStatistics();
        return Result.success(result, "Statistics retrieved");
    }

    /**
     * 获取所有教师
     */
    @GetMapping("/teacher/list")
    public ResponseEntity<Result> getTeacherList(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) String schId,
                                                  @RequestParam(required = false) String name,
                                                  HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        Map<String, Object> result = adminService.getTeachersPaginated(page, pageSize, schId, name);
        return Result.success(result, "Teacher list retrieved");
    }

    /**
     * 添加教师
     */
    @PostMapping("/teacher/add")
    public ResponseEntity<Result> addTeacher(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
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
    public ResponseEntity<Result> updateTeacher(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
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
     * 删除教师（同时删除关联的 user 记录）
     */
    @PostMapping("/teacher/delete/{schId}")
    public ResponseEntity<Result> deleteTeacher(@PathVariable String schId, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        Teacher teacher = adminService.getTeacherBySchId(schId);
        if (teacher == null) {
            return Result.error(404, "Teacher not found");
        }

        adminService.deleteTeacherCascade(schId);
        return Result.success(null, "Teacher deleted successfully");
    }

    /**
     * 添加学生
     */
    @PostMapping("/student/add")
    public ResponseEntity<Result> addStudent(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
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
    public ResponseEntity<Result> updateStudent(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
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
     * 删除学生（同时删除关联的 user 记录）
     */
    @PostMapping("/student/delete/{sid}")
    public ResponseEntity<Result> deleteStudent(@PathVariable String sid, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        Student student = adminService.getStudentBySid(sid);
        if (student == null) {
            return Result.error(404, "Student not found");
        }

        adminService.deleteStudentCascade(sid);
        return Result.success(null, "Student deleted successfully");
    }

    /**
     * 添加学生用户（原子操作：同时创建 user 和 student 记录）
     */
    @PostMapping("/student-user/add")
    public ResponseEntity<Result> addStudentUser(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        if (requireAdmin(httpRequest) == null) return Result.error(401, "Admin authentication required");
        String username = request.get("username");
        String password = request.get("password");
        String name = request.get("name");
        String phone = request.get("phone");
        String sid = request.get("sid");
        String major = request.get("major");
        String gender = request.get("gender");
        String sClassStr = request.get("s_class");

        if (username == null || password == null || name == null || sid == null) {
            return Result.error(400, "用户名、密码、姓名和学号为必填项");
        }

        if (adminService.getUserByUsername(username) != null) {
            return Result.error(409, "用户名已存在，请更换用户名");
        }

        if (adminService.getUserBySchId(sid) != null) {
            return Result.error(409, "该学号已被使用，请更换学号");
        }

        Map<String, Object> result = adminService.addStudentUser(request);
        return Result.success(result, "添加学生成功");
    }

    // -- Honor management (Feature 9) --
    @GetMapping("/honor/list")
    public ResponseEntity<Result> getHonorList(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) String sid,
            @RequestParam(required = false) String name, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        Map<String, Object> result = honorService.getHonorList(sid, name, page, pageSize);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        if (list != null) for (Map<String, Object> h : list) {
            if (h.get("award_date") != null) h.put("award_date", h.get("award_date").toString().substring(0,10));
        }
        return Result.success(result, "ok");
    }
    @GetMapping("/honor/detail/{id}")
    public ResponseEntity<Result> getHonorDetail(@PathVariable Long id, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        Map<String, Object> detail = honorService.getByIdWithName(id);
        if (detail != null && detail.get("award_date") != null) {
            detail.put("award_date", detail.get("award_date").toString().substring(0,10));
        }
        return Result.success(detail, "ok");
    }
    @PostMapping("/honor/add")
    public ResponseEntity<Result> addHonor(@RequestBody Honor honor, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        try { return Result.success(honorService.addHonor(honor), "添加成功"); }
        catch (RuntimeException e) { return Result.error(400, e.getMessage()); }
    }
    @PostMapping("/honor/update")
    public ResponseEntity<Result> updateHonor(@RequestBody Honor honor, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        return Result.success(honorService.updateHonor(honor), "修改成功");
    }
    @PostMapping("/honor/delete/{id}")
    public ResponseEntity<Result> deleteHonor(@PathVariable Long id, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        honorService.deleteHonor(id);
        return Result.success(null, "删除成功");
    }

    // -- Innovation practice management (Feature 10) --
    @GetMapping("/innovation-practice/pending")
    public ResponseEntity<Result> getPendingPractices(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name, @RequestParam(required = false) String title,
            @RequestParam(required = false) String type, @RequestParam(required = false) String status,
            HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        return Result.success(practiceService.getPending(page, pageSize, name, title, type, status), "ok");
    }
    @PostMapping("/innovation-practice/approve")
    public ResponseEntity<Result> approvePractice(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        Long id = Long.parseLong(body.get("id").toString());
        String status = (String) body.get("status");
        String comment = (String) body.get("comment");
        practiceService.approve(id, status, Long.parseLong(requireAdmin(req)), comment);
        return Result.success(null, "操作成功");
    }
    @GetMapping("/innovation-practice/detail/{id}")
    public ResponseEntity<Result> getPracticeDetail(@PathVariable Long id, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        return Result.success(practiceService.getByIdWithName(id), "ok");
    }
    @PostMapping("/innovation-practice/delete/{id}")
    public ResponseEntity<Result> deletePractice(@PathVariable Long id, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        practiceService.delete(id);
        return Result.success(null, "删除成功");
    }

    // -- Leave management (Feature 11) --
    @GetMapping("/leave/pending")
    public ResponseEntity<Result> getPendingLeaves(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name, @RequestParam(required = false) String type,
            @RequestParam(required = false) String status, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        return Result.success(leaveService.getPending(page, pageSize, name, type, status), "ok");
    }
    @GetMapping("/leave/detail/{id}")
    public ResponseEntity<Result> getLeaveDetail(@PathVariable Long id, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        return Result.success(leaveMapper.getByIdWithName(id), "ok");
    }
    @PostMapping("/leave/approve")
    public ResponseEntity<Result> approveLeave(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        Long id = Long.parseLong(body.get("id").toString());
        String status = (String) body.get("status");
        String comment = (String) body.get("comment");
        leaveService.approve(id, status, Long.parseLong(requireAdmin(req)), comment);
        return Result.success(null, "操作成功");
    }

    // -- Activity management (Feature 12) --
    @GetMapping("/activity/list")
    public ResponseEntity<Result> getActivityList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        return Result.success(activityService.getAllWithCount(page, pageSize, keyword), "ok");
    }
    @PostMapping("/activity/publish")
    public ResponseEntity<Result> publishActivity(@RequestBody Activity activity, HttpServletRequest req) {
        String adminId = requireAdmin(req);
        if (adminId == null) return Result.error(401, "Admin authentication required");
        activity.setSid(adminId);
        return Result.success(activityService.create(activity), "发布成功");
    }
    @PostMapping("/activity/update")
    public ResponseEntity<Result> updateActivity(@RequestBody Activity activity, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        return Result.success(activityService.update(activity), "更新成功");
    }
    @PostMapping("/activity/delete/{id}")
    public ResponseEntity<Result> deleteActivity(@PathVariable Long id, HttpServletRequest req) {
        if (requireAdmin(req) == null) return Result.error(401, "Admin authentication required");
        activityService.delete(id);
        return Result.success(null, "删除成功");
    }

    /**
     * 从HTTP请求中提取Bearer Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}