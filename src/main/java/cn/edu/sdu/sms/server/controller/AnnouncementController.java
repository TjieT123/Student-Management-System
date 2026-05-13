package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Announcement;
import cn.edu.sdu.sms.server.service.AnnouncementService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Get all announcements
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        return Result.success(announcements, "Announcements retrieved successfully");
    }

    /**
     * Get announcement by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return Result.error(404, "Announcement not found");
        }
        return Result.success(announcement, "Announcement retrieved");
    }

    /**
     * Publish new announcement (Admin only)
     */
    @PostMapping("/publish")
    public ResponseEntity<Result> publishAnnouncement(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        String publisherId = jwtTokenProvider.getUserIdFromToken(token);
        String publisherName = request.get("publisherName");
        String title = request.get("title");
        String content = request.get("content");

        if (title == null || content == null) {
            return Result.error(400, "Title and content are required");
        }

        int result = announcementService.publishAnnouncement(title, content, publisherId, publisherName);
        if (result > 0) {
            return Result.success(null, "Announcement published successfully");
        }

        return Result.error(500, "Failed to publish announcement");
    }

    /**
     * Delete announcement (Admin only)
     */
    @PostMapping("/delete/{id}")
    public ResponseEntity<Result> deleteAnnouncement(@PathVariable Long id, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return Result.error(404, "Announcement not found");
        }

        announcementService.deleteAnnouncement(id);

        return Result.success(null, "Announcement deleted successfully");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

