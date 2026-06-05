package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.CourseScoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CourseScoreService {

    @Autowired
    private CourseScoreMapper mapper;

    private static final double[][] GPA_MAPPING = {
        {90, 100, 4.0}, {85, 89, 3.7}, {82, 84, 3.3}, {78, 81, 3.0},
        {75, 77, 2.7}, {72, 74, 2.3}, {68, 71, 2.0}, {64, 67, 1.5},
        {60, 63, 1.0}, {0, 59, 0.0}
    };

    private double scoreToGradePoint(double score) {
        for (double[] mapping : GPA_MAPPING) {
            if (score >= mapping[0] && score <= mapping[1]) return mapping[2];
        }
        return 0.0;
    }

    public void upsertScore(Long courseId, String sid, Double finalScore) {
        double gp = scoreToGradePoint(finalScore);
        mapper.upsertScore(courseId, sid, finalScore, gp);
    }

    public void batchUpsertScores(Long courseId, List<Map<String, Object>> scores) {
        for (Map<String, Object> s : scores) {
            String sid = (String) s.get("sid");
            Double score = Double.parseDouble(s.get("finalScore").toString());
            upsertScore(courseId, sid, score);
        }
    }

    public List<Map<String, Object>> getStudentScores(String sid) {
        return mapper.getStudentAllScores(sid);
    }

    public List<Map<String, Object>> getCourseScores(Long courseId) {
        return mapper.getCourseAllScores(courseId);
    }

    public Map<String, Object> calculateGPA(String sid) {
        List<Map<String, Object>> scores = mapper.getStudentAllScores(sid);
        double totalWeighted = 0, totalCredits = 0;
        int completed = 0;
        for (Map<String, Object> s : scores) {
            Object scoreObj = s.get("finalScore");
            if (scoreObj == null) continue;
            Object creditsObj = s.get("credits");
            double credits = creditsObj != null ? ((Number) creditsObj).doubleValue() : 3.0;
            double gp = scoreToGradePoint(((Number) scoreObj).doubleValue());
            totalWeighted += credits * gp;
            totalCredits += credits;
            completed++;
        }
        double gpa = totalCredits > 0 ? totalWeighted / totalCredits : 0.0;
        Map<String, Object> result = new HashMap<>();
        result.put("gpa", Math.round(gpa * 100.0) / 100.0);
        result.put("totalCredits", totalCredits);
        result.put("completedCourses", completed);
        return result;
    }
}
