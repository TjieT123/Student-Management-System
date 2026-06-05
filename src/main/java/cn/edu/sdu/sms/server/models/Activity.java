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
public class Activity {
    private Long id;
    private String sid; // publisher (admin user id)
    private String title;
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String location;
    private String description;
    private String photos;
    private String content;
    private Integer maxParticipants;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
