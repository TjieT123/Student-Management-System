package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.AnnouncementMapper;
import cn.edu.sdu.sms.server.models.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    /**
     * 获取所有公告（按发布时间倒序）
     */
    public List<Announcement> getAllAnnouncements() {
        return announcementMapper.getAllAnnouncements();
    }

    /**
     * 分页获取公告列表
     */
    public Map<String, Object> getAnnouncementsPaginated(int page, int pageSize, Long id, String title) {
        int offset = (page - 1) * pageSize;
        int total = announcementMapper.countAnnouncements(id, title);
        List<Map<String, Object>> list = announcementMapper.getAllAnnouncementsPaginated(id, title, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    /**
     * 根据ID获取公告详情
     */
    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.getAnnouncementById(id);
    }

    /**
     * 发布新公告
     */
    public int publishAnnouncement(String title, String content, String publishBy, String publisherName) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setPublishBy(publishBy);
        announcement.setPublisherName(publisherName);
        announcement.setPublishTime(LocalDateTime.now());

        return announcementMapper.insertAnnouncement(announcement);
    }

    /**
     * Update announcement
     */
    public int updateAnnouncement(Announcement announcement) {
        return announcementMapper.updateAnnouncement(announcement);
    }

    /**
     * 选择性更新公告
     */
    public Announcement updateAnnouncementSelective(Long id, String title, String content, String publisherName) {
        Announcement announcement = announcementMapper.getAnnouncementById(id);
        if (announcement == null) return null;

        if (title != null) announcement.setTitle(title);
        if (content != null) announcement.setContent(content);
        if (publisherName != null) announcement.setPublisherName(publisherName);

        announcementMapper.updateAnnouncementSelective(announcement);
        return announcementMapper.getAnnouncementById(id);
    }

    /**
     * 删除公告
     */
    public int deleteAnnouncement(Long id) {
        return announcementMapper.deleteAnnouncement(id);
    }
}

