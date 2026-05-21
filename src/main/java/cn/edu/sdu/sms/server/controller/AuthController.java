package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.User;
import cn.edu.sdu.sms.server.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口，提供用户注册、登录和Token刷新功能。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

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
     * 注册
     */
    @PostMapping("/register")
    public ResponseEntity<Result> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String name = request.get("name");
        String role = request.get("role");
        String phone = request.get("phone");
        String schId = request.get("sch_id");

        if (username == null || password == null || name == null || role == null || schId == null) {
            return Result.error(400, "Username, password, name, role, and sch_id are required");
        }

        User user = authService.register(username, password, name, role, phone, schId);
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
}

