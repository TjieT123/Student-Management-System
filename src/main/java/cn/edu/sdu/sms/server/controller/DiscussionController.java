package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Discussion;
import cn.edu.sdu.sms.server.service.DiscussionService;
import cn.edu.sdu.sms.server.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussion")
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Get all discussions
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getAllDiscussions() {
        List<Discussion> discussions = discussionService.getAllDiscussions();
        return Result.success(discussions, "Discussions retrieved successfully");
    }

    /**
     * Get discussion with replies
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result> getDiscussionWithReplies(@PathVariable Long id) {
        Map<String, Object> result = discussionService.getDiscussionWithReplies(id);
        if (result == null) {
            return Result.error(404, "Discussion not found");
        }
        return Result.success(result, "Discussion retrieved");
    }

    /**
     * Publish new discussion
     */
    @PostMapping("/publish")
    public ResponseEntity<Result> publishDiscussion(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        String authorId = jwtTokenProvider.getUserIdFromToken(token);
        String authorName = request.get("authorName");
        String title = request.get("title");
        String content = request.get("content");

        if (title == null || content == null) {
            return Result.error(400, "Title and content are required");
        }

        int result = discussionService.publishDiscussion(title, content, authorId, authorName);
        if (result > 0) {
            return Result.success(null, "Discussion published successfully");
        }

        return Result.error(500, "Failed to publish discussion");
    }

    /**
     * Reply to discussion
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<Result> replyDiscussion(@PathVariable Long id, @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        String authorId = jwtTokenProvider.getUserIdFromToken(token);
        String authorName = request.get("authorName");
        String content = request.get("content");

        if (content == null) {
            return Result.error(400, "Content is required");
        }

        int result = discussionService.replyDiscussion(id, content, authorId, authorName);
        if (result > 0) {
            return Result.success(null, "Reply posted successfully");
        }

        return Result.error(500, "Failed to post reply");
    }

    /**
     * Delete discussion
     */
    @PostMapping("/{id}/delete")
    public ResponseEntity<Result> deleteDiscussion(@PathVariable Long id, HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "Unauthorized");
        }

        Map<String, Object> discussion = discussionService.getDiscussionWithReplies(id);
        if (discussion == null) {
            return Result.error(404, "Discussion not found");
        }

        discussionService.deleteDiscussion(id);

        return Result.success(null, "Discussion deleted successfully");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

