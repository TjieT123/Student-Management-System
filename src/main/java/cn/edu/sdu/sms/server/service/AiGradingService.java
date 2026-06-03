package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.dto.AiGradeRequest;
import cn.edu.sdu.sms.server.dto.AiGradeResult;
import cn.edu.sdu.sms.server.mapper.HomeworkSubmitMapper;
import cn.edu.sdu.sms.server.models.HomeworkSubmit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class AiGradingService {

    private static final Logger log = LoggerFactory.getLogger(AiGradingService.class);

    private static final String SYSTEM_PROMPT = """
        你是一位经验丰富的大学课程助教，负责批改各类学科的学生作业。
        请根据作业的具体要求和所属学科，灵活调整评分标准，对学生的提交进行客观、公正的评分。

        通用评分框架（可根据作业类型调整各维度权重）：

        1. 内容完整性（40分）：
           - 编程类：是否实现了所有要求的功能
           - 论文类：是否覆盖了核心论点，论据是否充分
           - 数学类：解题步骤是否完整，答案是否正确
           - 文科类：是否全面回答了问题，观点是否明确
           - 英语类：是否覆盖了所有要求的要点

        2. 逻辑与结构（25分）：
           - 编程类：代码结构、算法逻辑是否清晰
           - 论文类：论证逻辑是否严密，结构是否合理
           - 数学类：推导过程是否有逻辑错误
           - 文科类：行文是否有条理，层次是否分明
           - 英语类：篇章结构是否合理，衔接是否自然

        3. 准确性与规范性（25分）：
           - 编程类：代码规范、命名、注释
           - 论文类：引用格式、术语使用、学术规范
           - 数学类：计算准确性、符号使用规范
           - 文科类：事实准确性、概念理解正确性
           - 英语类：语法、拼写、用词准确性

        4. 深度与创新（10分）：
           - 是否有超出基本要求的深入思考
           - 是否有独特的见解或创新的解决方案
           - 是否能举一反三或联系实际

        评分原则：
        - 根据作业题目要求的具体内容判断作业类型，自动调整各维度权重
        - 评语应具体、有建设性，指出具体问题而非笼统评价
        - 亮点和改进建议应具体到可操作的要点
        - 鼓励学生的努力和进步，批评应温和且有指导意义

        请以纯净 JSON 格式返回评分结果，不要包含 markdown 代码块标记（不要用 ```json 包裹）。
        """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HomeworkSubmitMapper submitMapper;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    public AiGradingService(HomeworkSubmitMapper submitMapper) {
        this.submitMapper = submitMapper;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplateBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    public AiGradeResult grade(AiGradeRequest request) {
        HomeworkSubmit submit = submitMapper.getSubmissionById(request.getSubmitId());
        if (submit == null) {
            return null;
        }

        String userPrompt = buildUserPrompt(
                request.getHomeworkTitle(),
                request.getHomeworkContent(),
                submit.getContent()
        );

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.3,
                "max_tokens", 1000
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        try {
            log.info("AI grading request: url={}, model={}", apiUrl, model);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            log.info("AI response status: {}, body: {}", response.getStatusCode(), response.getBody());
            String aiContent = extractAiContent(response.getBody());
            AiGradeResult result = objectMapper.readValue(aiContent, AiGradeResult.class);

            if (result.getScore() != null) {
                result.setScore(Math.max(0, Math.min(100, result.getScore())));
            }

            return result;
        } catch (RestClientException e) {
            log.error("AI API call failed: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("AI grading error: {}", e.getMessage(), e);
            return null;
        }
    }

    public String getSuggestion(String homeworkTitle, String homeworkContent,
                                  String studentAnswer, Integer score, String teacherComment) {
        String userPrompt = buildSuggestionPrompt(homeworkTitle, homeworkContent,
                studentAnswer, score, teacherComment);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SUGGESTION_SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.5,
                "max_tokens", 1200
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            String aiContent = extractAiContent(response.getBody());
            JsonNode root = objectMapper.readTree(aiContent);
            return root.path("suggestion").asText();
        } catch (RestClientException e) {
            log.error("AI suggestion API call failed: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("AI suggestion error: {}", e.getMessage(), e);
            return null;
        }
    }

    private static final String SUGGESTION_SYSTEM_PROMPT = """
        你是一位耐心且富有洞察力的大学课程助教。学生已经收到了作业评分和教师评语，
        现在需要你根据作业题目、学生答案、教师评语和分数，为学生提供个性化学习改进建议。

        要求：
        1. 建议应具体、可操作，指出学习中需要加强的知识点或技能
        2. 语气应鼓励、支持，肯定学生的努力，同时明确指出提升方向
        3. 建议中可以包含推荐的学习资源方向（如推荐复习某章节、练习某类题型等）
        4. 长度控制在200-400字

        请以纯净 JSON 格式返回：
        {"suggestion": "你的建议内容..."}
        """;

    private String buildSuggestionPrompt(String homeworkTitle, String homeworkContent,
                                          String studentAnswer, Integer score,
                                          String teacherComment) {
        return """
            作业题目：%s

            作业要求：%s

            学生答案：%s

            教师评分：%d分

            教师评语：%s

            请根据以上信息，为该学生提供个性化的学习改进建议。
            严格按以下 JSON 格式返回：
            {"suggestion": "你的建议..."}
            """.formatted(homeworkTitle, homeworkContent, studentAnswer, score,
                teacherComment != null ? teacherComment : "无");
    }

    private String buildUserPrompt(String homeworkTitle, String homeworkContent,
                                   String studentSubmission) {
        return """
            作业题目：%s

            作业要求：
            %s

            学生提交内容：
            %s

            请完成以下任务：
            1. 根据作业类型，从通用评分框架中选取适用的维度进行评分
            2. 给出分数（0-100的整数）
            3. 撰写详细的综合评语（150-300字），包含具体分析和改进方向
            4. 列出2-4个具体的亮点（highlights）
            5. 列出2-4个具体的改进建议（suggestions）

            严格按以下 JSON 格式返回，不要包含任何其他内容：
            {"score": 85, "comment": "综合评语...", "highlights": "1. xx 2. xx", "suggestions": "1. xx 2. xx"}
            """.formatted(homeworkTitle, homeworkContent, studentSubmission);
    }

    private String extractAiContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0)
                    .path("message").path("content").asText();
            // 去除可能的 markdown 代码块标记
            content = content.trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("```json\\s*", "")
                        .replaceAll("```\\s*", "")
                        .trim();
            }
            return content;
        } catch (Exception e) {
            return responseBody;
        }
    }
}
