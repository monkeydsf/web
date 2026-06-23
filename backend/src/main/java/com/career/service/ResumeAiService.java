package com.career.service;

import com.career.dto.AiResumeReviewResponse;
import com.career.dto.ResumeMatchResponse;
import com.career.model.Job;
import com.career.model.Resume;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class ResumeAiService {
    private static final int MAX_RESUME_CHARS = 12000;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String endpoint;
    private final String model;

    public ResumeAiService(ObjectMapper objectMapper,
                           @Value("${app.bailian.api-key:}") String apiKey,
                           @Value("${app.bailian.endpoint}") String endpoint,
                           @Value("${app.bailian.model}") String model) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public AiResumeReviewResponse review(MultipartFile file, String prompt) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("百炼 API Key 未配置，请设置 DASHSCOPE_API_KEY 或 app.bailian.api-key");
        }
        validateFile(file);
        String resumeText = extractText(file);
        if (!StringUtils.hasText(resumeText)) {
            throw new IllegalArgumentException("未能从简历文件中读取到文本内容");
        }
        String finalPrompt = StringUtils.hasText(prompt) ? prompt.trim() : "请帮我修改这份简历。";
        return callBailian(resumeText, finalPrompt);
    }

    public AiResumeReviewResponse promptOnly(String prompt) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("百炼 API Key 未配置，请设置 DASHSCOPE_API_KEY 或 app.bailian.api-key");
        }
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("提示词不能为空");
        }
        return callBailian("无简历文件，本次仅测试提示词调用。", prompt.trim());
    }

    public ResumeMatchResponse matchResumeToJob(Resume resume, Job job) {
        String resumeText = buildResumeText(resume);
        Set<String> keywords = extractKeywords(job);
        int score = calculateScore(resumeText, job, keywords);
        List<String> strengths = new ArrayList<>();
        List<String> gaps = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (resumeText.contains(job.getTitle().toLowerCase(Locale.ROOT))) {
            strengths.add("简历中已经出现目标岗位名称或相近表述");
        }
        if (resumeText.contains(job.getCompany().toLowerCase(Locale.ROOT))) {
            strengths.add("简历中有相关企业或行业经历");
        }
        if (job.getEducationRequirement() != null && !job.getEducationRequirement().isBlank()) {
            if (resumeText.contains(job.getEducationRequirement().toLowerCase(Locale.ROOT))) {
                strengths.add("教育背景满足岗位要求");
            } else {
                gaps.add("教育要求与简历未明显对齐");
                suggestions.add("补充教育经历和相关课程，强化学历匹配");
            }
        }

        for (String keyword : keywords) {
            if (!resumeText.contains(keyword)) {
                gaps.add("缺少关键词：" + keyword);
            }
        }

        if (score >= 80) {
            suggestions.add("可以优先投递，继续强化项目成果和量化表达");
        } else if (score >= 60) {
            suggestions.add("适合投递，但建议先补齐关键技能和项目描述");
        } else {
            suggestions.add("当前匹配偏弱，建议先按岗位关键词重写简历");
        }

        String summary = "当前匹配度约 " + score + " 分，" + (score >= 80 ? "可以直接投递。" : score >= 60 ? "还有优化空间。" : "建议先做针对性修改。");
        return new ResumeMatchResponse(score, summary, strengths.stream().distinct().toList(), gaps.stream().distinct().toList(), suggestions.stream().distinct().toList());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传简历文件");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".pdf") && !name.endsWith(".doc") && !name.endsWith(".docx") && !name.endsWith(".txt")) {
            throw new IllegalArgumentException("仅支持 PDF、DOC、DOCX、TXT 格式的简历");
        }
    }

    private String extractText(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getOriginalFilename());
            new AutoDetectParser().parse(inputStream, handler, metadata, new ParseContext());
            String text = handler.toString().replaceAll("\\s{3,}", "\n\n").trim();
            return text.length() > MAX_RESUME_CHARS ? text.substring(0, MAX_RESUME_CHARS) : text;
        } catch (IOException | SAXException | TikaException exception) {
            throw new IllegalArgumentException("简历文件解析失败，请换成可复制文本的 PDF、DOCX 或 TXT 文件");
        }
    }

    private AiResumeReviewResponse callBailian(String resumeText, String userPrompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "你是专业校园求职简历顾问。请用中文输出，给出具体可执行的修改建议，避免空泛评价。"
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", """
                                            用户修改要求：
                                            %s

                                            简历文本：
                                            %s

                                            请按以下结构输出：
                                            1. 总体评分与一句话结论
                                            2. 需要优先修改的 5 个问题
                                            3. 可直接替换到简历中的改写示例
                                            4. 针对目标岗位的关键词建议
                                            5. 下一步行动清单
                                            """.formatted(userPrompt, resumeText)
                            )
                    ),
                    "temperature", 0.4,
                    "max_tokens", 1800
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("百炼调用失败：" + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            JsonNode usage = root.path("usage");
            return new AiResumeReviewResponse(
                    content,
                    root.path("model").asText(model),
                    usage.path("prompt_tokens").isMissingNode() ? null : usage.path("prompt_tokens").asInt(),
                    usage.path("completion_tokens").isMissingNode() ? null : usage.path("completion_tokens").asInt(),
                    usage.path("total_tokens").isMissingNode() ? null : usage.path("total_tokens").asInt()
            );
        } catch (IOException exception) {
            throw new IllegalStateException("百炼响应解析失败");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("百炼调用被中断");
        }
    }

    private String buildResumeText(Resume resume) {
        return String.join("\n",
                nullToEmpty(resume.getExpectedCity()),
                nullToEmpty(resume.getExpectedPosition()),
                nullToEmpty(resume.getExpectedSalary()),
                nullToEmpty(resume.getEducation()),
                nullToEmpty(resume.getSkills()),
                nullToEmpty(resume.getProjectExperience()),
                nullToEmpty(resume.getPortfolioUrl()),
                nullToEmpty(resume.getParsedText())
        ).toLowerCase(Locale.ROOT);
    }

    private int calculateScore(String resumeText, Job job, Set<String> keywords) {
        int score = 35;
        if (job.getTitle() != null && resumeText.contains(job.getTitle().toLowerCase(Locale.ROOT))) score += 20;
        if (job.getCity() != null && resumeText.contains(job.getCity().toLowerCase(Locale.ROOT))) score += 10;
        if (job.getJobType() != null && resumeText.contains(job.getJobType().toLowerCase(Locale.ROOT))) score += 10;
        if (job.getEducationRequirement() != null && resumeText.contains(job.getEducationRequirement().toLowerCase(Locale.ROOT))) score += 10;
        for (String keyword : keywords) {
            if (resumeText.contains(keyword)) score += 4;
        }
        return Math.max(0, Math.min(100, score));
    }

    private Set<String> extractKeywords(Job job) {
        Set<String> keywords = new LinkedHashSet<>();
        addKeywords(keywords, job.getDescription());
        addKeywords(keywords, job.getRequirementText());
        addKeywords(keywords, job.getTitle());
        addKeywords(keywords, job.getJobType());
        return keywords;
    }

    private void addKeywords(Set<String> keywords, String text) {
        if (!StringUtils.hasText(text)) return;
        for (String item : text.toLowerCase(Locale.ROOT).split("[、，,；;\\s/]+")) {
            String trimmed = item.trim();
            if (trimmed.length() >= 2 && trimmed.length() <= 12) {
                keywords.add(trimmed);
            }
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
