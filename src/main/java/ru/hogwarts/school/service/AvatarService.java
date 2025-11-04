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
import ru.hogwarts.school.dto.AvatarDto;
import ru.hogwarts.school.mapper.AvatarMapper;
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
    private final AvatarMapper avatarMapper;

    @Value("${avatars.dir.path:avatars}")
    private String avatarsDir;

    private static final int PREVIEW_WIDTH = 100;

    public AvatarService(AvatarRepository avatarRepository, StudentService studentService, AvatarMapper avatarMapper) {
        this.avatarRepository = avatarRepository;
        this.studentService = studentService;
        this.avatarMapper = avatarMapper;
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

        Student student = studentService.findStudentEntity(studentId);

        String normalizedStudentName = normalizeFileName(student.getName());
        Path fullSizeFilePath = Path.of(avatarsDir, normalizedStudentName + "_" + student.getId() + "_full."
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
    public AvatarDto findAvatar(Long studentId) {
        Student student = studentService.findStudentEntity(studentId);
        Avatar avatar = avatarRepository.findByStudent(student)
                                        .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Avatar not found for student with ID " + studentId
                                        ));
        return avatarMapper.toDto(avatar);
    }

    @Transactional
    public void getAvatarFromFile(Long studentId, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        AvatarDto avatarDTO = findAvatar(studentId);
        String filePath = avatarDTO.getFilePath();

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath))) {
            response.setContentType(avatarDTO.getMediaType());
            response.setContentLength((int) avatarDTO.getFileSize());
            bufferedInputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
        }
    }

    @Transactional
    public AvatarDto getAvatarFromDB(Long studentId) {
        return findAvatar(studentId);
    }

    @Transactional
    public Page<AvatarDto> getAllAvatarsWithPagination(int page, int size) {
        Pageable pageable = PaginationUtil.createPageRequest(page, size);
        Page<Avatar> avatarsPage = avatarRepository.findAll(pageable);
        return avatarsPage.map(avatarMapper::toDto);
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

    private String normalizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown_name";
        }

        String normalized = fileName
                .trim()
                .toLowerCase()
                .replaceAll("[\\\\/:*?\"<>|\\s]", "_")
                .replaceAll("[^a-z0-9_.-]", "")
                .replaceAll("_{2,}", "_");

        if (normalized.isEmpty()) {
            return "student_" + System.currentTimeMillis();
        }

        return normalized;
    }
}