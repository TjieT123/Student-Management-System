package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.NotificationMapper;
import cn.edu.sdu.sms.server.models.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper mapper;

    public void createNotification(Long userId, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setIsRead(false);
        n.setCreateTime(LocalDateTime.now());
        mapper.insert(n);
    }

    public int getUnreadCount(Long userId) {
        return mapper.countUnread(userId);
    }

    public Map<String, Object> getNotifications(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        int total = mapper.countByUserId(userId);
        List<Notification> list = mapper.getByUserId(userId, offset, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    public void markRead(Long id) { mapper.markRead(id); }
    public void markAllRead(Long userId) { mapper.markAllRead(userId); }
}
