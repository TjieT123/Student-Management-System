package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Discussion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscussionMapper {

    @Select("select * from discussion order by create_time desc")
    List<Discussion> getAllDiscussions();

    @Select("select * from discussion where id = #{id}")
    Discussion getDiscussionById(Long id);

    @Insert("insert into discussion(title, content, author_id, author_name, create_time, reply_count) " +
            "values(#{title}, #{content}, #{authorId}, #{authorName}, #{createTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDiscussion(Discussion discussion);

    @Update("update discussion set title = #{title}, content = #{content}, author_id = #{authorId}, " +
            "author_name = #{authorName}, create_time = #{createTime}, reply_count = #{replyCount} where id = #{id}")
    int updateDiscussion(Discussion discussion);

    @Delete("delete from discussion where id = #{id}")
    int deleteDiscussion(Long id);

    @Update("update discussion set reply_count = reply_count + 1 where id = #{discussionId}")
    int incrementReplyCount(Long discussionId);

    @Update("update discussion set reply_count = reply_count - 1 where id = #{discussionId}")
    int decrementReplyCount(Long discussionId);
}

