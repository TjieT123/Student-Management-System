package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.models.User;
import cn.edu.sdu.sms.server.mapper.UserMapper;
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
        result.put("user", user);

        return result;
    }

    /**
     * User registration
     */
    public User register(String username, String password, String name, String role, String phone, String schId) {
        // Check if user exists
        if (userMapper.getUserByUsername(username) != null) {
            return null;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(role);
        user.setPhone(phone);
        user.setSchId(schId);

        userMapper.insertUser(user);
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
}

