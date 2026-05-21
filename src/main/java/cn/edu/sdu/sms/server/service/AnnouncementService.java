package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.AnnouncementMapper;
import cn.edu.sdu.sms.server.models.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
     * 删除公告
     */
    public int deleteAnnouncement(Long id) {
        return announcementMapper.deleteAnnouncement(id);
    }
}

