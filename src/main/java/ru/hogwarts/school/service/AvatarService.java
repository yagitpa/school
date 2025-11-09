package ru.hogwarts.school.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);

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
        logger.info("Was invoked method for UPLOAD Avatar for Student with ID: {}", studentId);
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
            logger.warn("Attempt to UPLOAD empty file or file without name for Student with ID: {}", studentId);
            throw InvalidFileException.emptyFile();
        }

        String fileExtension = getExtension(file.getOriginalFilename());
        if (fileExtension == null || fileExtension.isBlank()) {
            logger.warn("File without extension provided for Student with ID: {}", studentId);
            throw InvalidFileException.missingExtension();
        }

        logger.debug("Processing file with extension {} for Student with ID: {}", fileExtension, studentId);
        Path dirPath = Path.of(avatarsDir);
        if (Files.notExists(dirPath)) {
            logger.info("CREATING avatars directory: {}", dirPath);
            Files.createDirectories(dirPath);
        }

        Student student = studentService.findStudentEntity(studentId);
        logger.debug("Found Student: {} with ID: {}", student.getName(), studentId);

        String normalizedStudentName = normalizeFileName(student.getName());
        Path fullSizeFilePath = Path.of(avatarsDir,
                student.getId() + "_" + normalizedStudentName + "_full." + fileExtension);

        logger.debug("SAVING full-size Avatar to {} for Student with ID: {}", fullSizeFilePath, studentId);
        try {
            file.transferTo(fullSizeFilePath);
        } catch (IOException e) {
            logger.error("Failed to transfer file for Student with ID: {}", studentId, e);
            throw new FileProcessingException("file transfer", e);
        }

        byte[] previewData;
        try {
            logger.debug("GENERATING preview Avatar for Student with ID: {}", studentId);
            previewData = generateImagePreview(file);
        } catch (IOException e) {
            logger.error("Failed to generate preview for Student with ID: {}",studentId, e);
            throw new ImageProcessingException("preview generation", e);
        }

        Avatar avatar = findOrCreateAvatar(student);
        avatar.setFilePath(fullSizeFilePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(previewData);
        avatar.setStudent(student);

        logger.info("SAVING Avatar to database for Student with ID: {}", studentId);
        avatarRepository.save(avatar);
        logger.info("Avatar successfully uploaded for Student with ID: {}", studentId);
    }

    @Transactional
    public AvatarInfoDto findAvatarInfo(Long studentId) {
        logger.info("Was invoked method for FIND Avatar info for Student with ID: {}", studentId);
        Student student = studentService.findStudentEntity(studentId);
        logger.debug("Looking for Avatar for Student with ID: {}", studentId);
        Avatar avatar = avatarRepository.findByStudent(student).orElseThrow(
                () -> {
                    logger.error("Avatar not found for Student with ID: {}", studentId);
                    return new AvatarNotFoundException(studentId);
                }
        );
        logger.debug("Avatar info found for Student with ID: {}", studentId);
        return avatarMapper.toInfoDto(avatar);
    }

    @Transactional
    public AvatarDataDto findAvatarData(Long studentId) {
        logger.info("Was invoked method for FIND Avatar data for Student with ID: {}", studentId);
        Student student = studentService.findStudentEntity(studentId);
        logger.debug("Looking dor Avatar data for Student with ID: {}", studentId);
        Avatar avatar = avatarRepository.findByStudent(student).orElseThrow(
                () -> {
                    logger.error("Avatar data not found for Student with ID: {}", studentId);
                    return new AvatarNotFoundException(studentId);
                }
        );
        logger.debug("Avatar data found for Student with ID: {}", studentId);
        return avatarMapper.toDataDto(avatar);
    }

    @Transactional
    public void getAvatarFromFile(Long studentId, HttpServletResponse response) throws IOException {
        logger.info("Was invoked method for GET Avatar from file for Student with ID: {}", studentId);
        AvatarInfoDto avatarInfo = findAvatarInfo(studentId);
        String filePath = avatarInfo.filePath();

        logger.debug("Streaming Avatar file from path: {} for Student with ID: {}", filePath, studentId);
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath))) {
            response.setContentType(avatarInfo.mediaType());
            response.setContentLength((int) avatarInfo.fileSize());
            bufferedInputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
            logger.debug("Avatar file successfully streamed for Student with ID: {}", studentId);
        } catch (IOException e) {
            logger.error("Failed to stream Avatar file for Student with ID: {}", studentId, e);
            throw new FileProcessingException("avatar file streaming", e);
        }
    }

    @Transactional
    public Page<AvatarInfoDto> getAllAvatarsWithPagination(int page, int size) {
        logger.info("Was invoked method for GET ALL avatars with pagination, page: {}, size: {}", page, size);
        Pageable pageable = PaginationUtil.createPageRequest(page, size);
        logger.debug("Created pageable request: page = {}, size = {}", page, size);
        Page<Avatar> avatarsPage = avatarRepository.findAll(pageable);
        logger.debug("Found {} avatars on page {}", avatarsPage.getNumberOfElements(), page);
        return avatarsPage.map(avatarMapper::toInfoDto);
    }

    // ========== HELPER METHODS ==========

    private byte[] generateImagePreview(MultipartFile file) throws IOException {
        logger.debug("Generating image preview for file: {}", file.getOriginalFilename());
        try (InputStream is = file.getInputStream()) {
            BufferedImage originalImage = ImageIO.read(is);

            if (originalImage == null) {
                logger.warn("Failed to read image for preview genetation, file: {}", file.getOriginalFilename());
                throw ImageProcessingException.forImageReading();
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int previewHeight = (int) ((double) originalHeight / originalWidth * PREVIEW_WIDTH);

            logger.debug("Original image size: {}x{}, preview size: {}x{}", originalWidth, originalHeight,
                    PREVIEW_WIDTH, previewHeight);
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

            logger.debug("Preview generated successfully for file: {}", file.getOriginalFilename());
            return baos.toByteArray();
        }
    }

    private Avatar findOrCreateAvatar(Student student) {
        logger.debug("Finding or creating Avatar for Student with ID: {}", student.getId());
        Optional<Avatar> existingAvatar = avatarRepository.findByStudent(student);
        if (existingAvatar.isPresent()) {
            logger.debug("Found existing Avatar for Student with ID: {}", student.getId());
            return existingAvatar.get();
        } else {
            logger.debug("Creating new Avatar for Student with ID: {}", student.getId());
            return new Avatar();
        }
    }

    private String getExtension(String fileName) {
        logger.debug("Getting extension for file: {}", fileName);
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            logger.warn("File name is null or doesn't contain extension: {}", fileName);
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String normalizeFileName(String fileName) {
        String defaultName = "student_" + System.currentTimeMillis();
        logger.debug("Normalizing file name: {}", fileName);
        if (fileName == null) {
            logger.warn("File name is null, using default name: {}", defaultName);
            return defaultName;
        }

        String normalized = fileName
                .trim()
                .toLowerCase()
                .replaceAll("[\\\\/:*?\"<>|\\s]", "_")
                .replaceAll("[^a-z0-9_.-]", "")
                .replaceAll("_{2,}", "_");

        if (normalized.isEmpty()) {
            logger.warn("Normalized file empty, using default name: {}", defaultName);
            return defaultName;
        }

        return normalized;
    }
}