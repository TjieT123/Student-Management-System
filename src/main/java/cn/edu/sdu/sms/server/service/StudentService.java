package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.StudentMapperEnhanced;
import cn.edu.sdu.sms.server.models.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentMapperEnhanced studentMapper;

    public List<Student> getAllStudents() {
        return studentMapper.getAllStudents();
    }

    public Student getStudentBySid(String sid) {
        return studentMapper.getStudentBySid(sid);
    }

    public int insertStudent(Student student) {
        return studentMapper.insertStudent(student);
    }

    public int updateStudent(Student student) {
        return studentMapper.updateStudent(student);
    }

    public int deleteStudent(String sid) {
        return studentMapper.deleteStudent(sid);
    }
}
