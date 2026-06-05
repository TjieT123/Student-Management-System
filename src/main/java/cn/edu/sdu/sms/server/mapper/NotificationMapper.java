package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Notification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into notification(user_id, title, content, is_read, create_time) " +
            "values(#{userId}, #{title}, #{content}, #{isRead}, #{createTime})")
    int insert(Notification notification);

    @Select("select * from notification where user_id = #{userId} order by create_time desc " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Notification> getByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from notification where user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("select count(*) from notification where user_id = #{userId} and is_read = 0")
    int countUnread(@Param("userId") Long userId);

    @Update("update notification set is_read = 1 where id = #{id}")
    int markRead(@Param("id") Long id);

    @Update("update notification set is_read = 1 where user_id = #{userId}")
    int markAllRead(@Param("userId") Long userId);
}
