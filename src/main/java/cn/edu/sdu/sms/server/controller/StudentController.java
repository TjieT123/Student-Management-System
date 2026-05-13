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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/getAll")
    public Object getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/api/student/homework/list")
    public ResponseEntity<Result> getStudentHomeworkList(@RequestParam Long courseId, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        String studentId = jwtTokenProvider.getUserIdFromToken(token);

        List<Homework> homeworks = homeworkService.getHomeworkByCourseId(courseId);

        for (Homework homework : homeworks) {
            HomeworkSubmit submit = homeworkService.getSubmissionByHomeworkIdAndSid(homework.getId(), studentId);
            if (submit != null) {
                Map<String, Object> hwMap = new HashMap<>();
                hwMap.put("homework", homework);
                hwMap.put("submitted", true);
                hwMap.put("submitTime", submit.getSubmitTime());
            } else {
                if (LocalDateTime.now().isAfter(homework.getDeadline())) {
                    Map<String, Object> hwMap = new HashMap<>();
                    hwMap.put("homework", homework);
                    hwMap.put("submitted", false);
                    hwMap.put("status", "已截止");
                } else {
                    Map<String, Object> hwMap = new HashMap<>();
                    hwMap.put("homework", homework);
                    hwMap.put("submitted", false);
                    hwMap.put("status", "未提交");
                }
            }
        }

        return Result.success(homeworks, "Homework list retrieved");
    }

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

    @GetMapping("/api/student/homework/submission/{id}")
    public ResponseEntity<Result> getSubmissionDetails(@PathVariable Long id) {
        HomeworkSubmit submit = homeworkService.getSubmissionById(id);
        if (submit == null) {
            return Result.error(404, "Submission not found");
        }

        return Result.success(submit, "Submission retrieved");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
