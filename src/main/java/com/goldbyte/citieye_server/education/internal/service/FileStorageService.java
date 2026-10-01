package com.goldbyte.citieye_server.education.internal.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for securely storing uploaded result card files with path traversal defense,
 * unique UUID naming, strict MIME type validation, and HTTP 400 error mapping.
 */
@Service
public class FileStorageService {

    private final Path storageLocation;
    private final String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".pdf"
    );

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "application/pdf"
    );

    public FileStorageService(@Value("${citieye.upload.dir:uploads/result-cards}") String uploadDir) {
        this.uploadDir = uploadDir;
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory: " + this.storageLocation, e);
        }
    }

    /**
     * Stores a multipart file securely and returns its relative access URL/path.
     *
     * @param file The uploaded multipart file
     * @return The relative storage path e.g. "/uploads/result-cards/uuid_filename.pdf"
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot store an empty or null file.");
        }

        String rawFilename = file.getOriginalFilename();
        if (rawFilename == null || rawFilename.isBlank()) {
            rawFilename = "result_card";
        }
        String originalFilename = StringUtils.cleanPath(rawFilename);

        // 1. Path Traversal Defense: Check for ".." or separator sequences
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path traversal attempt detected in filename: " + originalFilename);
        }

        // 2. Extension & Content-Type validation
        String lowerFilename = originalFilename.toLowerCase();
        boolean validExtension = ALLOWED_EXTENSIONS.stream().anyMatch(lowerFilename::endsWith);
        if (!validExtension) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file extension. Allowed extensions: " + ALLOWED_EXTENSIONS);
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File content type header is required.");
        }
        boolean validContentType = ALLOWED_CONTENT_TYPES.stream().anyMatch(t -> t.equalsIgnoreCase(contentType));
        if (!validContentType) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported content type: " + contentType + ". Allowed: " + ALLOWED_CONTENT_TYPES);
        }

        // 3. Unique UUID naming
        String sanitizedName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueFilename = UUID.randomUUID().toString() + "_" + sanitizedName;

        try {
            Path targetLocation = this.storageLocation.resolve(uniqueFilename).normalize();
            if (!targetLocation.startsWith(this.storageLocation)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot store file outside current storage directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = this.uploadDir.replace("\\", "/");
            if (!relativePath.startsWith("/")) {
                relativePath = "/" + relativePath;
            }
            if (!relativePath.endsWith("/")) {
                relativePath = relativePath + "/";
            }
            return relativePath + uniqueFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + uniqueFilename, ex);
        }
    }

    /**
     * Loads a stored file as a Spring Resource safely without throwing NPE on empty paths.
     */
    public Resource loadFileAsResource(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path cannot be null or empty.");
        }
        try {
            Path parsed = Paths.get(relativePath).getFileName();
            if (parsed == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path: " + relativePath);
            }
            String filename = parsed.toString();
            Path filePath = this.storageLocation.resolve(filename).normalize();
            if (!filePath.startsWith(this.storageLocation)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot access file outside current storage directory.");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found or not readable: " + relativePath);
            }
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path is invalid: " + relativePath, ex);
        }
    }
}
