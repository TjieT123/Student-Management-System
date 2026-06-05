package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.mapper.CourseMapper;
import cn.edu.sdu.sms.server.mapper.UserMapper;
import cn.edu.sdu.sms.server.models.Course;
import cn.edu.sdu.sms.server.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取所有课程（空则返回默认9门课）
     */
    public List<Course> getAllCourses() {
        List<Course> courses = courseMapper.getAllCourses();
        if (courses.isEmpty()) {
            return getPredefinedCourses();
        }
        return courses;
    }

    /**
     * 分页获取课程列表（不含detail和teacherId，含teacherName），支持按id、courseName、teacherId筛选
     */
    public Map<String, Object> getCoursesPaginated(int page, int pageSize, Long id, String courseName, String teacherId) {
        int offset = (page - 1) * pageSize;
        int total;
        List<Map<String, Object>> list;

        boolean hasFilter = id != null || (courseName != null && !courseName.isEmpty()) || (teacherId != null && !teacherId.isEmpty());

        if (hasFilter) {
            total = courseMapper.countCoursesFiltered(id, courseName, teacherId);
            if (total == 0) {
                list = new ArrayList<>();
            } else {
                list = courseMapper.getCoursesWithTeacherFiltered(id, courseName, teacherId, offset, pageSize);
            }
        } else {
            total = courseMapper.countCourses();
            if (total == 0) {
                List<Course> predefined = getPredefinedCourses();
                total = predefined.size();
                int toIndex = Math.min(offset + pageSize, total);
                list = new ArrayList<>();
                for (int i = offset; i < toIndex; i++) {
                    Course c = predefined.get(i);
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", c.getId());
                    item.put("courseName", c.getCourseName());
                    item.put("address", c.getAddress());
                    item.put("teacherName", null);
                    list.add(item);
                }
            } else {
                list = courseMapper.getCoursesWithTeacher(offset, pageSize);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    /**
     * 根据ID获取课程
     */
    public Course getCourseById(Long id) {
        return courseMapper.getCourseById(id);
    }

    /**
     * 修改课程（只更新传入的非空字段）
     */
    public Course updateCourse(Long id, String courseName, String detail, String address, String teacherId) {
        Course course = courseMapper.getCourseById(id);
        if (course == null) {
            return null;
        }
        if (courseName != null) {
            course.setCourseName(courseName);
        }
        if (detail != null) {
            course.setDetail(detail);
        }
        if (address != null) {
            course.setAddress(address);
        }
        if (teacherId != null) {
            course.setTeacherId(teacherId);
        }
        courseMapper.updateCourse(course);
        return course;
    }

    /**
     * 分页获取当前学生的选课列表（课程id、courseName、teacherName）
     */
    public Map<String, Object> getStudentCoursesPaginated(Long userId, int page, int pageSize) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String sid = user.getSchId();
        int offset = (page - 1) * pageSize;
        int total = courseMapper.countStudentCoursesBySid(sid);
        List<Map<String, Object>> list = courseMapper.getStudentCourses(sid, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    /**
     * 分页获取教师所教课程列表
     */
    public Map<String, Object> getTeacherCoursesPaginated(Long userId, int page, int pageSize) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String teacherId = user.getSchId();
        int offset = (page - 1) * pageSize;
        int total = courseMapper.countTeacherCourses(teacherId);
        List<Map<String, Object>> list = courseMapper.getTeacherCourses(teacherId, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);
        return result;
    }

    /**
     * 获取课程选课学生列表（仅任课教师可查看）
     */
    public List<Map<String, Object>> getCourseStudents(Long courseId, Long userId) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Course course = courseMapper.getCourseById(courseId);
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        String schId = user.getSchId();
        if (course.getTeacherId() == null || !course.getTeacherId().equals(schId)) {
            throw new RuntimeException("Only the course teacher can view enrolled students");
        }
        return courseMapper.getEnrolledStudents(courseId);
    }

    /**
     * 选课（带冲突检测）
     */
    public void enrollCourseByUserId(Long userId, Long courseId) {
        User user = userMapper.getUserById(userId);
        if (user == null) throw new RuntimeException("User not found");
        enrollCourse(user.getSchId(), courseId);
    }

    /**
     * 取消选课
     */
    public void cancelCourse(Long userId, Long courseId) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        String sid = user.getSchId();
        if (courseMapper.countStudentCourse(sid, courseId) == 0) {
            throw new RuntimeException("Not enrolled in this course");
        }
        courseMapper.deleteStudentCourse(sid, courseId);
    }

    // -- Schedule methods (Feature 2) --
    public List<Map<String, Object>> getStudentSchedule(String sid, int week) {
        return courseMapper.getStudentSchedule(sid, week);
    }

    public List<Map<String, Object>> getTeacherSchedule(String teacherId, int week) {
        return courseMapper.getTeacherSchedule(teacherId, week);
    }

    // -- Materials methods (Feature 3) --
    public List<cn.edu.sdu.sms.server.models.AttachmentItem> getMaterials(Long courseId) {
        String json = courseMapper.getMaterials(courseId);
        return new AttachmentService().parseAttachments(json);
    }

    public List<cn.edu.sdu.sms.server.models.AttachmentItem> addMaterial(Long courseId, cn.edu.sdu.sms.server.models.AttachmentItem item, Long userId) {
        Course course = courseMapper.getCourseById(courseId);
        if (course == null) throw new RuntimeException("Course not found");
        String current = courseMapper.getMaterials(courseId);
        AttachmentService as = new AttachmentService();
        String newJson = as.addAttachment(current, item);
        courseMapper.updateMaterials(courseId, newJson);
        return as.parseAttachments(newJson);
    }

    public void removeMaterial(Long courseId, int index, Long userId) {
        Course course = courseMapper.getCourseById(courseId);
        if (course == null) throw new RuntimeException("Course not found");
        String current = courseMapper.getMaterials(courseId);
        AttachmentService as = new AttachmentService();
        List<cn.edu.sdu.sms.server.models.AttachmentItem> list = as.parseAttachments(current);
        if (index < 0 || index >= list.size()) throw new RuntimeException("Invalid index");
        String newJson = as.removeAttachment(current, index);
        courseMapper.updateMaterials(courseId, newJson);
    }

    // -- Conflict detection for enroll (Feature 2) --
    @org.springframework.transaction.annotation.Transactional
    public void enrollCourse(String sid, Long courseId) {
        if (courseMapper.countStudentCourse(sid, courseId) > 0)
            throw new RuntimeException("Already enrolled in this course");

        Course newCourse = courseMapper.getCourseById(courseId);
        if (newCourse == null) throw new RuntimeException("Course not found");
        if (newCourse.getSchedule() == null || newCourse.getSchedule().isEmpty()) {
            courseMapper.insertStudentCourse(sid, courseId);
            return;
        }

        // Conflict detection
        List<Map<String, Object>> enrolled = courseMapper.getStudentEnrolledCoursesWithSchedule(sid);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            List<Map<String, Object>> newSchedule = mapper.readValue(newCourse.getSchedule(), List.class);
            int newStart = newCourse.getStartWeek() != null ? newCourse.getStartWeek() : 1;
            int newEnd = newCourse.getEndWeek() != null ? newCourse.getEndWeek() : 18;

            for (Map<String, Object> ec : enrolled) {
                String ecSchedule = (String) ec.get("schedule");
                if (ecSchedule == null || ecSchedule.isEmpty()) continue;
                List<Map<String, Object>> ecSlots = mapper.readValue(ecSchedule, List.class);
                Long ecStartObj = ec.get("startWeek") != null ? ((Number) ec.get("startWeek")).longValue() : 1L;
                Long ecEndObj = ec.get("endWeek") != null ? ((Number) ec.get("endWeek")).longValue() : 18L;
                int ecStart = ecStartObj.intValue();
                int ecEnd = ecEndObj.intValue();

                if (newStart > ecEnd || newEnd < ecStart) continue; // no week overlap

                for (Map<String, Object> ns : newSchedule) {
                    int nDay = ((Number) ns.get("dayOfWeek")).intValue();
                    int nSlot = ((Number) ns.get("slot")).intValue();
                    for (Map<String, Object> es : ecSlots) {
                        int eDay = ((Number) es.get("dayOfWeek")).intValue();
                        int eSlot = ((Number) es.get("slot")).intValue();
                        if (nDay == eDay && nSlot == eSlot) {
                            String conflictCourse = (String) ec.get("courseName");
                            throw new RuntimeException("该课程与《" + conflictCourse + "》（周" + getDayName(nDay) + " 第" + nSlot + "节）时间冲突");
                        }
                    }
                }
            }
        } catch (RuntimeException e) { throw e; }
        catch (Exception e) { /* parse error, skip conflict check */ }

        courseMapper.insertStudentCourse(sid, courseId);
    }

    /**
     * 固定9门课程
     */
    private List<Course> getPredefinedCourses() {
        List<Course> courses = new ArrayList<>();
        String[] courseNames = {"语文", "数学", "英语", "物理", "化学", "生物", "政治", "历史", "地理"};
        for (int i = 0; i < courseNames.length; i++) {
            Course course = new Course();
            course.setId((long) (i + 1));
            course.setCourseName(courseNames[i]);
            courses.add(course);
        }
        return courses;
    }

    private static String getDayName(int d) { return new String[]{"一","二","三","四","五","六","日"}[d-1]; }
}