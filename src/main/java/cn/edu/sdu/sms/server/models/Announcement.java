package cn.edu.sdu.sms.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Announcement {
    private Long id;
    private String title;
    private String content;
    private String publishBy;  // Publisher ID (User ID)
    private String publisherName;  // Publisher name (for display)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}

