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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkSubmitMapper submitMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 发布作业
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

        Homework homework = new Homework();
        homework.setCourseId(courseId);
        homework.setTitle(title);
        homework.setContent(content);
        homework.setDeadline(deadline);
        homework.setTeacherId(teacherId);
        homework.setCreateTime(LocalDateTime.now());

        homeworkMapper.insertHomework(homework);

        return Result.success(homework, "Homework published successfully");
    }

    /**
     * 获取教师作业列表
     */
    @GetMapping("/homework/list")
    public ResponseEntity<Result> getTeacherHomeworkList(HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        String teacherId = jwtTokenProvider.getUserIdFromToken(token);

        List<Homework> homeworks = homeworkMapper.getAllHomework();
        
        return Result.success(homeworks, "Homework list retrieved");
    }

    /**
     * 获取作业提交列表
     */
    @GetMapping("/homework/submit/list")
    public ResponseEntity<Result> getHomeworkSubmissions(@RequestParam Long homeworkId, HttpServletRequest httpRequest) {
        List<HomeworkSubmit> submissions = submitMapper.getSubmissionsByHomeworkId(homeworkId);

        return Result.success(submissions, "Submissions retrieved");
    }

    /**
     * 批改作业
     */
    @PostMapping("/homework/check")
    public ResponseEntity<Result> gradeHomework(@RequestBody Map<String, Object> request) {
        Long submitId = Long.parseLong(request.get("submitId").toString());
        Integer score = Integer.parseInt(request.get("score").toString());
        String comment = (String) request.get("comment");

        HomeworkSubmit submit = submitMapper.getSubmissionById(submitId);
        if (submit == null) {
            return Result.error(404, "Submission not found");
        }

        submit.setScore(score);
        submit.setComment(comment);
        submit.setStatus("GRADED");

        submitMapper.updateSubmission(submit);

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

