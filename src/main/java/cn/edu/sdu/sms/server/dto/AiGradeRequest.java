package cn.edu.sdu.sms.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiGradeRequest {
    private Long submitId;
    private Long homeworkId;
    private String homeworkTitle;
    private String homeworkContent;
}
