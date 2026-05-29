package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import cn.edu.sdu.sms.server.service.HomeworkService;
import cn.edu.sdu.sms.server.service.StudentService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 学生接口，提供学生信息查询和作业的查看、提交功能。
 */
@RestController
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 分页获取所有学生信息
     */
    @GetMapping("/getAll")
    public ResponseEntity<Result> getAllStudents(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = studentService.getStudentsPaginated(page, pageSize);
        return Result.success(result, "Students retrieved successfully");
    }

    /**
     * 分页获取某课程的作业列表及提交状态
     */
    @GetMapping("/api/student/homework/list")
    public ResponseEntity<Result> getStudentHomeworkList(@RequestParam Long courseId,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                         HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            Map<String, Object> result = homeworkService.getHomeworkByCourseIdPaginated(courseId, userId, page, pageSize);
            return Result.success(result, "Homework list retrieved");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 提交作业
     */
    @PostMapping("/api/student/homework/submit")
    public ResponseEntity<Result> submitHomework(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        String studentId = jwtTokenProvider.getUserIdFromToken(token);

        Long homeworkId = Long.parseLong(request.get("homeworkId").toString());
        String content = (String) request.get("content");

        if (content == null) {
            return Result.error(400, "Content is required");
        }

        Homework homework = homeworkService.getHomeworkById(homeworkId);
        if (homework == null) {
            return Result.error(404, "Homework not found");
        }

        if (LocalDateTime.now().isAfter(homework.getDeadline())) {
            return Result.error(400, "Homework deadline has passed");
        }

        HomeworkSubmit submit = homeworkService.submitHomework(homeworkId, studentId, content);
        return Result.success(submit, "Homework submitted successfully");
    }

    /**
     * 获取作业提交详情
     */
    @GetMapping("/api/student/homework/submission/{id}")
    public ResponseEntity<Result> getSubmissionDetails(@PathVariable Long id) {
        HomeworkSubmit submit = homeworkService.getSubmissionById(id);
        if (submit == null) {
            return Result.error(404, "Submission not found");
        }

        return Result.success(submit, "Submission retrieved");
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