package cn.edu.sdu.sms.server.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    private Long id;
    private String courseName;
    private String detail;
    private String address;
    private String teacherId;
    private String type = "REQUIRED";
    private Integer startWeek;
    private Integer endWeek;
    private String schedule;
    private String materials;
    private Double credits = 3.0;
}

