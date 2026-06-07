package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.CourseMapper;
import cn.edu.sdu.sms.server.mapper.HomeworkMapper;
import cn.edu.sdu.sms.server.mapper.HomeworkSubmitMapper;
import cn.edu.sdu.sms.server.mapper.StudentMapperEnhanced;
import cn.edu.sdu.sms.server.mapper.UserMapper;
import cn.edu.sdu.sms.server.models.AttachmentItem;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import cn.edu.sdu.sms.server.models.Student;
import cn.edu.sdu.sms.server.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class HomeworkService {

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkSubmitMapper submitMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StudentMapperEnhanced studentMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private AttachmentService attachmentService;

    public List<Homework> getHomeworkByCourseId(Long courseId) {
        return homeworkMapper.getHomeworkByCourseId(courseId);
    }

    public Homework getHomeworkById(Long id) {
        return homeworkMapper.getHomeworkById(id);
    }

    @Transactional
    public Homework updateHomework(Long id, String title, String content, String deadline, Long userId) {
        Homework homework = homeworkMapper.getHomeworkById(id);
        if (homework == null) {
            throw new RuntimeException("Homework not found");
        }
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (homework.getTeacherId() == null || !homework.getTeacherId().equals(userId.toString())) {
            throw new RuntimeException("Only the homework owner can edit");
        }

        if (title != null) homework.setTitle(title);
        if (content != null) homework.setContent(content);
        if (deadline != null) {
            homework.setDeadline(LocalDateTime.parse(deadline, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        homeworkMapper.updateHomeworkSelective(homework);
        return homework;
    }

    @Transactional
    public void deleteHomework(Long id, Long userId) {
        Homework homework = homeworkMapper.getHomeworkById(id);
        if (homework == null) {
            throw new RuntimeException("Homework not found");
        }
        if (homework.getTeacherId() == null || !homework.getTeacherId().equals(userId.toString())) {
            throw new RuntimeException("Only the homework owner can delete");
        }

        submitMapper.deleteByHomeworkId(id);
        homeworkMapper.deleteHomework(id);
    }

    public Map<String, Object> getHomeworkContentWithDetails(Long homeworkId, Long userId) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Map<String, Object> result = homeworkMapper.getHomeworkContentWithDetails(homeworkId, user.getSchId());
        if (result == null) {
            return null;
        }
        // 解析 attachments JSON 字符串为数组
        Object attachmentsObj = result.get("attachments");
        if (attachmentsObj instanceof String) {
            result.put("attachments", attachmentService.parseAttachments((String) attachmentsObj));
        }
        return result;
    }

    public List<Homework> getAllHomework() {
        return homeworkMapper.getAllHomework();
    }

    /**
     * 分页获取作业列表（不含content、teacherId、createTime，含teacherName）
     */
    public Map<String, Object> getHomeworkPaginated(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        int total = homeworkMapper.countAllHomework();
        List<Map<String, Object>> list = homeworkMapper.getHomeworkWithTeacherPaginated(offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    /**
     * 分页获取指定课程作业列表（不含courseId、content、teacherId、createTime，含courseName、teacherName和当前学生的提交status）
     */
    public Map<String, Object> getHomeworkByCourseIdPaginated(Long courseId, Long userId, int page, int pageSize) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String sid = user.getSchId();
        int offset = (page - 1) * pageSize;
        int total = homeworkMapper.countHomeworkByCourseId(courseId);
        List<Map<String, Object>> list = homeworkMapper.getHomeworkByCourseIdWithDetailsPaginated(courseId, sid, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    /**
     * 分页获取指定作业的提交列表（不含sid、content、comment，含studentName）
     */
    public Map<String, Object> getSubmissionsPaginated(Long homeworkId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        int total = submitMapper.countByHomeworkId(homeworkId);
        List<Map<String, Object>> list = submitMapper.getSubmissionsWithStudent(homeworkId, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    public List<Map<String, Object>> getUnsubmittedStudents(Long homeworkId) {
        Homework homework = homeworkMapper.getHomeworkById(homeworkId);
        if (homework == null) return List.of();
        Long courseId = homework.getCourseId();
        List<Map<String, Object>> enrolled = courseMapper.getEnrolledStudents(courseId);
        if (enrolled == null || enrolled.isEmpty()) return List.of();
        // 直接用 DISTINCT sid 查询，不依赖对象映射
        List<String> submittedSids = submitMapper.getSubmittedSids(homeworkId);
        Set<String> sidSet = new HashSet<>(submittedSids != null ? submittedSids : List.of());
        List<Map<String, Object>> unsubmitted = new ArrayList<>();
        for (Map<String, Object> stu : enrolled) {
            String sid = (String) stu.get("sid");
            if (sid != null && !sidSet.contains(sid)) unsubmitted.add(stu);
        }
        return unsubmitted;
    }

    public Map<String, Object> getHomeworkStatistics(Long homeworkId, Long userId) {
        Homework homework = homeworkMapper.getHomeworkById(homeworkId);
        if (homework == null) {
            throw new RuntimeException("Homework not found");
        }

        // 权限校验：当前教师必须是该作业所属课程的任课教师
        cn.edu.sdu.sms.server.models.Course course = courseMapper.getCourseById(homework.getCourseId());
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (course.getTeacherId() == null || !course.getTeacherId().equals(user.getSchId())) {
            throw new RuntimeException("Only the course teacher can view statistics");
        }

        int totalStudents = courseMapper.countStudentsByCourseId(homework.getCourseId());
        List<String> submittedSids = submitMapper.getSubmittedSids(homeworkId);
        int submittedCount = submittedSids != null ? submittedSids.size() : 0;
        // 获取分数用于分布统计
        List<Integer> scores = submitMapper.getScoresByHomeworkId(homeworkId);
        int unsubmittedCount = totalStudents - submittedCount;

        int fail = 0, pass = 0, good = 0, excellent = 0;
        for (Integer s : scores) {
            if (s == null) continue;
            if (s < 60) fail++;
            else if (s < 70) pass++;
            else if (s < 85) good++;
            else excellent++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalStudents", totalStudents);
        result.put("submittedCount", submittedCount);
        result.put("unsubmittedCount", unsubmittedCount);

        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("fail", fail);
        distribution.put("pass", pass);
        distribution.put("good", good);
        distribution.put("excellent", excellent);
        result.put("distribution", distribution);

        return result;
    }

    public HomeworkSubmit getSubmissionByHomeworkIdAndSid(Long homeworkId, String sid) {
        return submitMapper.getSubmissionByHomeworkIdAndSid(homeworkId, sid);
    }

    public Map<String, Object> getMySubmission(Long homeworkId, Long userId) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        HomeworkSubmit submit = submitMapper.getSubmissionByHomeworkIdAndSid(homeworkId, user.getSchId());
        if (submit == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("id", submit.getId());
        result.put("homeworkId", submit.getHomeworkId());
        result.put("sid", submit.getSid());
        result.put("content", submit.getContent());
        result.put("score", submit.getScore());
        result.put("comment", submit.getComment());
        result.put("status", submit.getStatus());
        result.put("submitTime", submit.getSubmitTime());
        result.put("attachments", attachmentService.parseAttachments(submit.getAttachments()));
        return result;
    }

    public HomeworkSubmit getSubmissionById(Long id) {
        return submitMapper.getSubmissionById(id);
    }

    public Map<String, Object> getSubmissionDetailWithStudent(Long id) {
        HomeworkSubmit submit = submitMapper.getSubmissionById(id);
        if (submit == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("id", submit.getId());
        result.put("homeworkId", submit.getHomeworkId());
        result.put("sid", submit.getSid());
        result.put("content", submit.getContent());
        result.put("score", submit.getScore());
        result.put("comment", submit.getComment());
        result.put("status", submit.getStatus());
        result.put("submitTime", submit.getSubmitTime());
        result.put("attachments", attachmentService.parseAttachments(submit.getAttachments()));

        Student student = studentMapper.getStudentBySid(submit.getSid());
        result.put("studentName", student != null ? student.getName() : null);

        Homework homework = homeworkMapper.getHomeworkById(submit.getHomeworkId());
        if (homework != null && homework.getTeacherId() != null) {
            User teacher = userMapper.getUserById(Long.parseLong(homework.getTeacherId()));
            result.put("teacherName", teacher != null ? teacher.getName() : null);
        } else {
            result.put("teacherName", null);
        }

        return result;
    }

    public List<HomeworkSubmit> getSubmissionsByHomeworkId(Long homeworkId) {
        return submitMapper.getSubmissionsByHomeworkId(homeworkId);
    }

    public HomeworkSubmit submitHomework(Long homeworkId, Long userId, String content) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String sid = user.getSchId();

        HomeworkSubmit existing = submitMapper.getSubmissionByHomeworkIdAndSid(homeworkId, sid);
        if (existing != null) {
            existing.setContent(content);
            existing.setSubmitTime(LocalDateTime.now());
            existing.setStatus("SUBMITTED");
            submitMapper.updateSubmission(existing);
            return existing;
        }

        HomeworkSubmit submit = new HomeworkSubmit();
        submit.setHomeworkId(homeworkId);
        submit.setSid(sid);
        submit.setContent(content);
        submit.setStatus("SUBMITTED");
        submit.setSubmitTime(LocalDateTime.now());
        submitMapper.insertSubmission(submit);
        return submit;
    }

    public Homework publishHomework(Long courseId, String title, String content, LocalDateTime deadline, String teacherId) {
        Homework homework = new Homework();
        homework.setCourseId(courseId);
        homework.setTitle(title);
        homework.setContent(content);
        homework.setDeadline(deadline);
        homework.setTeacherId(teacherId);
        homework.setCreateTime(LocalDateTime.now());
        homeworkMapper.insertHomework(homework);
        return homework;
    }

    public String getHomeworkAttachments(Long homeworkId) {
        return homeworkMapper.getAttachments(homeworkId);
    }

    // 教师追加作业附件
    public List<AttachmentItem> addHomeworkAttachment(Long homeworkId, AttachmentItem item, Long userId) {
        Homework homework = homeworkMapper.getHomeworkById(homeworkId);
        if (homework == null) throw new RuntimeException("Homework not found");
        if (homework.getTeacherId() == null || !homework.getTeacherId().equals(userId.toString()))
            throw new RuntimeException("Only the homework owner can upload attachments");

        String current = homeworkMapper.getAttachments(homeworkId);
        String newJson = attachmentService.addAttachment(current, item);
        homeworkMapper.updateAttachments(homeworkId, newJson);
        return attachmentService.parseAttachments(newJson);
    }

    // 教师删除作业附件
    public void removeHomeworkAttachment(Long homeworkId, int index, Long userId) {
        Homework homework = homeworkMapper.getHomeworkById(homeworkId);
        if (homework == null) throw new RuntimeException("Homework not found");
        if (homework.getTeacherId() == null || !homework.getTeacherId().equals(userId.toString()))
            throw new RuntimeException("Only the homework owner can delete attachments");

        String current = homeworkMapper.getAttachments(homeworkId);
        List<AttachmentItem> list = attachmentService.parseAttachments(current);
        if (index < 0 || index >= list.size()) throw new RuntimeException("Invalid attachment index");
        String newJson = attachmentService.removeAttachment(current, index);
        homeworkMapper.updateAttachments(homeworkId, newJson);
    }

    // 学生追加提交附件
    public List<AttachmentItem> addSubmissionAttachment(Long homeworkId, Long userId, AttachmentItem item) {
        User user = userMapper.getUserById(userId);
        if (user == null) throw new RuntimeException("User not found");
        String sid = user.getSchId();

        // 校验已选课
        cn.edu.sdu.sms.server.models.Course course =
            courseMapper.getCourseById(homeworkMapper.getHomeworkById(homeworkId).getCourseId());
        if (courseMapper.countStudentCourse(sid, homeworkMapper.getHomeworkById(homeworkId).getCourseId()) == 0)
            throw new RuntimeException("You are not enrolled in this course");

        Homework homework = homeworkMapper.getHomeworkById(homeworkId);
        if (homework == null) throw new RuntimeException("Homework not found");

        // 校验截止时间
        if (LocalDateTime.now().isAfter(homework.getDeadline()))
            throw new RuntimeException("Homework deadline has passed");

        // 查或建提交记录
        HomeworkSubmit submit = submitMapper.getSubmissionByHomeworkIdAndSid(homeworkId, sid);
        if (submit == null) {
            submit = new HomeworkSubmit();
            submit.setHomeworkId(homeworkId);
            submit.setSid(sid);
            submit.setContent("");
            submit.setStatus("SUBMITTED");
            submit.setSubmitTime(LocalDateTime.now());
            submitMapper.insertSubmission(submit);
            submit = submitMapper.getSubmissionByHomeworkIdAndSid(homeworkId, sid);
        }

        if ("GRADED".equals(submit.getStatus()))
            throw new RuntimeException("Cannot modify a graded submission");

        String current = submitMapper.getAttachments(submit.getId());
        String newJson = attachmentService.addAttachment(current, item);
        submitMapper.updateAttachments(submit.getId(), newJson);
        return attachmentService.parseAttachments(newJson);
    }

    // 学生删除提交附件
    public void removeSubmissionAttachment(int index, Long homeworkId, Long userId) {
        User user = userMapper.getUserById(userId);
        if (user == null) throw new RuntimeException("User not found");
        String sid = user.getSchId();

        cn.edu.sdu.sms.server.models.Course course =
            courseMapper.getCourseById(homeworkMapper.getHomeworkById(homeworkId).getCourseId());

        Homework homework = homeworkMapper.getHomeworkById(homeworkId);
        if (homework == null) throw new RuntimeException("Homework not found");
        if (LocalDateTime.now().isAfter(homework.getDeadline()))
            throw new RuntimeException("Homework deadline has passed");

        HomeworkSubmit submit = submitMapper.getSubmissionByHomeworkIdAndSid(homeworkId, sid);
        if (submit == null) throw new RuntimeException("Submission not found");
        if ("GRADED".equals(submit.getStatus()))
            throw new RuntimeException("Cannot modify a graded submission");

        String current = submitMapper.getAttachments(submit.getId());
        List<AttachmentItem> list = attachmentService.parseAttachments(current);
        if (index < 0 || index >= list.size()) throw new RuntimeException("Invalid attachment index");
        String newJson = attachmentService.removeAttachment(current, index);
        submitMapper.updateAttachments(submit.getId(), newJson);
    }

    public HomeworkSubmit gradeHomework(Long submitId, Integer score, String comment) {
        HomeworkSubmit submit = submitMapper.getSubmissionById(submitId);
        if (submit != null) {
            submit.setScore(score);
            submit.setComment(comment);
            submit.setStatus("GRADED");
            submitMapper.updateSubmission(submit);
        }
        return submit;
    }
}