package cn.edu.sdu.sms.server.controller;

import cn.edu.sdu.sms.server.date.Result;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 获取所有课程
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return Result.success(courses, "Courses retrieved successfully");
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