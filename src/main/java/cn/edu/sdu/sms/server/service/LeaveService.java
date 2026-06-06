package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.LeaveRequestMapper;
import cn.edu.sdu.sms.server.models.LeaveRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LeaveService {
    @Autowired private LeaveRequestMapper mapper;
    @Autowired private cn.edu.sdu.sms.server.mapper.UserMapper userMapper;
    @Autowired(required = false) private NotificationService notificationService;

    public LeaveRequest apply(LeaveRequest req) {
        req.setStatus("PENDING");
        req.setCreateTime(LocalDateTime.now());
        mapper.insert(req);
        return req;
    }
    public Map<String, Object> getMyLeaves(String sid, int page, int pageSize) {
        int offset = (page-1)*pageSize;
        Map<String, Object> r = new HashMap<>();
        r.put("total", mapper.countBySid(sid)); r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", mapper.getBySid(sid, offset, pageSize));
        return r;
    }
    public Map<String, Object> getPending(int page, int pageSize, String name, String type, String status) {
        int offset = (page-1)*pageSize;
        Map<String, Object> r = new HashMap<>();
        r.put("total", mapper.countAll(name, type, status)); r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", mapper.getAllWithName(offset, pageSize, name, type, status));
        return r;
    }
    public LeaveRequest getById(Long id) { return mapper.getById(id); }
    public void update(LeaveRequest req) { mapper.update(req); }
    public void approve(Long id, String status, Long reviewerId, String comment) {
        mapper.approve(id, status, reviewerId, comment);
        LeaveRequest lr = mapper.getById(id);
        if (notificationService != null && lr != null) {
            cn.edu.sdu.sms.server.models.User user = userMapper.getUserBySchId(lr.getSid());
            if (user != null)
                notificationService.createNotification(user.getId(), "请假申请已" + (status.equals("APPROVED") ? "通过" : "驳回"),
                    "您的请假申请（" + lr.getStartDate() + "至" + lr.getEndDate() + "）已" + (status.equals("APPROVED") ? "通过" : "驳回") + "。");
        }
    }
    public void delete(Long id) { mapper.delete(id); }
}
