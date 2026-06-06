package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.HonorMapper;
import cn.edu.sdu.sms.server.models.Honor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HonorService {
    @Autowired private HonorMapper mapper;

    public Map<String, Object> getHonorList(String sid, String name, int page, int pageSize) {
        int offset = (page-1)*pageSize;
        Map<String, Object> r = new HashMap<>();
        r.put("total", mapper.count(sid, name));
        r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", mapper.selectAll(sid, name, offset, pageSize));
        return r;
    }

    public Map<String, Object> getByIdWithName(Long id) { return mapper.getByIdWithName(id); }

    @Autowired private cn.edu.sdu.sms.server.mapper.StudentMapperEnhanced studentMapper;
    public Honor addHonor(Honor h) {
        if (h.getSid() == null || h.getSid().isEmpty()) throw new RuntimeException("学号不能为空");
        if (studentMapper.getStudentBySid(h.getSid()) == null) throw new RuntimeException("该学号对应的学生不存在");
        mapper.insert(h); return h;
    }
    public Honor updateHonor(Honor h) { mapper.update(h); return h; }
    public void deleteHonor(Long id) { mapper.delete(id); }
    public List<Map<String, Object>> getBySidWithName(String sid) { return mapper.getBySidWithName(sid); }
    public List<Honor> getBySid(String sid) { return mapper.getBySid(sid); }
}
