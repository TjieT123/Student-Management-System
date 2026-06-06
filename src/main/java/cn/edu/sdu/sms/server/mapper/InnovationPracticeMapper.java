package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.InnovationPractice;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface InnovationPracticeMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into innovation_practice(sid, type, title, start_date, end_date, organization, role, " +
            "description, result, status, attachments, create_time) " +
            "values(#{sid}, #{type}, #{title}, #{startDate}, #{endDate}, #{organization}, #{role}, " +
            "#{description}, #{result}, #{status}, #{attachments}, #{createTime})")
    int insert(InnovationPractice practice);

    @Select("<script>select * from innovation_practice where sid = #{sid} order by create_time desc " +
            "LIMIT #{limit} OFFSET #{offset}</script>")
    List<InnovationPractice> getBySid(@Param("sid") String sid, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>select count(*) from innovation_practice where sid = #{sid}</script>")
    int countBySid(@Param("sid") String sid);

    @Select("<script>select ip.*, s.name as student_name from innovation_practice ip " +
            "left join student s on ip.sid = s.sid " +
            "<where>" +
            "<if test='name != null and name != \"\"'>and s.name like concat('%',#{name},'%')</if>" +
            "<if test='title != null and title != \"\"'>and ip.title like concat('%',#{title},'%')</if>" +
            "<if test='type != null and type != \"\"'>and ip.type = #{type}</if>" +
            "<if test='status != null and status != \"\"'>and ip.status = #{status}</if>" +
            "</where>" +
            "order by ip.create_time desc LIMIT #{limit} OFFSET #{offset}</script>")
    List<Map<String, Object>> getAllWithName(@Param("offset") int offset, @Param("limit") int limit,
            @Param("name") String name, @Param("title") String title,
            @Param("type") String type, @Param("status") String status);

    @Select("<script>select count(*) from innovation_practice ip " +
            "left join student s on ip.sid = s.sid " +
            "<where>" +
            "<if test='name != null and name != \"\"'>and s.name like concat('%',#{name},'%')</if>" +
            "<if test='title != null and title != \"\"'>and ip.title like concat('%',#{title},'%')</if>" +
            "<if test='type != null and type != \"\"'>and ip.type = #{type}</if>" +
            "<if test='status != null and status != \"\"'>and ip.status = #{status}</if>" +
            "</where></script>")
    int countAll(@Param("name") String name, @Param("title") String title,
            @Param("type") String type, @Param("status") String status);

    @Select("select ip.*, s.name as student_name from innovation_practice ip " +
            "left join student s on ip.sid = s.sid " +
            "where ip.status = 'PENDING' order by ip.create_time desc LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getPending(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from innovation_practice where status = 'PENDING'")
    int countPending();

    @Select("select ip.*, s.name as student_name from innovation_practice ip " +
            "left join student s on ip.sid = s.sid where ip.id = #{id}")
    Map<String, Object> getByIdWithName(@Param("id") Long id);

    @Update("<script>update innovation_practice <set>" +
            "<if test='title != null'>title = #{title},</if>" +
            "<if test='type != null'>type = #{type},</if>" +
            "<if test='startDate != null'>start_date = #{startDate},</if>" +
            "<if test='endDate != null'>end_date = #{endDate},</if>" +
            "<if test='organization != null'>organization = #{organization},</if>" +
            "<if test='role != null'>role = #{role},</if>" +
            "<if test='description != null'>description = #{description},</if>" +
            "<if test='result != null'>result = #{result},</if>" +
            "<if test='attachments != null'>attachments = #{attachments},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "</set> where id = #{id}</script>")
    int update(InnovationPractice practice);

    @Select("select * from innovation_practice where id = #{id}")
    InnovationPractice getById(Long id);

    @Update("update innovation_practice set status = #{status}, reviewer_id = #{reviewerId}, " +
            "review_time = now(), review_comment = #{reviewComment} where id = #{id}")
    int approve(@Param("id") Long id, @Param("status") String status,
                @Param("reviewerId") Long reviewerId, @Param("reviewComment") String reviewComment);

    @Delete("delete from innovation_practice where id = #{id}")
    int delete(Long id);
}
