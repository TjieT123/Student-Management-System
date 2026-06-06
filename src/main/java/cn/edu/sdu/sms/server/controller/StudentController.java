package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.AttachmentItem;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import cn.edu.sdu.sms.server.service.AiGradingService;
import cn.edu.sdu.sms.server.service.AttachmentService;
import cn.edu.sdu.sms.server.service.CourseScoreService;
import cn.edu.sdu.sms.server.service.CourseService;
import cn.edu.sdu.sms.server.service.HomeworkService;
import cn.edu.sdu.sms.server.service.NotificationService;

import cn.edu.sdu.sms.server.service.StudentService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private CourseScoreService courseScoreService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.UserMapper userMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.HomeworkMapper homeworkMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.CourseMapper courseMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.AnnouncementMapper announcementMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.HonorMapper honorMapper;

    @Autowired
    private cn.edu.sdu.sms.server.mapper.InnovationPracticeMapper practiceMapper;

    @Autowired
    private cn.edu.sdu.sms.server.service.HonorService honorService;

    @Autowired
    private cn.edu.sdu.sms.server.service.InnovationPracticeService practiceService;

    @Autowired
    private cn.edu.sdu.sms.server.service.LeaveService leaveService;

    @Autowired
    private cn.edu.sdu.sms.server.service.ActivityService activityService;

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
            Map<String, Object> submit = homeworkService.getMySubmission(homeworkId, userId);
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
            return Result.success(data, "Homework submitted successfully");
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
     * 学生追加提交附件
     */
    @PutMapping("/api/student/homework/{homeworkId}/attachment")
    public ResponseEntity<Result> addSubmissionAttachment(@PathVariable String homeworkId,
                                                           @RequestBody AttachmentItem item,
                                                           HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));

        try {
            Long hwId = Long.parseLong(homeworkId);
            List<AttachmentItem> list = homeworkService.addSubmissionAttachment(hwId, userId, item);
            Map<String, Object> data = new HashMap<>();
            data.put("totalCount", list.size());
            return Result.success(data, "附件上传成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 学生删除提交附件
     */
    @DeleteMapping("/api/student/homework/attachment/{index}")
    public ResponseEntity<Result> deleteSubmissionAttachment(@PathVariable int index,
                                                              @RequestBody Map<String, Object> request,
                                                              HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        Long homeworkId = Long.parseLong(request.get("homeworkId").toString());

        try {
            homeworkService.removeSubmissionAttachment(index, homeworkId, userId);
            Map<String, Object> data = new HashMap<>();
            data.put("totalCount", 0);
            return Result.success(data, "附件删除成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
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
            Map<String, Object> mySubmit = homeworkService.getMySubmission(submit.getHomeworkId(), userId);
            if (mySubmit == null || !submissionId.equals(((Number) mySubmit.get("id")).longValue())) {
                return Result.error(403, "You can only view suggestions for your own submission");
            }
        } catch (RuntimeException e) {
            return Result.error(403, "Access denied");
        }

        Homework homework = homeworkService.getHomeworkById(submit.getHomeworkId());
        if (homework == null) {
            return Result.error(404, "Homework not found");
        }

        String hwAttJson = homeworkMapper.getAttachments(submit.getHomeworkId());
        String suggestion = aiGradingService.getSuggestion(
                homework.getTitle(), homework.getContent(),
                submit.getContent(), submit.getScore(), submit.getComment(),
                hwAttJson, submit.getAttachments());

        if (suggestion == null) {
            return Result.error(503, "AI建议服务暂时不可用，请稍后重试");
        }

        Map<String, String> data = new HashMap<>();
        data.put("suggestion", suggestion);
        return Result.success(data, "ok");
    }

    // -- Profile (Feature 1) --
    @GetMapping("/api/student/profile")
    public ResponseEntity<Result> getProfile(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        cn.edu.sdu.sms.server.models.User user = userMapper.getUserById(userId);
        if (user == null) return Result.error(404, "User not found");
        cn.edu.sdu.sms.server.models.Student student = studentService.getStudentBySid(user.getSchId());
        return Result.success(student, "ok");
    }

    // -- Scores & GPA (Feature 4) --
    @GetMapping("/api/student/scores")
    public ResponseEntity<Result> getMyScores(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        cn.edu.sdu.sms.server.models.User user = userMapper.getUserById(userId);
        if (user == null) return Result.error(404, "User not found");
        return Result.success(courseScoreService.getStudentScores(user.getSchId()), "ok");
    }

    @GetMapping("/api/student/gpa")
    public ResponseEntity<Result> getMyGPA(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        cn.edu.sdu.sms.server.models.User user = userMapper.getUserById(userId);
        if (user == null) return Result.error(404, "User not found");
        return Result.success(courseScoreService.calculateGPA(user.getSchId()), "ok");
    }

    // -- Home summary & profile-summary & resume (Features 5, 6) --
    @GetMapping("/api/student/home-summary")
    public ResponseEntity<Result> getHomeSummary(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        cn.edu.sdu.sms.server.models.User user = userMapper.getUserById(userId);
        if (user == null) return Result.error(404, "User not found");
        String sid = user.getSchId();
        Map<String, Object> result = new HashMap<>();
        result.put("courseCount", courseMapper.countStudentCoursesBySid(sid));
        result.put("announcements", announcementMapper.getAllAnnouncements().subList(0, Math.min(2, 0)));
        return Result.success(result, "ok");
    }

    @GetMapping("/api/student/profile-summary")
    public ResponseEntity<Result> getProfileSummary(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        cn.edu.sdu.sms.server.models.User user = userMapper.getUserById(userId);
        if (user == null) return Result.error(404, "User not found");
        String sid = user.getSchId();
        Map<String, Object> result = new HashMap<>();
        result.put("gpa", courseScoreService.calculateGPA(sid));
        result.put("courseCount", courseMapper.countStudentCoursesBySid(sid));
        result.put("homeworkStats", new HashMap<>());
        return Result.success(result, "ok");
    }

    @GetMapping("/api/student/resume")
    public ResponseEntity<Result> getResume(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        cn.edu.sdu.sms.server.models.User user = userMapper.getUserById(userId);
        if (user == null) return Result.error(404, "User not found");
        String sid = user.getSchId();
        Map<String, Object> result = new HashMap<>();
        result.put("studentInfo", studentService.getStudentBySid(sid));
        result.put("gpa", courseScoreService.calculateGPA(sid));
        result.put("scores", courseScoreService.getStudentScores(sid));
        result.put("honors", honorMapper.getBySid(sid));
        result.put("practices", practiceMapper.getBySid(sid, 0, 100));
        return Result.success(result, "ok");
    }

    // -- Honor (Feature 9) --
    @GetMapping("/api/student/honor/list")
    public ResponseEntity<Result> getMyHonors(HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        List<Map<String, Object>> honors = honorService.getBySidWithName(sid);
        for (Map<String, Object> h : honors) {
            if (h.get("award_date") != null) h.put("award_date", h.get("award_date").toString().substring(0,10));
        }
        return Result.success(honors, "ok");
    }

    // -- Innovation Practice (Feature 10) --
    @PostMapping("/api/student/innovation-practice/submit")
    public ResponseEntity<Result> submitPractice(@RequestBody cn.edu.sdu.sms.server.models.InnovationPractice practice, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        practice.setSid(sid);
        return Result.success(practiceService.submit(practice), "提交成功");
    }
    @GetMapping("/api/student/innovation-practice/my")
    public ResponseEntity<Result> getMyPractices(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        return Result.success(practiceService.getMyPractices(sid, page, pageSize), "ok");
    }

    @PutMapping("/api/student/innovation-practice/update/{id}")
    public ResponseEntity<Result> updatePractice(@PathVariable Long id, @RequestBody cn.edu.sdu.sms.server.models.InnovationPractice practice, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        // Verify ownership
        cn.edu.sdu.sms.server.models.InnovationPractice existing = practiceService.getById(id);
        if (existing == null) return Result.error(404, "实践记录不存在");
        if (!existing.getSid().equals(sid)) return Result.error(403, "无权修改");
        if (!"PENDING".equals(existing.getStatus()) && !"REJECTED".equals(existing.getStatus()))
            return Result.error(400, "只能编辑待审批或已驳回的记录");
        practice.setId(id); practice.setSid(sid); practice.setStatus("PENDING");
        practiceService.update(practice);
        return Result.success(null, "修改成功，已重新提交审批");
    }

    @DeleteMapping("/api/student/innovation-practice/delete/{id}")
    public ResponseEntity<Result> deleteMyPractice(@PathVariable Long id, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        cn.edu.sdu.sms.server.models.InnovationPractice existing = practiceService.getById(id);
        if (existing == null) return Result.error(404, "实践记录不存在");
        if (!existing.getSid().equals(sid)) return Result.error(403, "无权删除");
        practiceService.delete(id);
        return Result.success(null, "删除成功");
    }

    // -- Leave (Feature 11) --
    @PostMapping("/api/student/leave/apply")
    public ResponseEntity<Result> applyLeave(@RequestBody cn.edu.sdu.sms.server.models.LeaveRequest leave, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        leave.setSid(userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId());
        if (leave.getStartDate().isAfter(leave.getEndDate())) return Result.error(400, "开始日期不能晚于结束日期");
        return Result.success(leaveService.apply(leave), "提交成功");
    }
    @PutMapping("/api/student/leave/update/{id}")
    public ResponseEntity<Result> updateLeave(@PathVariable Long id, @RequestBody cn.edu.sdu.sms.server.models.LeaveRequest leave, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        cn.edu.sdu.sms.server.models.LeaveRequest existing = leaveService.getById(id);
        if (existing == null) return Result.error(404, "请假记录不存在");
        if (!existing.getSid().equals(sid)) return Result.error(403, "无权修改");
        if (!"PENDING".equals(existing.getStatus()) && !"REJECTED".equals(existing.getStatus()))
            return Result.error(400, "只能编辑待审批或已驳回的记录");
        leave.setId(id); leave.setSid(sid); leave.setStatus("PENDING");
        leaveService.update(leave);
        return Result.success(null, "修改成功，已重新提交审批");
    }

    @GetMapping("/api/student/leave/my")
    public ResponseEntity<Result> getMyLeaves(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        return Result.success(leaveService.getMyLeaves(sid, page, pageSize), "ok");
    }

    // -- Activity (Feature 12) - Student view & register --
    @GetMapping("/api/student/activity/list")
    public ResponseEntity<Result> getAvailableActivities(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        return Result.success(activityService.getAvailableForStudent(sid, page, pageSize, keyword), "ok");
    }
    @PostMapping("/api/student/activity/register/{activityId}")
    public ResponseEntity<Result> registerActivity(@PathVariable Long activityId, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        try { activityService.register(activityId, sid); return Result.success(null, "报名成功"); }
        catch (RuntimeException e) { return Result.error(400, e.getMessage()); }
    }
    @PostMapping("/api/student/activity/cancel/{activityId}")
    public ResponseEntity<Result> cancelRegistration(@PathVariable Long activityId, HttpServletRequest req) {
        String token = getTokenFromRequest(req);
        if (token == null) return Result.error(401, "Unauthorized");
        String sid = userMapper.getUserById(Long.parseLong(jwtTokenProvider.getUserIdFromToken(token))).getSchId();
        activityService.cancelRegistration(activityId, sid);
        return Result.success(null, "取消成功");
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