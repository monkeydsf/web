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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public AiResumeReviewResponse review(MultipartFile file, String prompt) {
        ensureApiKey();
        validateFile(file);
        String resumeText = extractText(file);
        if (!StringUtils.hasText(resumeText)) {
            throw new IllegalArgumentException("未能从简历文件中读取到文本内容");
        }
        String finalPrompt = StringUtils.hasText(prompt) ? prompt.trim() : "请帮我修改这份简历。";
        return callBailianPrompt(defaultResumeSystemPrompt(), """
                用户修改要求：
                %s

                简历文本：
                %s
                """.formatted(finalPrompt, resumeText));
    }

    public AiResumeReviewResponse promptOnly(String prompt) {
        ensureApiKey();
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("提示词不能为空");
        }
        return callBailianPrompt(defaultChatSystemPrompt(), prompt.trim());
    }

    public AiResumeReviewResponse generateTool(String toolKey, String input, String context) {
        ensureApiKey();
        if (!StringUtils.hasText(input)) {
            throw new IllegalArgumentException("输入内容不能为空");
        }

        String key = toolKey == null ? "" : toolKey.trim().toLowerCase(Locale.ROOT);
        String ctx = StringUtils.hasText(context) ? context.trim() : "";
        return switch (key) {
            case "job-match" -> callBailianPrompt(
                    "你是岗位匹配顾问。输出岗位方向、关键词、城市建议、筛选规则和投递优先级，直接给结论。",
                    "求职需求：\n%s\n\n参考信息：\n%s".formatted(input.trim(), ctx)
            );
            case "interview-practice" -> callBailianPrompt(
                    "你是面试训练教练。输出模拟问题、参考回答要点、追问方向和复习清单，务实直接。",
                    "目标岗位：\n%s\n\n补充信息：\n%s".formatted(input.trim(), ctx)
            );
            case "offer-negotiation" -> callBailianPrompt(
                    "你是 Offer 谈判顾问。输出风险判断、可谈条件、沟通话术和优先级，给出明确建议。",
                    "Offer 情况：\n%s\n\n补充信息：\n%s".formatted(input.trim(), ctx)
            );
            case "personality-test" -> callBailianPrompt(
                    "你是职业性格分析助手。输出性格画像、适合岗位、团队环境建议和避雷提醒。",
                    "个人偏好：\n%s\n\n补充信息：\n%s".formatted(input.trim(), ctx)
            );
            case "30-day-plan", "thirty-day-plan" -> callBailianPrompt(
                    "你是 30 天求职规划师。输出按周拆解的计划、每日任务、检查点和执行节奏，必须可落地。",
                    "目标与基础：\n%s\n\n补充信息：\n%s".formatted(input.trim(), ctx)
            );
            case "application-script" -> callBailianPrompt(
                    "你是投递文案助手。输出邮件标题、正文、开场白和内推话术，要求简洁专业，可直接复制。",
                    "岗位与个人亮点：\n%s\n\n补充信息：\n%s".formatted(input.trim(), ctx)
            );
            case "company-radar" -> callBailianPrompt(
                    "你是公司避坑分析师。输出风险等级、需要追问的问题、可接受条件和避坑建议，结论优先。",
                    "公司与岗位信息：\n%s\n\n补充信息：\n%s".formatted(input.trim(), ctx)
            );
            case "consult" -> callBailianPrompt(
                    "你是求职对话顾问。先给可执行建议，再指出需要补充的信息，回答要短而直接。",
                    "用户问题：\n%s\n\n历史上下文：\n%s".formatted(input.trim(), ctx.isBlank() ? "无" : ctx)
            );
            default -> throw new IllegalArgumentException("不支持的 AI 工具类型: " + toolKey);
        };
    }

    public ResumeMatchResponse matchResumeToJob(Resume resume, Job job) {
        String resumeText = buildResumeText(resume);
        Set<String> keywords = extractKeywords(job);
        int score = calculateScore(resumeText, job, keywords);
        List<String> strengths = new ArrayList<>();
        List<String> gaps = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (job.getTitle() != null && resumeText.contains(job.getTitle().toLowerCase(Locale.ROOT))) {
            strengths.add("简历里已经出现目标岗位或相近表述");
        }
        if (job.getCompany() != null && resumeText.contains(job.getCompany().toLowerCase(Locale.ROOT))) {
            strengths.add("简历里有相关企业或行业经历");
        }

        if (job.getEducationRequirement() != null && !job.getEducationRequirement().isBlank()) {
            if (resumeText.contains(job.getEducationRequirement().toLowerCase(Locale.ROOT))) {
                strengths.add("教育背景匹配岗位要求");
            } else {
                gaps.add("教育背景与岗位要求未明显对齐");
                suggestions.add("补充教育经历和相关课程");
            }
        }

        for (String keyword : keywords) {
            if (!resumeText.contains(keyword)) {
                gaps.add("缺少关键词：" + keyword);
            }
        }

        suggestions.add(score >= 80 ? "可以优先投递，继续强化项目结果和量化指标"
                : score >= 60 ? "适合投递，但建议先补齐关键词和项目描述"
                : "当前匹配偏弱，建议按岗位关键词重写简历");

        String summary = "当前匹配度约 " + score + " 分，" + (score >= 80 ? "可以直接投递。" : score >= 60 ? "还有优化空间。" : "建议针对性修改。");
        return new ResumeMatchResponse(score, summary,
                strengths.stream().distinct().toList(),
                gaps.stream().distinct().toList(),
                suggestions.stream().distinct().toList());
    }

    private void ensureApiKey() {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("百炼 API Key 未配置，请设置 DASHSCOPE_API_KEY 或 app.bailian.api-key");
        }
    }

    private String defaultResumeSystemPrompt() {
        return "你是专业校园求职简历顾问。请用中文输出，给出具体可执行的修改建议，避免空泛评价。";
    }

    private String defaultChatSystemPrompt() {
        return "你是一个求职助手。请用中文输出，内容要具体、可执行，避免空话。";
    }

    private AiResumeReviewResponse callBailianPrompt(String systemPrompt, String userPrompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
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
                throw new IllegalStateException("百炼调用失败: " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode usage = root.path("usage");
            return new AiResumeReviewResponse(
                    root.path("choices").path(0).path("message").path("content").asText(""),
                    root.path("model").asText(model),
                    usage.path("prompt_tokens").isMissingNode() ? null : usage.path("prompt_tokens").asInt(),
                    usage.path("completion_tokens").isMissingNode() ? null : usage.path("completion_tokens").asInt(),
                    usage.path("total_tokens").isMissingNode() ? null : usage.path("total_tokens").asInt()
            );
        } catch (IOException exception) {
            throw new IllegalStateException("百炼响应解析失败", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("百炼调用被中断", exception);
        }
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
        if (!StringUtils.hasText(text)) {
            return;
        }
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
