package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Honor;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface HonorMapper {

    @Select("<script>select * from honor where 1=1 " +
            "<if test='sid != null and sid != \"\"'>and sid like concat('%', #{sid}, '%') </if>" +
            "order by award_date desc LIMIT #{limit} OFFSET #{offset}</script>")
    List<Map<String, Object>> selectAll(@Param("sid") String sid, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>select count(*) from honor where 1=1 " +
            "<if test='sid != null and sid != \"\"'>and sid like concat('%', #{sid}, '%') </if></script>")
    int count(@Param("sid") String sid);

    @Select("select * from honor where id = #{id}")
    Honor getById(Long id);

    @Select("select * from honor where sid = #{sid} order by award_date desc")
    List<Honor> getBySid(String sid);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into honor(sid, title, type, level, award_date, description) " +
            "values(#{sid}, #{title}, #{type}, #{level}, #{awardDate}, #{description})")
    int insert(Honor honor);

    @Update("update honor set title = #{title}, type = #{type}, level = #{level}, " +
            "award_date = #{awardDate}, description = #{description} where id = #{id}")
    int update(Honor honor);

    @Delete("delete from honor where id = #{id}")
    int delete(Long id);
}
