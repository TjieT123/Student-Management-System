package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from user")
    List<User> getAllUsers();

    @Select("select * from user where id = #{id}")
    User getUserById(Long id);

    @Select("select * from user where username = #{username}")
    User getUserByUsername(String username);

    @Select("select id, username, name, role, phone, sch_id from user where role = #{role} order by id LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getUsersByRolePaginated(@Param("role") String role, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from user where role = #{role}")
    int countByRole(@Param("role") String role);

    @Select("select u.id, u.username, u.name, u.role, u.phone, u.sch_id, s.major, s.gender, s.s_class as sClass " +
            "from user u left join student s on u.sch_id = s.sid " +
            "where u.role = 'STUDENT' order by u.id LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> getStudentUsersPaginated(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from user where role = 'STUDENT'")
    int countStudentUsers();

    @Insert("insert into user(username, password, name, role, phone, sch_id) " +
            "values(#{username}, #{password}, #{name}, #{role}, #{phone}, #{schId})")
    int insertUser(User user);

    @Update("update user " +
            "set password = #{password}, name = #{name}, role = #{role}, phone = #{phone}, sch_id = #{schId} " +
            "where id = #{id}")
    int updateUser(User user);

    @Delete("delete from user where id = #{id}")
    int deleteUser(Long id);

    @Select("select * from user where sch_id = #{schId}")
    User getUserBySchId(@Param("schId") String schId);

    @Delete("delete from user where sch_id = #{schId}")
    int deleteUserBySchId(@Param("schId") String schId);
}

