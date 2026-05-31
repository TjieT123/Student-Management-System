package cn.edu.sdu.sms.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiGradeResult {
    private Integer score;
    private String comment;
    private String highlights;
    private String suggestions;
}
