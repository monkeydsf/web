package com.career.controller;

import com.career.dto.ResumeDto;
import com.career.dto.ResumeRequest;
import com.career.model.Resume;
import com.career.model.User;
import com.career.repository.ResumeRepository;
import com.career.service.AuthService;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.xml.sax.SAXException;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final ResumeRepository resumeRepository;
    private final AuthService authService;

    public ResumeController(ResumeRepository resumeRepository, AuthService authService) {
        this.resumeRepository = resumeRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResumeDto get(@RequestHeader("X-Token") String token) {
        User user = authService.requireStudent(token);
        Resume resume = resumeRepository.findByUser(user).orElseGet(() -> createEmpty(user));
        return ResumeDto.from(resume);
    }

    @PutMapping
    public ResumeDto save(@RequestHeader("X-Token") String token, @RequestBody ResumeRequest request) {
        User user = authService.requireStudent(token);
        Resume resume = resumeRepository.findByUser(user).orElseGet(() -> createEmpty(user));
        resume.setExpectedCity(request.expectedCity());
        resume.setExpectedPosition(request.expectedPosition());
        resume.setExpectedSalary(request.expectedSalary());
        resume.setEducation(request.education());
        resume.setSkills(request.skills());
        resume.setProjectExperience(request.projectExperience());
        resume.setPortfolioUrl(request.portfolioUrl());
        resume.setUpdatedAt(LocalDateTime.now());
        return ResumeDto.from(resumeRepository.save(resume));
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeDto uploadFile(@RequestHeader("X-Token") String token, @RequestParam("file") MultipartFile file) throws IOException {
        User user = authService.requireStudent(token);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传简历文件");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename());
        String extension = getExtension(originalName);
        String contentType = file.getContentType();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("仅支持 PDF、DOC、DOCX 格式的简历");
        }
        if (StringUtils.hasText(contentType) && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            // Some browsers send Word files as octet-stream. The extension check above remains the source of truth.
            String normalizedType = contentType.toLowerCase(Locale.ROOT);
            if (!normalizedType.equals("application/octet-stream")) {
                throw new IllegalArgumentException("仅支持 PDF、DOC、DOCX 格式的简历");
            }
        }

        Path folder = Path.of("uploads", "resumes").toAbsolutePath().normalize();
        Files.createDirectories(folder);
        String storedName = "resume-" + user.getId() + "-" + UUID.randomUUID() + "." + extension;
        Path target = folder.resolve(storedName).normalize();
        if (!target.startsWith(folder)) {
            throw new IllegalArgumentException("文件名不合法");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        String parsedText = extractText(file);

        Resume resume = resumeRepository.findByUser(user).orElseGet(() -> createEmpty(user));
        resume.setFileName(originalName);
        resume.setFileUrl("/uploads/resumes/" + storedName);
        resume.setParsedText(parsedText);
        resume.setUpdatedAt(LocalDateTime.now());
        return ResumeDto.from(resumeRepository.save(resume));
    }

    private Resume createEmpty(User user) {
        Resume resume = new Resume();
        resume.setUser(user);
        return resumeRepository.save(resume);
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String extractText(MultipartFile file) {
        try (var inputStream = file.getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getOriginalFilename());
            new AutoDetectParser().parse(inputStream, handler, metadata, new ParseContext());
            String text = handler.toString().replaceAll("\\s{3,}", "\n\n").trim();
            return text.length() > 12000 ? text.substring(0, 12000) : text;
        } catch (IOException | SAXException | TikaException exception) {
            return "";
        }
    }
}
