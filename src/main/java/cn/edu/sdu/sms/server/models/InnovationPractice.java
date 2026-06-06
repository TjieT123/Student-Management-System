package cn.edu.sdu.sms.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InnovationPractice {
    private Long id;
    private String sid;
    private String type;
    private String title;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String organization;
    private String role;
    private String description;
    private String result;
    private String status;
    private String attachments;
    private Long reviewerId;
    private LocalDateTime reviewTime;
    private String reviewComment;
    private LocalDateTime createTime;
}
