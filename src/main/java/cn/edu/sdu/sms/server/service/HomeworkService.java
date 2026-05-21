package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.HomeworkMapper;
import cn.edu.sdu.sms.server.mapper.HomeworkSubmitMapper;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
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
     * 分页获取指定课程作业列表（不含courseId、content、teacherId、createTime，含courseName和teacherName）
     */
    public Map<String, Object> getHomeworkByCourseIdPaginated(Long courseId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        int total = homeworkMapper.countHomeworkByCourseId(courseId);
        List<Map<String, Object>> list = homeworkMapper.getHomeworkByCourseIdWithDetailsPaginated(courseId, offset, pageSize);

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

    public List<HomeworkSubmit> getSubmissionsByHomeworkId(Long homeworkId) {
        return submitMapper.getSubmissionsByHomeworkId(homeworkId);
    }

    public HomeworkSubmit submitHomework(Long homeworkId, String studentId, String content) {
        HomeworkSubmit existing = submitMapper.getSubmissionByHomeworkIdAndSid(homeworkId, studentId);
        if (existing != null) {
            existing.setContent(content);
            existing.setSubmitTime(LocalDateTime.now());
            existing.setStatus("未批改");
            submitMapper.updateSubmission(existing);
            return existing;
        }

        HomeworkSubmit submit = new HomeworkSubmit();
        submit.setHomeworkId(homeworkId);
        submit.setSid(studentId);
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