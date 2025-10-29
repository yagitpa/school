package ru.hogwarts.school.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.util.PaginationUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class AvatarService {
    private final AvatarRepository avatarRepository;
    private final StudentService studentService;

    @Value("${avatars.dir.path:avatars}")
    private String avatarsDir;

    private static final int PREVIEW_WIDTH = 100;

    public AvatarService(AvatarRepository avatarRepository, StudentService studentService) {
        this.avatarRepository = avatarRepository;
        this.studentService = studentService;
    }

    @Transactional
    public void uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
            return;
        }

        String fileExtension = getExtension(file.getOriginalFilename());
        if (fileExtension == null || fileExtension.isBlank()) {
            return;
        }

        Path dirPath = Path.of(avatarsDir);
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Student student = studentService.findStudent(studentId);

        Path fullSizeFilePath = Path.of(avatarsDir, student.getName() + "_" + student.getId() + "_full."
                + fileExtension);
        file.transferTo(fullSizeFilePath);

        byte[] previewData = generateImagePreview(file);

        Avatar avatar = findOrCreateAvatar(student);
        avatar.setFilePath(fullSizeFilePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(previewData);
        avatar.setStudent(student);

        avatarRepository.save(avatar);
    }

    @Transactional
    public Avatar findAvatar(Long studentId) {
        Student student = studentService.findStudent(studentId);
        return avatarRepository.findByStudent(student)
                               .orElseThrow(() -> new ResponseStatusException(
                                       HttpStatus.NOT_FOUND,
                                       "Avatar not found for student with ID " + studentId
                               ));
    }

    @Transactional
    public void getAvatarFromFile(Long studentId, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        Avatar avatar = findAvatar(studentId);
        String filePath = avatar.getFilePath();

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath))) {
            response.setContentType(avatar.getMediaType());
            response.setContentLength((int) avatar.getFileSize());
            bufferedInputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
        }
    }

    @Transactional
    public Avatar getAvatarFromDB(Long studentId) {
        return findAvatar(studentId);
    }

    @Transactional
    public Page<Avatar> getAllAvatarsWithPagination(int page, int size) {
        Pageable pageable = PaginationUtil.createPageRequest(page, size);
        return avatarRepository.findAll(pageable);
    }

    // ========== HELPER METHODS ==========

    private byte[] generateImagePreview(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            BufferedImage originalImage = ImageIO.read(is);

            if (originalImage == null) {
                throw new IOException("Could not read image");
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int previewHeight = (int) ((double) originalHeight / originalWidth * PREVIEW_WIDTH);

            BufferedImage previewImage = new BufferedImage(PREVIEW_WIDTH, previewHeight, originalImage.getType());
            Graphics2D graphics = previewImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(originalImage, 0, 0, PREVIEW_WIDTH, previewHeight, null);
            graphics.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String formatName = getExtension(file.getOriginalFilename());
            assert formatName != null;
            ImageIO.write(previewImage, formatName, baos);

            return baos.toByteArray();
        }
    }

    private Avatar findOrCreateAvatar(Student student) {
        Optional<Avatar> existingAvatar = avatarRepository.findByStudent(student);
        return existingAvatar.orElse(new Avatar());
    }

    private String getExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}