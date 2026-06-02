package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.dto.RegisterRequest;
import cn.edu.sdu.sms.server.models.User;
import cn.edu.sdu.sms.server.service.AuthService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口，提供用户注册、登录、Token刷新和密码修改功能。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 登录
     */
    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return Result.error(400, "Username and password are required");
        }

        Map<String, Object> loginResult = authService.login(username, password);
        if (loginResult == null) {
            return Result.error(401, "Invalid username or password");
        }

        return Result.success(loginResult, "Login successful");
    }

    /**
     * 注册（自动创建关联的 student 或 teacher 记录）
     */
    @PostMapping("/register")
    public ResponseEntity<Result> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getPassword() == null
                || req.getName() == null || req.getRole() == null || req.getSch_id() == null) {
            return Result.error(400, "Username, password, name, role, and sch_id are required");
        }

        User user = authService.register(req);
        if (user == null) {
            return Result.error(409, "Username already exists");
        }

        return Result.success(user, "Registration successful");
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Result> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return Result.error(400, "Refresh token is required");
        }

        String newToken = authService.refreshToken(refreshToken);
        if (newToken == null) {
            return Result.error(401, "Invalid or expired refresh token");
        }

        Map<String, String> response = new HashMap<>();
        response.put("token", newToken);

        return Result.success(response, "Token refreshed");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<Result> changePassword(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error(400, "oldPassword and newPassword are required");
        }

        Long result = authService.changePassword(userId, oldPassword, newPassword);
        if (result == null) {
            return Result.error(400, "Invalid old password or user not found");
        }

        return Result.success(null, "Password changed successfully");
    }

    /**
     * 从HTTP请求中提取Bearer Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

