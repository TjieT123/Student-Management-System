package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.DiscussionReply;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscussionReplyMapper {

    @Select("select * from discussion_reply where discussion_id = #{discussionId} order by create_time asc")
    List<DiscussionReply> getRepliesByDiscussionId(Long discussionId);

    @Select("select * from discussion_reply where id = #{id}")
    DiscussionReply getReplyById(Long id);

    @Insert("insert into discussion_reply(discussion_id, content, author_id, author_name, create_time) " +
            "values(#{discussionId}, #{content}, #{authorId}, #{authorName}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertReply(DiscussionReply reply);

    @Update("update discussion_reply set content = #{content}, author_id = #{authorId}, " +
            "author_name = #{authorName}, create_time = #{createTime} where id = #{id}")
    int updateReply(DiscussionReply reply);

    @Delete("delete from discussion_reply where id = #{id}")
    int deleteReply(Long id);

    @Delete("delete from discussion_reply where discussion_id = #{discussionId}")
    int deleteRepliesByDiscussionId(Long discussionId);
}

