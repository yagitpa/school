package ru.hogwarts.school.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<byte[]> getAvatarFromDb(@PathVariable Long studentId) {
        try {
            Avatar avatar = avatarService.findAvatar(studentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
            headers.setContentLength(avatar.getData().length);
            headers.set("Content-Disposition", "inline; filename=preview.jpg");

            return new ResponseEntity<>(avatar.getData(), headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    @GetMapping("/all-pageable")
    public ResponseEntity<Page<Avatar>> getAllAvatarsWithPagination(@RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Avatar> avatarsPage = avatarService.getAllAvatarsWithPagination(page, size);
            return ResponseEntity.ok(avatarsPage);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all-list")
    public ResponseEntity<List<Avatar>> getAllAvatars() {
        try {
            List<Avatar> avatars = avatarService.getAllAvatars();
            return ResponseEntity.ok(avatars);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}