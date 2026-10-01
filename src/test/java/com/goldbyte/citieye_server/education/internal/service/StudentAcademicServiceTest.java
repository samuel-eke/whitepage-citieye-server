package com.goldbyte.citieye_server.education.internal.service;

import com.goldbyte.citieye_server.education.internal.model.AcademicStatusTransition;
import com.goldbyte.citieye_server.education.internal.model.StudentAcademicUpdate;
import com.goldbyte.citieye_server.education.internal.repo.AcademicStatusTransitionRepository;
import com.goldbyte.citieye_server.education.internal.repo.StudentAcademicUpdateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentAcademicServiceTest {

    @Mock
    private StudentAcademicUpdateRepository studentAcademicUpdateRepository;

    @Mock
    private AcademicStatusTransitionRepository academicStatusTransitionRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private StudentAcademicService studentAcademicService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("R1: Submit academic update with multipart file upload persists update and logs status transition")
    void testSubmitAcademicUpdate_withMultipartFile_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "result_card.png", "image/png", "dummy-image-content".getBytes()
        );
        when(fileStorageService.storeFile(file)).thenReturn("/uploads/result-cards/result_card_uuid.png");

        StudentAcademicUpdate savedUpdate = new StudentAcademicUpdate();
        savedUpdate.setId(101L);
        savedUpdate.setCitizenCode("CIT-ED-ORE-00100");
        savedUpdate.setAcademicLevel("SECONDARY");
        savedUpdate.setAcademicYear("2024/2025");
        savedUpdate.setClassGrade("SSS 3");
        savedUpdate.setInstitutionName("Immaculate Conception College");
        savedUpdate.setStatus("SUBMITTED");
        savedUpdate.setResultCardUrl("/uploads/result-cards/result_card_uuid.png");

        when(studentAcademicUpdateRepository.save(any(StudentAcademicUpdate.class))).thenReturn(savedUpdate);

        StudentAcademicUpdate result = studentAcademicService.submitAcademicUpdate(
                "CIT-ED-ORE-00100",
                "SECONDARY",
                "2024/2025",
                "SSS 3",
                "Immaculate Conception College",
                file,
                null,
                "Final senior secondary results"
        );

        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals("SUBMITTED", result.getStatus());
        assertEquals("/uploads/result-cards/result_card_uuid.png", result.getResultCardUrl());

        // Verify status transition was logged in audit trail
        ArgumentCaptor<AcademicStatusTransition> transitionCaptor = ArgumentCaptor.forClass(AcademicStatusTransition.class);
        verify(academicStatusTransitionRepository, times(1)).save(transitionCaptor.capture());
        AcademicStatusTransition logged = transitionCaptor.getValue();
        assertEquals(101L, logged.getUpdateId());
        assertEquals("CIT-ED-ORE-00100", logged.getCitizenCode());
        assertEquals("SUBMITTED", logged.getStatus());
        assertNull(logged.getPreviousStatus());
    }

    @Test
    @DisplayName("R1: Submit academic update with dynamic subject-grade mappings persists and logs transition")
    void testSubmitAcademicUpdate_withSubjectGrades_success() {
        Map<String, String> grades = Map.of(
                "Mathematics", "A1",
                "English Language", "B2",
                "Physics", "A1",
                "Chemistry", "B3"
        );

        StudentAcademicUpdate savedUpdate = new StudentAcademicUpdate();
        savedUpdate.setId(102L);
        savedUpdate.setCitizenCode("CIT-ED-ORE-00100");
        savedUpdate.setAcademicLevel("SECONDARY");
        savedUpdate.setAcademicYear("2024/2025");
        savedUpdate.setStatus("SUBMITTED");
        savedUpdate.setSubjectGrades(grades);

        when(studentAcademicUpdateRepository.save(any(StudentAcademicUpdate.class))).thenReturn(savedUpdate);

        StudentAcademicUpdate result = studentAcademicService.submitAcademicUpdate(
                "CIT-ED-ORE-00100",
                "SECONDARY",
                "2024/2025",
                "SSS 2",
                "Edo High School",
                null,
                grades,
                "Term 3 terminal examination results"
        );

        assertNotNull(result);
        assertEquals(102L, result.getId());
        assertEquals(grades, result.getSubjectGrades());
        verify(fileStorageService, never()).storeFile(any());
        verify(academicStatusTransitionRepository, times(1)).save(any(AcademicStatusTransition.class));
    }

    @Test
    @DisplayName("R1 Compulsory Validation: Reject when both file and subject-grades are missing (400 Bad Request)")
    void testSubmitAcademicUpdate_missingBothFileAndGrades_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                studentAcademicService.submitAcademicUpdate(
                        "CIT-ED-ORE-00100",
                        "PRIMARY",
                        "2024/2025",
                        "Primary 5",
                        "Central Primary School",
                        null,
                        null,
                        "Missing documentation"
                )
        );

        assertTrue(ex.getMessage().contains("Compulsory submission requirement"));
        verify(studentAcademicUpdateRepository, never()).save(any());
        verify(academicStatusTransitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("R1 Boundary Defense: Degenerate grade map with blank keys/values rejected with 400 Bad Request")
    void testSubmitAcademicUpdate_degenerateGradeMap_throwsBadRequest() {
        Map<String, String> blankGrades = Map.of(
                "", "",
                "   ", "   "
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                studentAcademicService.submitAcademicUpdate(
                        "CIT-ED-ORE-00100",
                        "PRIMARY",
                        "2024/2025",
                        "Primary 5",
                        "Central Primary School",
                        null,
                        blankGrades,
                        "Degenerate grades test"
                )
        );

        assertTrue(ex.getMessage().contains("Compulsory submission requirement"));
        verify(studentAcademicUpdateRepository, never()).save(any());
    }

    @Test
    @DisplayName("R1 Status Transition: Real-time transition SUBMITTED -> VERIFIED with audit logging")
    void testUpdateStatus_validTransitionSubmittedToVerified_success() {
        StudentAcademicUpdate existing = new StudentAcademicUpdate();
        existing.setId(100L);
        existing.setCitizenCode("CIT-ED-ORE-00100");
        existing.setAcademicLevel("SECONDARY");
        existing.setAcademicYear("2024/2025");
        existing.setStatus("SUBMITTED");

        when(studentAcademicUpdateRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(studentAcademicUpdateRepository.save(any(StudentAcademicUpdate.class))).thenReturn(existing);

        StudentAcademicUpdate updated = studentAcademicService.updateStatus(
                100L, "VERIFIED", "Physically validated original result card", "CIT-OFR-ED-ORE-0001"
        );

        assertEquals("VERIFIED", updated.getStatus());

        ArgumentCaptor<AcademicStatusTransition> captor = ArgumentCaptor.forClass(AcademicStatusTransition.class);
        verify(academicStatusTransitionRepository, times(1)).save(captor.capture());
        AcademicStatusTransition logged = captor.getValue();
        assertEquals("SUBMITTED", logged.getPreviousStatus());
        assertEquals("VERIFIED", logged.getStatus());
        assertEquals("CIT-OFR-ED-ORE-0001", logged.getPerformedBy());
        assertEquals("Physically validated original result card", logged.getNotes());
    }

    @Test
    @DisplayName("R1 State Machine: Direct transition from SUBMITTED to ACTIVE is strictly rejected (400 Bad Request)")
    void testUpdateStatus_directJumpToActive_throwsBadRequest() {
        StudentAcademicUpdate existing = new StudentAcademicUpdate();
        existing.setId(100L);
        existing.setCitizenCode("CIT-ED-ORE-00100");
        existing.setStatus("SUBMITTED");

        when(studentAcademicUpdateRepository.findById(100L)).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                studentAcademicService.updateStatus(100L, "ACTIVE", "Bypassing verification", "CIT-OFR-ED-ORE-0001"));

        assertTrue(ex.getMessage().contains("Cannot transition directly to ACTIVE"));
        verify(studentAcademicUpdateRepository, never()).save(any());
    }

    @Test
    @DisplayName("R1 State Machine: Transition from VERIFIED to ACTIVE succeeds")
    void testUpdateStatus_verifiedToActive_success() {
        StudentAcademicUpdate existing = new StudentAcademicUpdate();
        existing.setId(100L);
        existing.setCitizenCode("CIT-ED-ORE-00100");
        existing.setStatus("VERIFIED");

        when(studentAcademicUpdateRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(studentAcademicUpdateRepository.save(any(StudentAcademicUpdate.class))).thenReturn(existing);

        StudentAcademicUpdate updated = studentAcademicService.updateStatus(
                100L, "ACTIVE", "Enrolled in active cohort", "CIT-OFR-ED-ORE-0001"
        );

        assertEquals("ACTIVE", updated.getStatus());
        verify(studentAcademicUpdateRepository, times(1)).save(any(StudentAcademicUpdate.class));
        verify(academicStatusTransitionRepository, times(1)).save(any(AcademicStatusTransition.class));
    }

    @Test
    @DisplayName("R1 State Machine: Unknown status string is rejected with 400 Bad Request")
    void testUpdateStatus_unknownStatus_throwsBadRequest() {
        StudentAcademicUpdate existing = new StudentAcademicUpdate();
        existing.setId(100L);
        existing.setStatus("SUBMITTED");

        when(studentAcademicUpdateRepository.findById(100L)).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                studentAcademicService.updateStatus(100L, "MALICIOUS_INJECTION", "test", "CIT-OFR-ED-ORE-0001"));

        assertTrue(ex.getMessage().contains("Invalid status"));
        verify(studentAcademicUpdateRepository, never()).save(any());
    }

    @Test
    @DisplayName("R1 Academic Progression Query: Returns full chronological series of updates and transitions")
    void testGetAcademicProgression_returnsChronologicalSeries() {
        StudentAcademicUpdate u1 = new StudentAcademicUpdate();
        u1.setId(10L);
        u1.setAcademicYear("2023/2024");
        u1.setStatus("ACTIVE");

        StudentAcademicUpdate u2 = new StudentAcademicUpdate();
        u2.setId(20L);
        u2.setAcademicYear("2024/2025");
        u2.setStatus("SUBMITTED");

        AcademicStatusTransition t1 = new AcademicStatusTransition();
        t1.setUpdateId(10L);
        t1.setStatus("SUBMITTED");

        AcademicStatusTransition t2 = new AcademicStatusTransition();
        t2.setUpdateId(10L);
        t2.setStatus("ACTIVE");

        when(studentAcademicUpdateRepository.findAllByCitizenCodeOrderBySubmittedAtDesc("CIT-ED-ORE-00100"))
                .thenReturn(List.of(u2, u1));
        when(academicStatusTransitionRepository.findAllByCitizenCodeOrderByTransitionTimestampAsc("CIT-ED-ORE-00100"))
                .thenReturn(List.of(t1, t2));

        StudentAcademicService.AcademicProgressionResponse response =
                studentAcademicService.getAcademicProgression("CIT-ED-ORE-00100");

        assertNotNull(response);
        assertEquals("CIT-ED-ORE-00100", response.getCitizenCode());
        assertEquals(2, response.getUpdates().size());
        assertEquals(2, response.getTransitions().size());
    }
}
