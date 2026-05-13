package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.HomeworkMapper;
import cn.edu.sdu.sms.server.mapper.HomeworkSubmitMapper;
import cn.edu.sdu.sms.server.models.Homework;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
