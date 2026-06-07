package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.LeaveRequest;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface LeaveRequestMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into leave_request(sid, type, start_date, end_date, reason, status, create_time) " +
            "values(#{sid}, #{type}, #{startDate}, #{endDate}, #{reason}, #{status}, #{createTime})")
    int insert(LeaveRequest req);

    @Select("select * from leave_request where sid = #{sid} order by create_time desc LIMIT #{limit} OFFSET #{offset}")
    List<LeaveRequest> getBySid(@Param("sid") String sid, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from leave_request where sid = #{sid}")
    int countBySid(@Param("sid") String sid);

    @Select("<script>select lr.*, s.name as student_name from leave_request lr " +
            "left join student s on lr.sid = s.sid " +
            "<where>" +
            "<if test='name != null and name != \"\"'>and s.name like concat('%',#{name},'%')</if>" +
            "<if test='type != null and type != \"\"'>and lr.type = #{type}</if>" +
            "<if test='status != null and status != \"\"'>and lr.status = #{status}</if>" +
            "</where>" +
            "order by lr.create_time desc LIMIT #{limit} OFFSET #{offset}</script>")
    List<Map<String, Object>> getAllWithName(@Param("offset") int offset, @Param("limit") int limit,
            @Param("name") String name, @Param("type") String type, @Param("status") String status);

    @Select("<script>select count(*) from leave_request lr " +
            "left join student s on lr.sid = s.sid " +
            "<where>" +
            "<if test='name != null and name != \"\"'>and s.name like concat('%',#{name},'%')</if>" +
            "<if test='type != null and type != \"\"'>and lr.type = #{type}</if>" +
            "<if test='status != null and status != \"\"'>and lr.status = #{status}</if>" +
            "</where></script>")
    int countAll(@Param("name") String name, @Param("type") String type, @Param("status") String status);

    @Select("select lr.*, s.name as student_name from leave_request lr " +
            "left join student s on lr.sid = s.sid " +
            "where lr.status = 'PENDING' order by lr.create_time desc LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getPending(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from leave_request where status = 'PENDING'")
    int countPending();

    @Select("select lr.*, s.name as student_name from leave_request lr " +
            "left join student s on lr.sid = s.sid where lr.id = #{id}")
    Map<String, Object> getByIdWithName(@Param("id") Long id);

    @Update("<script>update leave_request <set>" +
            "<if test='type != null'>type = #{type},</if>" +
            "<if test='startDate != null'>start_date = #{startDate},</if>" +
            "<if test='endDate != null'>end_date = #{endDate},</if>" +
            "<if test='reason != null'>reason = #{reason},</if>" +
"" +
            "<if test='status != null'>status = #{status},</if>" +
            "</set> where id = #{id}</script>")
    int update(LeaveRequest req);

    @Select("select * from leave_request where id = #{id}")
    LeaveRequest getById(Long id);

    @Update("update leave_request set status = #{status}, reviewer_id = #{reviewerId}, " +
            "review_time = now(), review_comment = #{reviewComment} where id = #{id}")
    int approve(@Param("id") Long id, @Param("status") String status,
                @Param("reviewerId") Long reviewerId, @Param("reviewComment") String reviewComment);

    @Delete("delete from leave_request where id = #{id}")
    int delete(Long id);

    @Delete("delete from leave_request where sid = #{sid}")
    int deleteBySid(String sid);
}
