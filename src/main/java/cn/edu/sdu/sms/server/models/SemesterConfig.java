package cn.edu.sdu.sms.server.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SemesterConfig {
    private Integer id;
    private String semesterName;
    private LocalDate startWeekDate;
    private Integer totalWeeks;
}
