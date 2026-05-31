package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.service.CourseService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 课程接口，提供课程列表查看和详情查询功能。
 */
@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 分页获取课程列表
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getAllCourses(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) Long id,
                                                @RequestParam(required = false) String courseName,
                                                @RequestParam(required = false) String teacherId) {
        Map<String, Object> result = courseService.getCoursesPaginated(page, pageSize, id, courseName, teacherId);
        return Result.success(result, "Courses retrieved successfully");
    }

    /**
     * 根据id获取课程
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return Result.error(404, "Course not found");
        }
        return Result.success(course, "Course retrieved");
    }

    /**
     * 分页获取当前学生的选课列表（课程id、courseName、teacherName）
     */
    @GetMapping("/my-courses")
    public ResponseEntity<Result> getMyCourses(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            Map<String, Object> result = courseService.getStudentCoursesPaginated(userId, page, pageSize);
            return Result.success(result, "Student courses retrieved successfully");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 分页获取当前教师所教课程列表
     */
    @GetMapping("/teacher-courses")
    public ResponseEntity<Result> getTeacherCourses(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                     HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            Map<String, Object> result = courseService.getTeacherCoursesPaginated(userId, page, pageSize);
            return Result.success(result, "Teacher courses retrieved successfully");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 修改课程（只传需要修改的字段即可）
     */
    @PostMapping("/update")
    public ResponseEntity<Result> updateCourse(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Object idObj = request.get("id");
        if (idObj == null || idObj.toString().trim().isEmpty()) {
            return Result.error(400, "Course ID is required");
        }

        Long id = Long.parseLong(idObj.toString().trim());
        String courseName = (String) request.get("courseName");
        String detail = (String) request.get("detail");
        String address = (String) request.get("address");
        String teacherId = (String) request.get("teacherId");

        Course course = courseService.updateCourse(id, courseName, detail, address, teacherId);
        if (course == null) {
            return Result.error(404, "Course not found");
        }

        return Result.success(course, "Course updated successfully");
    }

    /**
     * 选课
     */
    @PostMapping("/enroll")
    public ResponseEntity<Result> enrollCourse(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Long courseId = request.get("courseId") != null ? Long.parseLong(request.get("courseId").toString()) : null;
        if (courseId == null) {
            return Result.error(400, "courseId is required");
        }

        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            courseService.enrollCourse(userId, courseId);
            return Result.success(null, "Course enrolled successfully");
        } catch (RuntimeException e) {
            return Result.error(409, e.getMessage());
        }
    }

    /**
     * 取消选课
     */
    @PostMapping("/cancel")
    public ResponseEntity<Result> cancelCourse(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Long courseId = request.get("courseId") != null ? Long.parseLong(request.get("courseId").toString()) : null;
        if (courseId == null) {
            return Result.error(400, "courseId is required");
        }

        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            courseService.cancelCourse(userId, courseId);
            return Result.success(null, "Course canceled successfully");
        } catch (RuntimeException e) {
            return Result.error(409, e.getMessage());
        }
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