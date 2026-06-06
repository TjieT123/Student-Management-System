package cn.edu.sdu.sms.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Honor {
    private Long id;
    private String sid;
    private String title;
    private String type;
    private String level;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate awardDate;
    private String description;
}
