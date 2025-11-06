package ru.hogwarts.school.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.dto.AvatarDataDto;
import ru.hogwarts.school.dto.AvatarInfoDto;
import ru.hogwarts.school.service.AvatarService;

import java.io.IOException;

@RestController
@RequestMapping("/avatar")
public class AvatarController {
    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping(value = "/{studentId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@PathVariable Long studentId,
                                               @RequestParam MultipartFile file) throws IOException {
        avatarService.uploadAvatar(studentId, file);
        return ResponseEntity.ok("Avatar uploaded successfully");
    }

    @GetMapping("/{studentId}/preview-info")
    public ResponseEntity<AvatarInfoDto> getAvatarPreviewInfo(@PathVariable Long studentId) {
        AvatarInfoDto avatarInfo = avatarService.findAvatarInfo(studentId);
        return ResponseEntity.ok(avatarInfo);
    }

    @GetMapping("/{studentId}/preview-data")
    public ResponseEntity<byte[]> getAvatarPreviewData(@PathVariable Long studentId) {
        AvatarDataDto avatarData = avatarService.findAvatarData(studentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatarData.mediaType()));
        headers.setContentLength(avatarData.data().length);
        headers.set("Content-Disposition", "inline; filename=preview.jpg");

        return new ResponseEntity<>(avatarData.data(), headers, HttpStatus.OK);
    }

    @GetMapping("/{studentId}/full")
    public void getAvatarFull(@PathVariable Long studentId,
                              HttpServletResponse response) {
        try {
            avatarService.getAvatarFromFile(studentId, response);
        } catch (IOException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<AvatarInfoDto>> getAllAvatarsWithPagination(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AvatarInfoDto> avatarsPage = avatarService.getAllAvatarsWithPagination(page, size);
        return ResponseEntity.ok(avatarsPage);
    }
}