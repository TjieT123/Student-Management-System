package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.DiscussionMapper;
import cn.edu.sdu.sms.server.mapper.DiscussionReplyMapper;
import cn.edu.sdu.sms.server.models.Discussion;
import cn.edu.sdu.sms.server.models.DiscussionReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscussionService {

    @Autowired
    private DiscussionMapper discussionMapper;

    @Autowired
    private DiscussionReplyMapper replyMapper;

    /**
     * Get all discussions (ordered by create time descending)
     */
    public List<Discussion> getAllDiscussions() {
        return discussionMapper.getAllDiscussions();
    }

    /**
     * Get discussion by ID with replies
     */
    public Map<String, Object> getDiscussionWithReplies(Long id) {
        Discussion discussion = discussionMapper.getDiscussionById(id);
        if (discussion == null) {
            return null;
        }

        List<DiscussionReply> replies = replyMapper.getRepliesByDiscussionId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("discussion", discussion);
        result.put("replies", replies);

        return result;
    }

    /**
     * Publish new discussion
     */
    public int publishDiscussion(String title, String content, String authorId, String authorName) {
        Discussion discussion = new Discussion();
        discussion.setTitle(title);
        discussion.setContent(content);
        discussion.setAuthorId(authorId);
        discussion.setAuthorName(authorName);
        discussion.setCreateTime(LocalDateTime.now());
        discussion.setReplyCount(0);

        return discussionMapper.insertDiscussion(discussion);
    }

    /**
     * Reply to discussion
     */
    public int replyDiscussion(Long discussionId, String content, String authorId, String authorName) {
        DiscussionReply reply = new DiscussionReply();
        reply.setDiscussionId(discussionId);
        reply.setContent(content);
        reply.setAuthorId(authorId);
        reply.setAuthorName(authorName);
        reply.setCreateTime(LocalDateTime.now());

        int result = replyMapper.insertReply(reply);
        if (result > 0) {
            discussionMapper.incrementReplyCount(discussionId);
        }

        return result;
    }

    /**
     * Delete reply
     */
    public int deleteReply(Long replyId, Long discussionId) {
        int result = replyMapper.deleteReply(replyId);
        if (result > 0) {
            discussionMapper.decrementReplyCount(discussionId);
        }
        return result;
    }

    /**
     * Delete discussion (also deletes all replies)
     */
    public int deleteDiscussion(Long id) {
        replyMapper.deleteRepliesByDiscussionId(id);
        return discussionMapper.deleteDiscussion(id);
    }
}

