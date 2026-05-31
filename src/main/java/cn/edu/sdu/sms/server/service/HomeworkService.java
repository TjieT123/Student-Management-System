package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.HomeworkMapper;
import cn.edu.sdu.sms.server.mapper.HomeworkSubmitMapper;
import cn.edu.sdu.sms.server.mapper.StudentMapperEnhanced;
import cn.edu.sdu.sms.server.mapper.UserMapper;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import cn.edu.sdu.sms.server.models.Student;
import cn.edu.sdu.sms.server.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<Homework> getHomeworkByCourseId(Long courseId) {
        return homeworkMapper.getHomeworkByCourseId(courseId);
    }

    public Homework getHomeworkById(Long id) {
        return homeworkMapper.getHomeworkById(id);
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

    public HomeworkSubmit getSubmissionByHomeworkIdAndSid(Long homeworkId, String sid) {
        return submitMapper.getSubmissionByHomeworkIdAndSid(homeworkId, sid);
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

        Student student = studentMapper.getStudentBySid(submit.getSid());
        result.put("studentName", student != null ? student.getName() : null);

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
            existing.setStatus("未批改");
            submitMapper.updateSubmission(existing);
            return existing;
        }

        HomeworkSubmit submit = new HomeworkSubmit();
        submit.setHomeworkId(homeworkId);
        submit.setSid(sid);
        submit.setContent(content);
        submit.setStatus("未批改");
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