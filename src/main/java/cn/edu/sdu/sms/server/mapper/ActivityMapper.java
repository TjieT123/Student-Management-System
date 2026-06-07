package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Activity;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityMapper {
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into activity(sid, title, type, date, location, description, photos, content, max_participants, create_time) " +
            "values(#{sid}, #{title}, #{type}, #{date}, #{location}, #{description}, #{photos}, #{content}, #{maxParticipants}, #{createTime})")
    int insert(Activity activity);

    @Select("select * from activity order by create_time desc LIMIT #{limit} OFFSET #{offset}")
    List<Activity> selectAll(@Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>select count(*) from activity " +
            "<where><if test='keyword != null and keyword != \"\"'>title like concat('%',#{keyword},'%')</if></where></script>")
    int countAll(@Param("keyword") String keyword);

    @Select("select * from activity where id = #{id}")
    Activity getById(Long id);

    @Update("update activity set title=#{title}, type=#{type}, date=#{date}, location=#{location}, " +
            "description=#{description}, photos=#{photos}, content=#{content}, max_participants=#{maxParticipants} where id=#{id}")
    int update(Activity activity);

    @Delete("delete from activity where id = #{id}")
    int delete(Long id);

    // Registration
    @Insert("insert into activity_registration(activity_id, sid, register_time) values(#{activityId}, #{sid}, now())")
    int register(@Param("activityId") Long activityId, @Param("sid") String sid);

    @Select("select count(*) from activity_registration where activity_id = #{activityId}")
    int countRegistrations(@Param("activityId") Long activityId);

    @Select("select count(*) from activity_registration where activity_id = #{activityId} and sid = #{sid}")
    int isRegistered(@Param("activityId") Long activityId, @Param("sid") String sid);

    @Delete("delete from activity_registration where activity_id = #{activityId} and sid = #{sid}")
    int cancelRegistration(@Param("activityId") Long activityId, @Param("sid") String sid);

    @Delete("delete from activity_registration where sid = #{sid}")
    int cancelAllRegistrationsBySid(@Param("sid") String sid);

    @Delete("delete from activity_registration where activity_id = #{activityId}")
    int deleteRegistrationsByActivityId(@Param("activityId") Long activityId);

    @Select("<script>select a.*, (select count(*) from activity_registration where activity_id = a.id) as registered_count " +
            "from activity a " +
            "<where><if test='keyword != null and keyword != \"\"'>a.title like concat('%',#{keyword},'%')</if></where> " +
            "order by a.create_time desc LIMIT #{limit} OFFSET #{offset}</script>")
    List<Map<String, Object>> selectAllWithCount(@Param("offset") int offset, @Param("limit") int limit, @Param("keyword") String keyword);
}
