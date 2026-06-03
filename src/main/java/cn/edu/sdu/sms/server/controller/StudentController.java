package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import cn.edu.sdu.sms.server.service.AiGradingService;
import cn.edu.sdu.sms.server.service.HomeworkService;
import cn.edu.sdu.sms.server.service.StudentService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
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

    @Autowired
    private AiGradingService aiGradingService;

    /**
     * 分页获取所有学生信息
     */
    @GetMapping("/getAll")
    public ResponseEntity<Result> getAllStudents(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String sid,
                                                 @RequestParam(required = false) String name) {
        Map<String, Object> result = studentService.getStudentsPaginated(page, pageSize, sid, name);
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
     * 获取作业详情（含content、teacherName、deadline、courseName、score、status）
     */
    @GetMapping("/api/student/homework/{homeworkId}/content")
    public ResponseEntity<Result> getHomeworkContent(@PathVariable Long homeworkId, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            Map<String, Object> data = homeworkService.getHomeworkContentWithDetails(homeworkId, userId);
            if (data == null) {
                return Result.error(404, "Homework not found");
            }
            return Result.success(data, "ok");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取当前学生对指定作业的提交记录
     */
    @GetMapping("/api/student/homework/my-submission")
    public ResponseEntity<Result> getMySubmission(@RequestParam Long homeworkId, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            HomeworkSubmit submit = homeworkService.getMySubmission(homeworkId, userId);
            if (submit == null) {
                return Result.error(404, "Submission not found");
            }
            return Result.success(submit, "ok");
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
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

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

        try {
            HomeworkSubmit submit = homeworkService.submitHomework(homeworkId, userId, content);
            return Result.success(submit, "Homework submitted successfully");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取作业提交详情
     */
    @GetMapping("/api/student/homework/submission/{id}")
    public ResponseEntity<Result> getSubmissionDetails(@PathVariable Long id) {
        Map<String, Object> submit = homeworkService.getSubmissionDetailWithStudent(id);
        if (submit == null) {
            return Result.error(404, "Submission not found");
        }

        return Result.success(submit, "Submission retrieved");
    }

    /**
     * AI 学习建议
     */
    @PostMapping("/api/student/homework/ai-suggestion")
    public ResponseEntity<Result> getAiSuggestion(@RequestBody Map<String, Object> request,
                                                   HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        Long submissionId = Long.parseLong(request.get("submissionId").toString());
        HomeworkSubmit submit = homeworkService.getSubmissionById(submissionId);
        if (submit == null) {
            return Result.error(404, "Submission not found");
        }

        // 校验当前学生是否为该提交的所属人
        try {
            HomeworkSubmit mySubmit = homeworkService.getMySubmission(submit.getHomeworkId(), userId);
            if (mySubmit == null || !mySubmit.getId().equals(submissionId)) {
                return Result.error(403, "You can only view suggestions for your own submission");
            }
        } catch (RuntimeException e) {
            return Result.error(403, "Access denied");
        }

        Homework homework = homeworkService.getHomeworkById(submit.getHomeworkId());
        if (homework == null) {
            return Result.error(404, "Homework not found");
        }

        String suggestion = aiGradingService.getSuggestion(
                homework.getTitle(), homework.getContent(),
                submit.getContent(), submit.getScore(), submit.getComment());

        if (suggestion == null) {
            return Result.error(503, "AI建议服务暂时不可用，请稍后重试");
        }

        Map<String, String> data = new HashMap<>();
        data.put("suggestion", suggestion);
        return Result.success(data, "ok");
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