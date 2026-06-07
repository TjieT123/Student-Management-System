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

    public Map<String, Object> getAllWithCount(int page, int pageSize, String keyword) {
        int offset = (page-1)*pageSize;
        Map<String, Object> r = new HashMap<>();
        r.put("total", mapper.countAll(keyword)); r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", mapper.selectAllWithCount(offset, pageSize, keyword));
        return r;
    }

    public Map<String, Object> getAvailableForStudent(String sid, int page, int pageSize, String keyword) {
        int offset = (page-1)*pageSize;
        // Fetch more to allow filtering then paginate
        List<Map<String, Object>> all = mapper.selectAllWithCount(0, 500, null);
        List<Map<String, Object>> available = new ArrayList<>();
        for (Map<String, Object> a : all) {
            Object d = a.get("date");
            if (d != null) {
                try {
                    String ds = d.toString();
                    if (ds.length() >= 10) ds = ds.substring(0, 10);
                    LocalDate ld = LocalDate.parse(ds);
                    if (ld.isBefore(LocalDate.now())) continue;
                } catch (Exception ignored) { continue; }
            }
            if (keyword != null && !keyword.isEmpty()) {
                String title = (String) a.get("title");
                String loc = (String) a.get("location");
                boolean match = (title != null && title.toLowerCase().contains(keyword.toLowerCase()))
                             || (loc != null && loc.toLowerCase().contains(keyword.toLowerCase()));
                if (!match) continue;
            }
            Object rc = a.get("registered_count");
            Object mp = a.get("maxParticipants");
            int max = mp != null ? ((Number) mp).intValue() : 0;
            a.put("isRegistered", mapper.isRegistered(((Number)a.get("id")).longValue(), sid) > 0);
            a.put("isFull", max > 0 && ((Number)rc).intValue() >= max);
            available.add(a);
        }
        int total = available.size();
        int from = Math.min(offset, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> r = new HashMap<>();
        r.put("total", total); r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", available.subList(from, to));
        return r;
    }

    public void register(Long activityId, String sid) {
        Activity a = mapper.getById(activityId);
        if (a == null) throw new RuntimeException("活动不存在");
        if (a.getDate() != null && a.getDate().isBefore(LocalDateTime.now())) throw new RuntimeException("活动已过期，无法报名");
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
    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id) {
        mapper.deleteRegistrationsByActivityId(id);
        mapper.delete(id);
    }
    public Activity update(Activity a) { mapper.update(a); return a; }
}
