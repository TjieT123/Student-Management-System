package cn.edu.sdu.sms.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String role;
    private String phone;
    private String sch_id;
    private String major;
    private String gender;
    private Integer s_class;
    private Integer grade;
    // 学生扩展字段
    private String idCard;
    private String birthDate;
    private Integer enrollmentYear;
    private String nativePlace;
    private String politicalStatus;
    private String address;
    private String contactName;
    private String contactPhone;
    private String socialRelations;
}
