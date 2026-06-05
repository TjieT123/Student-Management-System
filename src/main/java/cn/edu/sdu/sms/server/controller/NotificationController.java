package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.service.NotificationService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/list")
    public ResponseEntity<Result> getList(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        return Result.success(notificationService.getNotifications(userId, page, pageSize), "ok");
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Result> getUnreadCount(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        Map<String, Object> data = new HashMap<>();
        data.put("count", notificationService.getUnreadCount(userId));
        return Result.success(data, "ok");
    }

    @PostMapping("/read/{id}")
    public ResponseEntity<Result> markRead(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        notificationService.markRead(id);
        return Result.success(null, "ok");
    }

    @PostMapping("/read-all")
    public ResponseEntity<Result> markAllRead(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) return Result.error(401, "Unauthorized");
        Long userId = Long.parseLong(jwtTokenProvider.getUserIdFromToken(token));
        notificationService.markAllRead(userId);
        return Result.success(null, "ok");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
