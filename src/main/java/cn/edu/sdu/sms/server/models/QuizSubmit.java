package cn.edu.sdu.sms.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizSubmit {
    private Long id;
    private Long quizId;
    private String sid;  // Student ID
    private String answers;  // Stored as JSON string
    private Integer score;
    private String status;  // SUBMITTED, GRADED, AUTO_SUBMITTED
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
}

