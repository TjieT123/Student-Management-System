package cn.edu.sdu.sms.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Discussion {
    private Long id;
    private String title;
    private String content;
    private String authorId;  // Author ID (User ID / Student ID)
    private String authorName;  // Author name (for display)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer replyCount;  // Number of replies
}

