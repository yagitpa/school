package ru.hogwarts.school.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
public class AvatarService {
    private final AvatarRepository avatarRepository;
    private final StudentService studentService;

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final int PREVIEW_WIDTH = 100;
    private static final int BUFFER_SIZE = 1024;

    @Value("${avatars.dir.path}")
    private String avatarsDir;

    public AvatarService(AvatarRepository avatarRepository, StudentService studentService) {
        this.avatarRepository = avatarRepository;
        this.studentService = studentService;
    }

    public Long uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        Student student = studentService.findStudent(studentId);

        Path filePath = Path.of(avatarsDir, studentId + "." + getExtension(file.getOriginalFilename()));
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);

        try (InputStream is = file.getInputStream();
             OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
             BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
             BufferedOutputStream bos = new BufferedOutputStream(os, BUFFER_SIZE)) {
            bis.transferTo(bos);
        }

        Avatar avatar = findOrCreateAvatar(studentId);
        avatar.setStudent(student);
        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(generateImagePreview(filePath));

        Avatar savedAvatar = avatarRepository.save(avatar);
        return savedAvatar.getId();
    }

    private Avatar findOrCreateAvatar(Long studentId) {
        return avatarRepository.findByStudentId(studentId)
                .orElse(new Avatar());
    }

    private byte[] generateImagePreview(Path filePath) throws IOException {
        try (InputStream is = Files.newInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                throw new IOException("Could not read image");
            }

            int height = image.getHeight() / (image.getWidth() / PREVIEW_WIDTH);
            BufferedImage preview = new BufferedImage(PREVIEW_WIDTH, height, image.getType());
            Graphics2D graphics = preview.createGraphics();
            graphics.drawImage(image, 0, 0, PREVIEW_WIDTH, height, null);
            graphics.dispose();

            ImageIO.write(preview, getExtension(filePath.getFileName().toString()), baos);
            return baos.toByteArray();
        }
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public Avatar findAvatar(Long studentId) {
        return avatarRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Avatar not found for student with ID " + studentId
                ));
    }

    public void getAvatarFromFile(Long studentId, HttpServletResponse response) throws IOException {
        Avatar avatar = findAvatar(studentId);
        File file = new File(avatar.getFilePath());

        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar file not found");
        }

        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }

        response.setContentType(contentType);
        response.setContentLength((int) file.length());

        try (InputStream is = Files.newInputStream(file.toPath());
             OutputStream os = response.getOutputStream()) {
            is.transferTo(os);
        }
    }
}