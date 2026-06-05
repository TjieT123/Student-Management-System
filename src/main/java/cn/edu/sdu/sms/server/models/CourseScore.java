package cn.edu.sdu.sms.server.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseScore {
    private Long id;
    private Long courseId;
    private String sid;
    private Double finalScore;
    private Double gradePoint;
    private LocalDateTime updateTime;
}
