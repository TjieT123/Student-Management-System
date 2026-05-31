package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import cn.edu.sdu.sms.server.service.HomeworkService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 教师接口，提供作业的发布、查看和批改功能。
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 发布新作业
     */
    @PostMapping("/homework/publish")
    public ResponseEntity<Result> publishHomework(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        String teacherId = jwtTokenProvider.getUserIdFromToken(token);

        Long courseId = Long.parseLong(request.get("courseId").toString());
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        String deadlineStr = (String) request.get("deadline");

        if (title == null || content == null || deadlineStr == null) {
            return Result.error(400, "Title, content, and deadline are required");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime deadline = LocalDateTime.parse(deadlineStr, fmt);

        Homework homework = homeworkService.publishHomework(courseId, title, content, deadline, teacherId);

        return Result.success(homework, "Homework published successfully");
    }

    /**
     * 分页获取作业列表
     */
    @GetMapping("/homework/list")
    public ResponseEntity<Result> getTeacherHomeworkList(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                         HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Map<String, Object> result = homeworkService.getHomeworkPaginated(page, pageSize);
        return Result.success(result, "Homework list retrieved");
    }

    /**
     * 分页获取指定作业的学生提交列表（不含 content、comment，含 studentName）
     */
    @GetMapping("/homework/submit/list")
    public ResponseEntity<Result> getHomeworkSubmissions(@RequestParam Long homeworkId,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = homeworkService.getSubmissionsPaginated(homeworkId, page, pageSize);
        return Result.success(result, "Submissions retrieved");
    }

    /**
     * 获取单个提交详情（含 content、comment 等完整信息）
     */
    @GetMapping("/homework/submission/{id}")
    public ResponseEntity<Result> getSubmissionDetail(@PathVariable Long id, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        HomeworkSubmit submit = homeworkService.getSubmissionById(id);
        if (submit == null) {
            return Result.error(404, "Submission not found");
        }

        return Result.success(submit, "Submission retrieved");
    }

    /**
     * 批改学生作业
     */
    @PostMapping("/homework/check")
    public ResponseEntity<Result> gradeHomework(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Long submitId = Long.parseLong(request.get("submitId").toString());
        Integer score = Integer.parseInt(request.get("score").toString());
        String comment = (String) request.get("comment");

        HomeworkSubmit submit = homeworkService.gradeHomework(submitId, score, comment);
        if (submit == null) {
            return Result.error(404, "Submission not found");
        }

        return Result.success(submit, "Homework graded successfully");
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