package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.mapper.HomeworkMapper;
import cn.edu.sdu.sms.server.mapper.HomeworkSubmitMapper;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
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
@RequestMapping("/api/student")
public class StudentControllerAPI {

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkSubmitMapper submitMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 获取学生作业
     */
    @GetMapping("/homework/list")
    public ResponseEntity<Result> getStudentHomeworkList(@RequestParam Long courseId, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        String studentId = jwtTokenProvider.getUserIdFromToken(token);

        List<Homework> homeworks = homeworkMapper.getHomeworkByCourseId(courseId);

        // Add submission status for each homework
        for (Homework homework : homeworks) {
            HomeworkSubmit submit = submitMapper.getSubmissionByHomeworkIdAndSid(homework.getId(), studentId);
            if (submit != null) {
                // Create a wrapper to include status
                Map<String, Object> hwMap = new HashMap<>();
                hwMap.put("homework", homework);
                hwMap.put("submitted", true);
                hwMap.put("submitTime", submit.getSubmitTime());
            } else {
                // Check if deadline passed
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

    /**
     * 提交作业
     */
    @PostMapping("/homework/submit")
    public ResponseEntity<Result> submitHomework(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        String studentId = jwtTokenProvider.getUserIdFromToken(token);

        Long homeworkId = Long.parseLong(request.get("homeworkId").toString());
        String content = (String) request.get("content");

        if (content == null) {
            return Result.error(400, "Content is required");
        }

        // Check if homework exists and deadline not passed
        Homework homework = homeworkMapper.getHomeworkById(homeworkId);
        if (homework == null) {
            return Result.error(404, "Homework not found");
        }

        if (LocalDateTime.now().isAfter(homework.getDeadline())) {
            return Result.error(400, "Homework deadline has passed");
        }

        // Check if already submitted
        HomeworkSubmit existing = submitMapper.getSubmissionByHomeworkIdAndSid(homeworkId, studentId);
        if (existing != null) {
            // Update existing submission
            existing.setContent(content);
            existing.setSubmitTime(LocalDateTime.now());
            existing.setStatus("未批改");
            submitMapper.updateSubmission(existing);
            return Result.success(existing, "Homework updated successfully");
        }

        // Create new submission
        HomeworkSubmit submit = new HomeworkSubmit();
        submit.setHomeworkId(homeworkId);
        submit.setSid(studentId);
        submit.setContent(content);
        submit.setStatus("未批改");
        submit.setSubmitTime(LocalDateTime.now());

        submitMapper.insertSubmission(submit);

        return Result.success(submit, "Homework submitted successfully");
    }

    /**
     * 获取学生提交详情
     */
    @GetMapping("/homework/submission/{id}")
    public ResponseEntity<Result> getSubmissionDetails(@PathVariable Long id) {
        HomeworkSubmit submit = submitMapper.getSubmissionById(id);
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

