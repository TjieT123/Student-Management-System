package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.dto.RegisterRequest;
import cn.edu.sdu.sms.server.mapper.StudentMapperEnhanced;
import cn.edu.sdu.sms.server.mapper.TeacherMapper;
import cn.edu.sdu.sms.server.mapper.UserMapper;
import cn.edu.sdu.sms.server.models.Student;
import cn.edu.sdu.sms.server.models.Teacher;
import cn.edu.sdu.sms.server.models.User;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StudentMapperEnhanced studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    /**
     * User login
     */
    public Map<String, Object> login(String username, String password) {
        User user = userMapper.getUserByUsername(username);
//        System.out.println(password);
//        System.out.println("正确的密码为:" + userMapper.getUserByUsername(username).getPassword());
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        String token = jwtTokenProvider.generateToken(username, user.getId().toString(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(username, user.getId().toString());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);

        // 清除密码，不返回给前端
        user.setPassword(null);
        result.put("user", user);

        return result;
    }

    /**
     * User registration（自动创建关联的 student 或 teacher 记录）
     */
    public User register(RegisterRequest req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setName(req.getName());
        user.setRole(req.getRole());
        user.setPhone(req.getPhone());
        user.setSchId(req.getSch_id());

        userMapper.insertUser(user);

        if ("STUDENT".equals(req.getRole())) {
            Student student = new Student();
            student.setSid(req.getSch_id());
            student.setName(req.getName());
            student.setMajor(req.getMajor());
            student.setGender(req.getGender());
            if (req.getS_class() != null) {
                student.setSClass(req.getS_class());
            }
            if (req.getGrade() != null) {
                student.setGrade(req.getGrade());
            }
            // 扩展字段
            if (req.getIdCard() != null) student.setIdCard(req.getIdCard());
            if (req.getBirthDate() != null) {
                try { student.setBirthDate(java.sql.Date.valueOf(java.time.LocalDate.parse(req.getBirthDate()))); } catch (Exception ignored) {}
            }
            if (req.getEnrollmentYear() != null) student.setEnrollmentYear(req.getEnrollmentYear());
            if (req.getNativePlace() != null) student.setNativePlace(req.getNativePlace());
            if (req.getPoliticalStatus() != null) student.setPoliticalStatus(req.getPoliticalStatus());
            if (req.getAddress() != null) student.setAddress(req.getAddress());
            if (req.getContactName() != null) student.setContactName(req.getContactName());
            if (req.getContactPhone() != null) student.setContactPhone(req.getContactPhone());
            if (req.getSocialRelations() != null) student.setSocialRelations(req.getSocialRelations());
            studentMapper.insertStudent(student);
        } else if ("TEACHER".equals(req.getRole())) {
            Teacher teacher = new Teacher();
            teacher.setSchId(req.getSch_id());
            teacher.setName(req.getName());
            teacherMapper.insertTeacher(teacher);
        }

        return user;
    }

    /**
     * Refresh token
     */
    public String refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return null;
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);

        return jwtTokenProvider.generateToken(username, userId, role);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userMapper.getUserById(id);
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    public User getUserBySchId(String schId) {
        return userMapper.getUserBySchId(schId);
    }

    /**
     * Update user
     */
    public int updateUser(User user) {
        return userMapper.updateUser(user);
    }

    /**
     * Delete user
     */
    public int deleteUser(Long id) {
        return userMapper.deleteUser(id);
    }

    /**
     * Change password — returns null on failure, user id on success
     */
    public Long changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.getUserById(userId);
        if (user == null) return null;
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) return null;
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateUser(user);
        return userId;
    }
}

