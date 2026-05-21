package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.StudentMapperEnhanced;
import cn.edu.sdu.sms.server.models.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    @Autowired
    private StudentMapperEnhanced studentMapper;

    public List<Student> getAllStudents() {
        return studentMapper.getAllStudents();
    }

    /**
     * 分页获取学生列表
     */
    public Map<String, Object> getStudentsPaginated(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        int total = studentMapper.countStudents();
        List<Student> list = studentMapper.getAllStudentsPaginated(offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
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
