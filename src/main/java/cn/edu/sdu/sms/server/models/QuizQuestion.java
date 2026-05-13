package cn.edu.sdu.sms.server.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestion {
    private Long id;
    private Long quizId;
    private String questionText;
    private String questionType;  // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private Integer points;
    private Integer questionOrder;  // Order in quiz
}

