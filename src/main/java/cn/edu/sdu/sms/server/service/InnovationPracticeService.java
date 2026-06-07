package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.InnovationPracticeMapper;
import cn.edu.sdu.sms.server.models.InnovationPractice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class InnovationPracticeService {
    @Autowired private InnovationPracticeMapper mapper;
    @Autowired private cn.edu.sdu.sms.server.mapper.UserMapper userMapper;

    public InnovationPractice submit(InnovationPractice p) {
        p.setStatus("PENDING");
        p.setCreateTime(LocalDateTime.now());
        mapper.insert(p);
        return p;
    }
    public Map<String, Object> getMyPractices(String sid, int page, int pageSize) {
        int offset = (page-1)*pageSize;
        Map<String, Object> r = new HashMap<>();
        r.put("total", mapper.countBySid(sid));
        r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", mapper.getBySid(sid, offset, pageSize));
        return r;
    }
    public Map<String, Object> getPending(int page, int pageSize, String name, String title, String type, String status) {
        int offset = (page-1)*pageSize;
        Map<String, Object> r = new HashMap<>();
        r.put("total", mapper.countAll(name, title, type, status));
        r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", mapper.getAllWithName(offset, pageSize, name, title, type, status));
        return r;
    }
    public Map<String, Object> getByIdWithName(Long id) { return mapper.getByIdWithName(id); }
    public InnovationPractice getById(Long id) { return mapper.getById(id); }
    public void update(InnovationPractice p) { mapper.update(p); }
    public void approve(Long id, String status, Long reviewerId, String comment) {
        mapper.approve(id, status, reviewerId, comment);
    }
    public void delete(Long id) { mapper.delete(id); }
}
