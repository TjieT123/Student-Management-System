package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from user")
    List<User> getAllUsers();

    @Select("select * from user where id = #{id}")
    User getUserById(Long id);

    @Select("select * from user where username = #{username}")
    User getUserByUsername(String username);

    @Insert("insert into user(username, password, name, role, phone, sch_id) values(#{username}, #{password}, #{name}, #{role}, #{phone}, #{schId})")
    int insertUser(User user);

    @Update("update user set password = #{password}, name = #{name}, role = #{role}, phone = #{phone}, sch_id = #{schId} where id = #{id}")
    int updateUser(User user);

    @Delete("delete from user where id = #{id}")
    int deleteUser(Long id);
}

