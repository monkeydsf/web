package com.career.controller;

import com.career.dto.SiteBackgroundDto;
import com.career.model.SiteBackground;
import com.career.repository.SiteBackgroundRepository;
import com.career.service.AuthService;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/backgrounds")
public class SiteBackgroundController {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif", "svg");
    private final SiteBackgroundRepository backgroundRepository;
    private final AuthService authService;
    private final Path uploadDir = Path.of("uploads", "backgrounds");

    public SiteBackgroundController(SiteBackgroundRepository backgroundRepository, AuthService authService) {
        this.backgroundRepository = backgroundRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<SiteBackgroundDto> list() {
        return backgroundRepository.findAllByOrderByPageKeyAsc().stream()
                .map(SiteBackgroundDto::from)
                .toList();
    }

    @PostMapping(value = "/{pageKey}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SiteBackgroundDto upload(@RequestHeader("X-Token") String token,
                                    @PathVariable String pageKey,
                                    @RequestParam("file") MultipartFile file) throws IOException {
        authService.requireAdmin(token);
        SiteBackground background = backgroundRepository.findById(pageKey)
                .orElseThrow(() -> new IllegalArgumentException("背景页面不存在"));
        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片文件");
        }
        String extension = extension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("仅支持 jpg、png、webp、gif、svg 图片");
        }
        Files.createDirectories(uploadDir);
        String filename = pageKey + "-" + UUID.randomUUID() + "." + extension;
        Path target = uploadDir.resolve(filename).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        background.setImageUrl("/uploads/backgrounds/" + filename);
        background.setUpdatedAt(LocalDateTime.now());
        return SiteBackgroundDto.from(backgroundRepository.save(background));
    }

    private String extension(String filename) {
        String cleanName = StringUtils.cleanPath(filename == null ? "" : filename);
        int dot = cleanName.lastIndexOf('.');
        if (dot < 0 || dot == cleanName.length() - 1) {
            throw new IllegalArgumentException("图片文件缺少扩展名");
        }
        return cleanName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
