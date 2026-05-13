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
     * Get all announcements (ordered by publish time descending)
     */
    public List<Announcement> getAllAnnouncements() {
        return announcementMapper.getAllAnnouncements();
    }

    /**
     * Get announcement by ID
     */
    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.getAnnouncementById(id);
    }

    /**
     * Publish new announcement
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
     * Delete announcement
     */
    public int deleteAnnouncement(Long id) {
        return announcementMapper.deleteAnnouncement(id);
    }
}

