package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Quiz;
import cn.edu.sdu.sms.server.models.QuizSubmit;
import cn.edu.sdu.sms.server.service.QuizService;
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
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/list")
    public ResponseEntity<Result> getAllQuizzes() {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        return Result.success(quizzes, "Quizzes retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result> getQuizWithQuestions(@PathVariable Long id) {
        Map<String, Object> result = quizService.getQuizWithQuestions(id);
        if (result == null) {
            return Result.error(404, "Quiz not found");
        }
        return Result.success(result, "Quiz retrieved");
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Result> getQuizzesByCourseId(@PathVariable Long courseId) {
        List<Quiz> quizzes = quizService.getQuizzesByCourseId(courseId);
        return Result.success(quizzes, "Course quizzes retrieved");
    }

    @PostMapping("/create")
    public ResponseEntity<Result> createQuiz(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        String teacherId = jwtTokenProvider.getUserIdFromToken(token);
        Long courseId = Long.parseLong(request.get("courseId").toString());
        String title = (String) request.get("title");
        String description = (String) request.get("description");
        Integer duration = request.get("duration") != null ? Integer.parseInt(request.get("duration").toString()) : null;

        if (courseId == null || title == null || duration == null) {
            return Result.error(400, "Course ID, title, and duration are required");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (request.get("startTime") != null) {
            startTime = LocalDateTime.parse(request.get("startTime").toString(), fmt);
        }
        if (request.get("endTime") != null) {
            endTime = LocalDateTime.parse(request.get("endTime").toString(), fmt);
        }

        Quiz quiz = quizService.createQuiz(courseId, title, description, duration, teacherId, startTime, endTime);
        return Result.success(quiz, "Quiz created successfully");
    }

    @PostMapping("/{id}/question/add")
    public ResponseEntity<Result> addQuestion(@PathVariable Long id, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        String questionText = (String) request.get("questionText");
        String questionType = (String) request.get("questionType");
        String optionA = (String) request.get("optionA");
        String optionB = (String) request.get("optionB");
        String optionC = (String) request.get("optionC");
        String optionD = (String) request.get("optionD");
        String correctAnswer = (String) request.get("correctAnswer");
        Integer points = request.get("points") != null ? Integer.parseInt(request.get("points").toString()) : 0;
        Integer questionOrder = request.get("questionOrder") != null ? Integer.parseInt(request.get("questionOrder").toString()) : 0;

        if (questionText == null || questionType == null || correctAnswer == null) {
            return Result.error(400, "Question text, type, and correct answer are required");
        }

        int result = quizService.addQuestion(id, questionText, questionType, optionA, optionB, optionC, optionD, correctAnswer, points, questionOrder);
        if (result > 0) {
            return Result.success(null, "Question added successfully");
        }
        return Result.error(500, "Failed to add question");
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Result> submitQuiz(@PathVariable Long id, @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        String studentId = jwtTokenProvider.getUserIdFromToken(token);
        String answers = request.get("answers");

        if (answers == null) {
            return Result.error(400, "Answers are required");
        }

        int result = quizService.submitQuiz(id, studentId, answers);
        if (result > 0) {
            return Result.success(null, "Quiz submitted successfully");
        }
        return Result.error(500, "Failed to submit quiz");
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<Result> getQuizSubmissions(@PathVariable Long id) {
        List<QuizSubmit> submissions = quizService.getQuizSubmissions(id);
        return Result.success(submissions, "Submissions retrieved");
    }

    @GetMapping("/{id}/submission")
    public ResponseEntity<Result> getStudentQuizSubmission(@PathVariable Long id, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        String studentId = jwtTokenProvider.getUserIdFromToken(token);
        QuizSubmit submit = quizService.getStudentQuizSubmission(id, studentId);
        if (submit == null) {
            return Result.error(404, "Submission not found");
        }
        return Result.success(submit, "Submission retrieved");
    }

    @PostMapping("/submission/{submitId}/grade")
    public ResponseEntity<Result> gradeQuizSubmission(@PathVariable Long submitId, @RequestBody Map<String, Object> request) {
        Integer score = Integer.parseInt(request.get("score").toString());

        int result = quizService.gradeQuizSubmission(submitId, score);
        if (result > 0) {
            return Result.success(null, "Quiz graded successfully");
        }
        return Result.error(404, "Submission not found");
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<Result> deleteQuiz(@PathVariable Long id, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        int result = quizService.deleteQuiz(id);
        if (result > 0) {
            return Result.success(null, "Quiz deleted successfully");
        }
        return Result.error(404, "Quiz not found");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
