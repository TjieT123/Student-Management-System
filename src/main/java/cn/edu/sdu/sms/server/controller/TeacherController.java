package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.dto.AiGradeRequest;
import cn.edu.sdu.sms.server.dto.AiGradeResult;
import cn.edu.sdu.sms.server.models.AttachmentItem;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import cn.edu.sdu.sms.server.service.AiGradingService;
import cn.edu.sdu.sms.server.service.AttachmentService;
import cn.edu.sdu.sms.server.service.HomeworkService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private AiGradingService aiGradingService;

    @Autowired
    private AttachmentService attachmentService;

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

        Map<String, Object> submit = homeworkService.getSubmissionDetailWithStudent(id);
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

        Map<String, Object> data = new HashMap<>();
        data.put("id", submit.getId());
        data.put("homeworkId", submit.getHomeworkId());
        data.put("sid", submit.getSid());
        data.put("content", submit.getContent());
        data.put("score", submit.getScore());
        data.put("comment", submit.getComment());
        data.put("status", submit.getStatus());
        data.put("submitTime", submit.getSubmitTime());
        data.put("attachments", attachmentService.parseAttachments(submit.getAttachments()));
        return Result.success(data, "Homework graded successfully");
    }

    /**
     * AI 判卷
     */
    @PostMapping("/homework/ai-grade")
    public ResponseEntity<Result> aiGrade(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        String role = jwtTokenProvider.getRoleFromToken(token);
        if (!"TEACHER".equals(role)) {
            return Result.error(401, "仅教师角色可使用AI判卷功能");
        }

        Long submitId = Long.parseLong(request.get("submitId").toString());
        String homeworkTitle = (String) request.get("homeworkTitle");
        String homeworkContent = (String) request.get("homeworkContent");

        AiGradeRequest aiRequest = new AiGradeRequest(submitId, homeworkTitle, homeworkContent);
        AiGradeResult result = aiGradingService.grade(aiRequest);

        if (result == null) {
            // 区分是提交不存在还是AI服务异常
            return Result.error(503, "AI评分服务暂时不可用，请稍后重试或手动批改");
        }

        return Result.success(result, "AI评分完成");
    }

    /**
     * 编辑作业
     */
    @PostMapping("/homework/update")
    public ResponseEntity<Result> updateHomework(@RequestBody Map<String, Object> request,
                                                  HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }
        String role = jwtTokenProvider.getRoleFromToken(token);
        if (!"TEACHER".equals(role)) {
            return Result.error(401, "仅教师角色可编辑作业");
        }
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        Long id = Long.parseLong(request.get("id").toString());
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        String deadline = (String) request.get("deadline");

        try {
            Homework homework = homeworkService.updateHomework(id, title, content, deadline, userId);
            return Result.success(homework, "Homework updated successfully");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 删除作业（级联删除提交记录）
     */
    @PostMapping("/homework/delete/{id}")
    public ResponseEntity<Result> deleteHomework(@PathVariable Long id,
                                                  HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }
        String role = jwtTokenProvider.getRoleFromToken(token);
        if (!"TEACHER".equals(role)) {
            return Result.error(401, "仅教师角色可删除作业");
        }
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            homeworkService.deleteHomework(id, userId);
            return Result.success(null, "Homework deleted successfully");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 教师追加作业附件
     */
    @PutMapping("/homework/{homeworkId}/attachment")
    public ResponseEntity<Result> addHomeworkAttachment(@PathVariable String homeworkId,
                                                         @RequestBody AttachmentItem item,
                                                         HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) return Result.error(401, "Unauthorized");
        if (!"TEACHER".equals(jwtTokenProvider.getRoleFromToken(token)))
            return Result.error(401, "仅教师角色可操作");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            Long hwId = Long.parseLong(homeworkId);
            List<AttachmentItem> list = homeworkService.addHomeworkAttachment(hwId, item, userId);
            Map<String, Object> data = new HashMap<>();
            data.put("totalCount", list.size());
            data.put("attachments", list);
            return Result.success(data, "附件上传成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 教师删除作业附件
     */
    @DeleteMapping("/homework/{homeworkId}/attachment/{index}")
    public ResponseEntity<Result> deleteHomeworkAttachment(@PathVariable String homeworkId,
                                                            @PathVariable int index,
                                                            HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) return Result.error(401, "Unauthorized");
        if (!"TEACHER".equals(jwtTokenProvider.getRoleFromToken(token)))
            return Result.error(401, "仅教师角色可操作");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            homeworkService.removeHomeworkAttachment(Long.parseLong(homeworkId), index, userId);
            Map<String, Object> data = new HashMap<>();
            data.put("totalCount", 0);
            return Result.success(data, "附件删除成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 作业提交统计
     */
    @GetMapping("/homework/{homeworkId}/statistics")
    public ResponseEntity<Result> getHomeworkStatistics(@PathVariable Long homeworkId,
                                                         HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }
        String role = jwtTokenProvider.getRoleFromToken(token);
        if (!"TEACHER".equals(role)) {
            return Result.error(401, "仅教师角色可查看统计");
        }
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            Map<String, Object> result = homeworkService.getHomeworkStatistics(homeworkId, userId);
            return Result.success(result, "Statistics retrieved");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
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