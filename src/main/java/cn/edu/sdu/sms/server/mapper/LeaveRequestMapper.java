package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.LeaveRequest;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LeaveRequestMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into leave_request(sid, type, start_date, end_date, reason, destination, status, create_time) " +
            "values(#{sid}, #{type}, #{startDate}, #{endDate}, #{reason}, #{destination}, #{status}, #{createTime})")
    int insert(LeaveRequest req);

    @Select("select * from leave_request where sid = #{sid} order by create_time desc LIMIT #{limit} OFFSET #{offset}")
    List<LeaveRequest> getBySid(@Param("sid") String sid, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from leave_request where sid = #{sid}")
    int countBySid(@Param("sid") String sid);

    @Select("select * from leave_request where status = 'PENDING' order by create_time desc LIMIT #{limit} OFFSET #{offset}")
    List<LeaveRequest> getPending(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from leave_request where status = 'PENDING'")
    int countPending();

    @Select("select * from leave_request where id = #{id}")
    LeaveRequest getById(Long id);

    @Update("update leave_request set status = #{status}, reviewer_id = #{reviewerId}, " +
            "review_time = now(), review_comment = #{reviewComment} where id = #{id}")
    int approve(@Param("id") Long id, @Param("status") String status,
                @Param("reviewerId") Long reviewerId, @Param("reviewComment") String reviewComment);

    @Delete("delete from leave_request where id = #{id}")
    int delete(Long id);
}
