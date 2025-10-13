package ru.hogwarts.school.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    public Long uploadAvatar(@PathVariable Long studentId,
                             @RequestParam MultipartFile file) throws IOException {
        return avatarService.uploadAvatar(studentId, file);
    }

    @GetMapping("/{studentId}/from-db")
    public byte[] getAvatarFromDb(@PathVariable Long studentId) {
        return avatarService.findAvatar(studentId).getData();
    }

    @GetMapping("/{studentId}/from-file")
    public ResponseEntity<Void> getAvatarFromFile(@PathVariable Long studentId,
                                                  HttpServletResponse response) {
        try {
            avatarService.getAvatarFromFile(studentId, response);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}