package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.ActivityMapper;
import cn.edu.sdu.sms.server.models.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ActivityService {
    @Autowired private ActivityMapper mapper;

    public Activity create(Activity a) {
        a.setCreateTime(LocalDateTime.now());
        mapper.insert(a);
        return a;
    }

    public Map<String, Object> getAllWithCount(int page, int pageSize) {
        int offset = (page-1)*pageSize;
        Map<String, Object> r = new HashMap<>();
        r.put("total", mapper.countAll()); r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", mapper.selectAllWithCount(offset, pageSize));
        return r;
    }

    public Map<String, Object> getAvailableForStudent(String sid, int page, int pageSize) {
        int offset = (page-1)*pageSize;
        List<Map<String, Object>> all = mapper.selectAllWithCount(offset, pageSize);
        // Filter: only show future activities
        List<Map<String, Object>> available = new ArrayList<>();
        for (Map<String, Object> a : all) {
            Object d = a.get("date");
            if (d != null) {
                try {
                    LocalDate ld = d instanceof LocalDate ? (LocalDate) d : LocalDate.parse(d.toString());
                    if (ld.isBefore(LocalDate.now())) continue; // skip past activities
                } catch (Exception ignored) { continue; }
            }
            Object rc = a.get("registered_count");
            int regCount = rc != null ? ((Number) rc).intValue() : 0;
            Object mp = a.get("maxParticipants");
            int max = mp != null ? ((Number) mp).intValue() : 0;
            a.put("isRegistered", mapper.isRegistered(((Number)a.get("id")).longValue(), sid) > 0);
            a.put("isFull", max > 0 && regCount >= max);
            available.add(a);
        }
        Map<String, Object> r = new HashMap<>();
        r.put("total", available.size()); r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", available);
        return r;
    }

    public void register(Long activityId, String sid) {
        Activity a = mapper.getById(activityId);
        if (a == null) throw new RuntimeException("活动不存在");
        if (a.getDate() != null && a.getDate().isBefore(LocalDate.now())) throw new RuntimeException("活动已过期，无法报名");
        if (mapper.isRegistered(activityId, sid) > 0) throw new RuntimeException("已报名，不能重复报名");
        if (a.getMaxParticipants() != null && a.getMaxParticipants() > 0) {
            int count = mapper.countRegistrations(activityId);
            if (count >= a.getMaxParticipants()) throw new RuntimeException("报名人数已满");
        }
        mapper.register(activityId, sid);
    }

    public void cancelRegistration(Long activityId, String sid) {
        mapper.cancelRegistration(activityId, sid);
    }

    public Activity getById(Long id) { return mapper.getById(id); }
    public void delete(Long id) { mapper.delete(id); }
    public Activity update(Activity a) { mapper.update(a); return a; }
}
