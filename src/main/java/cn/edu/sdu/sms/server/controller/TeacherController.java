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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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

    @GetMapping("/homework/list")
    public ResponseEntity<Result> getTeacherHomeworkList(HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        String teacherId = jwtTokenProvider.getUserIdFromToken(token);

        List<Homework> homeworks = homeworkService.getAllHomework();

        return Result.success(homeworks, "Homework list retrieved");
    }

    @GetMapping("/homework/submit/list")
    public ResponseEntity<Result> getHomeworkSubmissions(@RequestParam Long homeworkId, HttpServletRequest httpRequest) {
        List<HomeworkSubmit> submissions = homeworkService.getSubmissionsByHomeworkId(homeworkId);

        return Result.success(submissions, "Submissions retrieved");
    }

    @PostMapping("/homework/check")
    public ResponseEntity<Result> gradeHomework(@RequestBody Map<String, Object> request) {
        Long submitId = Long.parseLong(request.get("submitId").toString());
        Integer score = Integer.parseInt(request.get("score").toString());
        String comment = (String) request.get("comment");

        HomeworkSubmit submit = homeworkService.gradeHomework(submitId, score, comment);
        if (submit == null) {
            return Result.error(404, "Submission not found");
        }

        return Result.success(submit, "Homework graded successfully");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
