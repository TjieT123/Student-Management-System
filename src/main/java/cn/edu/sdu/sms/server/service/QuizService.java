package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.QuizMapper;
import cn.edu.sdu.sms.server.mapper.QuizQuestionMapper;
import cn.edu.sdu.sms.server.mapper.QuizSubmitMapper;
import cn.edu.sdu.sms.server.models.Quiz;
import cn.edu.sdu.sms.server.models.QuizQuestion;
import cn.edu.sdu.sms.server.models.QuizSubmit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    @Autowired
    private QuizMapper quizMapper;

    @Autowired
    private QuizQuestionMapper questionMapper;

    @Autowired
    private QuizSubmitMapper submitMapper;

    /**
     * Get all quizzes
     */
    public List<Quiz> getAllQuizzes() {
        return quizMapper.getAllQuizzes();
    }

    /**
     * Get quiz by ID with questions
     */
    public Map<String, Object> getQuizWithQuestions(Long id) {
        Quiz quiz = quizMapper.getQuizById(id);
        if (quiz == null) {
            return null;
        }

        List<QuizQuestion> questions = questionMapper.getQuestionsByQuizId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("quiz", quiz);
        result.put("questions", questions);

        return result;
    }

    /**
     * Get quizzes by course ID
     */
    public List<Quiz> getQuizzesByCourseId(Long courseId) {
        return quizMapper.getQuizzesByCourseId(courseId);
    }

    /**
     * Get quizzes by teacher ID
     */
    public List<Quiz> getQuizzesByTeacherId(String teacherId) {
        return quizMapper.getQuizzesByTeacherId(teacherId);
    }

    /**
     * Create new quiz
     */
    public Quiz createQuiz(Long courseId, String title, String description, Integer duration,
                          String teacherId, LocalDateTime startTime, LocalDateTime endTime) {
        Quiz quiz = new Quiz();
        quiz.setCourseId(courseId);
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setDuration(duration);
        quiz.setTeacherId(teacherId);
        quiz.setStartTime(startTime);
        quiz.setEndTime(endTime);
        quiz.setCreateTime(LocalDateTime.now());
        quiz.setTotalQuestions(0);

        quizMapper.insertQuiz(quiz);
        return quiz;
    }

    /**
     * Add question to quiz
     */
    public int addQuestion(Long quizId, String questionText, String questionType,
                          String optionA, String optionB, String optionC, String optionD,
                          String correctAnswer, Integer points, Integer questionOrder) {
        QuizQuestion question = new QuizQuestion();
        question.setQuizId(quizId);
        question.setQuestionText(questionText);
        question.setQuestionType(questionType);
        question.setOptionA(optionA);
        question.setOptionB(optionB);
        question.setOptionC(optionC);
        question.setOptionD(optionD);
        question.setCorrectAnswer(correctAnswer);
        question.setPoints(points);
        question.setQuestionOrder(questionOrder);

        return questionMapper.insertQuestion(question);
    }

    /**
     * Submit quiz
     */
    public int submitQuiz(Long quizId, String sid, String answers) {
        QuizSubmit submit = new QuizSubmit();
        submit.setQuizId(quizId);
        submit.setSid(sid);
        submit.setAnswers(answers);
        submit.setStatus("SUBMITTED");
        submit.setSubmitTime(LocalDateTime.now());

        return submitMapper.insertSubmit(submit);
    }

    /**
     * Get quiz submissions
     */
    public List<QuizSubmit> getQuizSubmissions(Long quizId) {
        return submitMapper.getSubmitsByQuizId(quizId);
    }

    /**
     * Get student's quiz submission
     */
    public QuizSubmit getStudentQuizSubmission(Long quizId, String sid) {
        return submitMapper.getSubmitByQuizIdAndSid(quizId, sid);
    }

    /**
     * Grade quiz submission
     */
    public int gradeQuizSubmission(Long submitId, Integer score) {
        QuizSubmit submit = submitMapper.getSubmitById(submitId);
        if (submit != null) {
            submit.setScore(score);
            submit.setStatus("GRADED");
            return submitMapper.updateSubmit(submit);
        }
        return 0;
    }

    /**
     * Delete quiz (also deletes questions and submissions)
     */
    public int deleteQuiz(Long id) {
        questionMapper.deleteQuestionsByQuizId(id);
        submitMapper.deleteSubmitsByQuizId(id);
        return quizMapper.deleteQuiz(id);
    }
}


