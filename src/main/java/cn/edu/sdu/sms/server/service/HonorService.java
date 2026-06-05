package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.HonorMapper;
import cn.edu.sdu.sms.server.models.Honor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class HonorService {
    @Autowired private HonorMapper mapper;
    public Map<String, Object> getHonorList(String sid, int page, int pageSize) {
        int offset = (page-1)*pageSize;
        Map<String, Object> r = new HashMap<>();
        r.put("total", mapper.count(sid));
        r.put("page", page); r.put("pageSize", pageSize);
        r.put("list", mapper.selectAll(sid, offset, pageSize));
        return r;
    }
    public Honor addHonor(Honor h) { mapper.insert(h); return h; }
    public Honor updateHonor(Honor h) { mapper.update(h); return h; }
    public void deleteHonor(Long id) { mapper.delete(id); }
    public List<Honor> getBySid(String sid) { return mapper.getBySid(sid); }
}
