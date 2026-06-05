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

    @Select("select * from innovation_practice where status = 'PENDING' order by create_time desc " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<InnovationPractice> getPending(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from innovation_practice where status = 'PENDING'")
    int countPending();

    @Select("select * from innovation_practice where id = #{id}")
    InnovationPractice getById(Long id);

    @Update("update innovation_practice set status = #{status}, reviewer_id = #{reviewerId}, " +
            "review_time = now(), review_comment = #{reviewComment} where id = #{id}")
    int approve(@Param("id") Long id, @Param("status") String status,
                @Param("reviewerId") Long reviewerId, @Param("reviewComment") String reviewComment);

    @Delete("delete from innovation_practice where id = #{id}")
    int delete(Long id);
}
