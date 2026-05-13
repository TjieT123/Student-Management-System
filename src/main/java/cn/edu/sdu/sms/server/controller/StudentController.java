package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {

    @Autowired
    private StudentMapper studentMapper;

    @GetMapping("/getAll")
    public Object getAllStudents() {
        return studentMapper.getAllStudents();
    }
}
