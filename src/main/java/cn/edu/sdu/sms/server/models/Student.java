package cn.edu.sdu.sms.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private String sid;
    private String name;
    private String major;
    private String gender;
    private Integer sClass;
    // 扩展字段
    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.util.Date birthDate;
    private Integer enrollmentYear;
    private String idCard;
    private String nativePlace;
    private String politicalStatus;
    private String address;
    private String contactName;
    private String contactPhone;
    private String socialRelations; // JSON
    private Integer grade; // 年级 (1990-2050)
}
