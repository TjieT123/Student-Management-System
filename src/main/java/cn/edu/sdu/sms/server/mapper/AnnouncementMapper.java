package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AnnouncementMapper {

    @Select("select * from announcement order by publish_time desc")
    List<Announcement> getAllAnnouncements();

    @Select("<script>" +
            "select id, title, publish_by as publishBy, publisher_name as publisherName, publish_time as publishTime " +
            "from announcement where 1=1 " +
            "<if test='id != null'>and id = #{id} </if>" +
            "<if test='title != null and title != \"\"'>and title like concat('%', #{title}, '%') </if>" +
            "order by publish_time desc LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Map<String, Object>> getAllAnnouncementsPaginated(@Param("id") Long id, @Param("title") String title, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "select count(*) from announcement where 1=1 " +
            "<if test='id != null'>and id = #{id} </if>" +
            "<if test='title != null and title != \"\"'>and title like concat('%', #{title}, '%') </if>" +
            "</script>")
    int countAnnouncements(@Param("id") Long id, @Param("title") String title);

    @Select("select * from announcement where id = #{id}")
    Announcement getAnnouncementById(Long id);

    @Insert("insert into announcement(title, content, publish_by, publisher_name, publish_time) " +
            "values(#{title}, #{content}, #{publishBy}, #{publisherName}, #{publishTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAnnouncement(Announcement announcement);

    @Update("<script>" +
            "update announcement " +
            "<set>" +
            "<if test='title != null'>title = #{title}, </if>" +
            "<if test='content != null'>content = #{content}, </if>" +
            "<if test='publisherName != null'>publisher_name = #{publisherName}, </if>" +
            "</set>" +
            "where id = #{id}" +
            "</script>")
    int updateAnnouncementSelective(Announcement announcement);

    @Update("update announcement set title = #{title}, content = #{content}, publish_by = #{publishBy}, " +
            "publisher_name = #{publisherName}, publish_time = #{publishTime} where id = #{id}")
    int updateAnnouncement(Announcement announcement);

    @Delete("delete from announcement where id = #{id}")
    int deleteAnnouncement(Long id);

    @Delete("delete from announcement where publish_by = #{publishBy}")
    int deleteAnnouncementByPublisher(String publishBy);
}

