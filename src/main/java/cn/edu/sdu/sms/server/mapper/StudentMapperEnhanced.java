package cn.edu.sdu.sms.server.mapper;

import cn.edu.sdu.sms.server.models.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentMapperEnhanced {

    @Select("select * from student")
    List<Student> getAllStudents();

    @Select("select * from student where sid = #{sid}")
    Student getStudentBySid(String sid);

    @Select("<script>" +
            "select * from student where 1=1 " +
            "<if test='sid != null and sid != \"\"'>and sid like concat('%', #{sid}, '%') </if>" +
            "<if test='name != null and name != \"\"'>and name like concat('%', #{name}, '%') </if>" +
            "order by sid LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Student> getAllStudentsPaginated(@Param("sid") String sid, @Param("name") String name, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "select count(*) from student where 1=1 " +
            "<if test='sid != null and sid != \"\"'>and sid like concat('%', #{sid}, '%') </if>" +
            "<if test='name != null and name != \"\"'>and name like concat('%', #{name}, '%') </if>" +
            "</script>")
    int countStudents(@Param("sid") String sid, @Param("name") String name);

    @Insert("insert into student(sid, name, major, gender, s_class, birth_date, enrollment_year, id_card, native_place, political_status, address, contact_name, contact_phone, social_relations, grade) " +
            "values(#{sid}, #{name}, #{major}, #{gender}, #{sClass}, #{birthDate}, #{enrollmentYear}, #{idCard}, #{nativePlace}, #{politicalStatus}, #{address}, #{contactName}, #{contactPhone}, #{socialRelations}, #{grade})")
    int insertStudent(Student student);

    @Update("<script>" +
            "update student <set>" +
            "<if test='name != null'>name = #{name}, </if>" +
            "<if test='major != null'>major = #{major}, </if>" +
            "<if test='gender != null'>gender = #{gender}, </if>" +
            "<if test='sClass != null'>s_class = #{sClass}, </if>" +
            "<if test='birthDate != null'>birth_date = #{birthDate}, </if>" +
            "<if test='enrollmentYear != null'>enrollment_year = #{enrollmentYear}, </if>" +
            "<if test='idCard != null'>id_card = #{idCard}, </if>" +
            "<if test='nativePlace != null'>native_place = #{nativePlace}, </if>" +
            "<if test='politicalStatus != null'>political_status = #{politicalStatus}, </if>" +
            "<if test='address != null'>address = #{address}, </if>" +
            "<if test='contactName != null'>contact_name = #{contactName}, </if>" +
            "<if test='contactPhone != null'>contact_phone = #{contactPhone}, </if>" +
            "<if test='socialRelations != null'>social_relations = #{socialRelations}, </if>" +
            "<if test='grade != null'>grade = #{grade}, </if>" +
            "</set> where sid = #{sid}" +
            "</script>")
    int updateStudent(Student student);

    @Delete("delete from student where sid = #{sid}")
    int deleteStudent(String sid);
}

