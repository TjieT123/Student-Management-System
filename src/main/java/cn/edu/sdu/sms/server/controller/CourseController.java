package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 课程接口，提供课程列表查看和详情查询功能。
 */
@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 分页获取课程列表（不含detail和teacherId，含teacherName）
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getAllCourses(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = courseService.getCoursesPaginated(page, pageSize);
        return Result.success(result, "Courses retrieved successfully");
    }

    /**
     * 根据id获取课程
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return Result.error(404, "Course not found");
        }
        return Result.success(course, "Course retrieved");
    }
}