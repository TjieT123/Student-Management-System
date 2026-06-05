package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.SemesterConfig;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SemesterConfigMapper {

    @Select("select * from semester_config where id = 1")
    SemesterConfig getConfig();

    @Insert("insert into semester_config(id, semester_name, start_week_date, total_weeks) values(1, #{semesterName}, #{startWeekDate}, #{totalWeeks}) " +
            "on duplicate key update semester_name = #{semesterName}, start_week_date = #{startWeekDate}, total_weeks = #{totalWeeks}")
    int upsertConfig(SemesterConfig config);
}
