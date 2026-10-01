package com.goldbyte.citieye_server.education.internal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.init();
    }

    @Test
    @DisplayName("Fix 3: Null file throws ResponseStatusException with 400 Bad Request")
    void testStoreFile_nullFile_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                fileStorageService.storeFile(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Cannot store an empty or null file"));
    }

    @Test
    @DisplayName("Fix 3: Path traversal attempt throws ResponseStatusException with 400 Bad Request")
    void testStoreFile_pathTraversal_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../../etc/passwd.pdf", "application/pdf", "malicious content".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                fileStorageService.storeFile(file));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Path traversal attempt detected"));
    }

    @Test
    @DisplayName("Fix 3: Unsupported file extension throws ResponseStatusException with 400 Bad Request")
    void testStoreFile_unsupportedExtension_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/octet-stream", "binary data".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                fileStorageService.storeFile(file));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Unsupported file extension"));
    }

    @Test
    @DisplayName("Fix 3: Null content-type header throws ResponseStatusException with 400 Bad Request")
    void testStoreFile_nullContentType_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "result.pdf", null, "pdf content".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                fileStorageService.storeFile(file));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("File content type header is required"));
    }

    @Test
    @DisplayName("Fix 3: Unsupported content-type throws ResponseStatusException with 400 Bad Request")
    void testStoreFile_unsupportedContentType_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "result.pdf", "application/x-msdos-program", "pdf content".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                fileStorageService.storeFile(file));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Unsupported content type"));
    }

    @Test
    @DisplayName("Fix 3: Load file as resource with empty/null relative path throws ResponseStatusException with 400 Bad Request")
    void testLoadFileAsResource_emptyPath_throwsBadRequest() {
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () ->
                fileStorageService.loadFileAsResource(""));
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () ->
                fileStorageService.loadFileAsResource(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());
    }

    @Test
    @DisplayName("Fix 3: Valid file store and load returns valid resource")
    void testStoreAndLoadFile_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "report_card.png", "image/png", "sample image content".getBytes()
        );

        String storedPath = fileStorageService.storeFile(file);
        assertNotNull(storedPath);
        assertTrue(storedPath.endsWith(".png"));

        var resource = fileStorageService.loadFileAsResource(storedPath);
        assertNotNull(resource);
        assertTrue(resource.exists());
    }
}
