package ru.hogwarts.school.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.dto.AvatarDataDto;
import ru.hogwarts.school.dto.AvatarInfoDto;
import ru.hogwarts.school.exception.AvatarNotFoundException;
import ru.hogwarts.school.exception.FileProcessingException;
import ru.hogwarts.school.exception.ImageProcessingException;
import ru.hogwarts.school.exception.InvalidFileException;
import ru.hogwarts.school.mapper.AvatarMapper;
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

    private static final int PREVIEW_WIDTH = 100;
    private final AvatarRepository avatarRepository;
    private final StudentService studentService;
    private final AvatarMapper avatarMapper;

    @Value("${avatars.dir.path:avatars}")
    private String avatarsDir;

    public AvatarService(AvatarRepository avatarRepository, StudentService studentService, AvatarMapper avatarMapper) {
        this.avatarRepository = avatarRepository;
        this.studentService = studentService;
        this.avatarMapper = avatarMapper;
    }

    @Transactional
    public void uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
            throw InvalidFileException.emptyFile();
        }

        String fileExtension = getExtension(file.getOriginalFilename());
        if (fileExtension == null || fileExtension.isBlank()) {
            throw InvalidFileException.missingExtension();
        }

        Path dirPath = Path.of(avatarsDir);
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Student student = studentService.findStudentEntity(studentId);

        String normalizedStudentName = normalizeFileName(student.getName());
        Path fullSizeFilePath = Path.of(avatarsDir,
                student.getId() + "_" + normalizedStudentName + "_full." + fileExtension);

        try {
            file.transferTo(fullSizeFilePath);
        } catch (IOException e) {
            throw new FileProcessingException("file transfer", e);
        }

        byte[] previewData;
        try {
            previewData = generateImagePreview(file);
        } catch (IOException e) {
            throw new ImageProcessingException("preview generation", e);
        }

        Avatar avatar = findOrCreateAvatar(student);
        avatar.setFilePath(fullSizeFilePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(previewData);
        avatar.setStudent(student);

        avatarRepository.save(avatar);
    }

    @Transactional
    public AvatarInfoDto findAvatarInfo(Long studentId) {
        Student student = studentService.findStudentEntity(studentId);
        Avatar avatar = avatarRepository.findByStudent(student).orElseThrow(
                () -> new AvatarNotFoundException(studentId)
        );
        return avatarMapper.toInfoDto(avatar);
    }

    @Transactional
    public AvatarDataDto findAvatarData(Long studentId) {
        Student student = studentService.findStudentEntity(studentId);
        Avatar avatar = avatarRepository.findByStudent(student).orElseThrow(
                () -> new AvatarNotFoundException(studentId)
        );
        return avatarMapper.toDataDto(avatar);
    }

    @Transactional
    public void getAvatarFromFile(Long studentId, HttpServletResponse response) throws IOException {
        AvatarInfoDto avatarInfo = findAvatarInfo(studentId);
        String filePath = avatarInfo.filePath();

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath))) {
            response.setContentType(avatarInfo.mediaType());
            response.setContentLength((int) avatarInfo.fileSize());
            bufferedInputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new FileProcessingException("avatar file streaming", e);
        }
    }

    @Transactional
    public Page<AvatarInfoDto> getAllAvatarsWithPagination(int page, int size) {
        Pageable pageable = PaginationUtil.createPageRequest(page, size);
        Page<Avatar> avatarsPage = avatarRepository.findAll(pageable);
        return avatarsPage.map(avatarMapper::toInfoDto);
    }

    // ========== HELPER METHODS ==========

    private byte[] generateImagePreview(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            BufferedImage originalImage = ImageIO.read(is);

            if (originalImage == null) {
                throw ImageProcessingException.forImageReading();
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