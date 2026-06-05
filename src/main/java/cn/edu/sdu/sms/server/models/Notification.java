package cn.edu.sdu.sms.server.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
}
