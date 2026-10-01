package com.goldbyte.citieye_server.education.internal.service;

import com.goldbyte.citieye_server.education.internal.model.AcademicStatusTransition;
import com.goldbyte.citieye_server.education.internal.model.StudentAcademicUpdate;
import com.goldbyte.citieye_server.education.internal.repo.AcademicStatusTransitionRepository;
import com.goldbyte.citieye_server.education.internal.repo.StudentAcademicUpdateRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class StudentAcademicService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("SUBMITTED", "VERIFIED", "ACTIVE", "REJECTED");

    private final StudentAcademicUpdateRepository studentAcademicUpdateRepository;
    private final AcademicStatusTransitionRepository academicStatusTransitionRepository;
    private final FileStorageService fileStorageService;

    public StudentAcademicService(
            StudentAcademicUpdateRepository studentAcademicUpdateRepository,
            AcademicStatusTransitionRepository academicStatusTransitionRepository,
            FileStorageService fileStorageService) {
        this.studentAcademicUpdateRepository = studentAcademicUpdateRepository;
        this.academicStatusTransitionRepository = academicStatusTransitionRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Submits an annual academic update for an enrolled student.
     * Enforces the compulsory submission requirement: either a result card image/PDF OR
     * dynamic subject-grade key-value mappings must be provided.
     * Rejects degenerate grade maps (empty keys or values) when no file is provided.
     */
    @Transactional
    public StudentAcademicUpdate submitAcademicUpdate(
            String citizenCode,
            String academicLevel,
            String academicYear,
            String classGrade,
            String institutionName,
            MultipartFile resultCardFile,
            Map<String, String> subjectGrades,
            String notes) {

        if (citizenCode == null || citizenCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Citizen code is required.");
        }
        if (academicLevel == null || academicLevel.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Academic level is required.");
        }
        if (academicYear == null || academicYear.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Academic year is required.");
        }

        // Compulsory dual-option validation:
        boolean hasFile = resultCardFile != null && !resultCardFile.isEmpty();
        boolean hasGrades = subjectGrades != null && subjectGrades.entrySet().stream()
                .anyMatch(entry -> entry.getKey() != null && !entry.getKey().isBlank() &&
                                   entry.getValue() != null && !entry.getValue().isBlank());

        if (!hasFile && !hasGrades) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Compulsory submission requirement: either a result card document/image or subject grades mapping must be provided.");
        }

        String resultCardUrl = null;
        if (hasFile) {
            resultCardUrl = fileStorageService.storeFile(resultCardFile);
        }

        StudentAcademicUpdate update = new StudentAcademicUpdate();
        update.setCitizenCode(citizenCode.trim());
        update.setAcademicLevel(academicLevel.trim().toUpperCase());
        update.setAcademicYear(academicYear.trim());
        update.setClassGrade(classGrade != null ? classGrade.trim() : null);
        update.setInstitutionName(institutionName != null ? institutionName.trim() : null);
        update.setResultCardUrl(resultCardUrl);
        update.setSubjectGrades(hasGrades ? subjectGrades : null);
        update.setStatus("SUBMITTED");
        update.setSubmittedAt(OffsetDateTime.now());
        update.setNotes(notes);

        StudentAcademicUpdate savedUpdate = studentAcademicUpdateRepository.save(update);

        // Real-time audit-ready transition log
        AcademicStatusTransition transition = new AcademicStatusTransition();
        transition.setUpdateId(savedUpdate.getId());
        transition.setCitizenCode(savedUpdate.getCitizenCode());
        transition.setAcademicLevel(savedUpdate.getAcademicLevel());
        transition.setAcademicYear(savedUpdate.getAcademicYear());
        transition.setPreviousStatus(null);
        transition.setStatus("SUBMITTED");
        transition.setTransitionTimestamp(OffsetDateTime.now());
        transition.setPerformedBy(citizenCode);
        transition.setNotes("Initial student annual academic result submission");

        academicStatusTransitionRepository.save(transition);

        return savedUpdate;
    }

    /**
     * Administrative status transition update endpoint logic.
     * Transitions status (SUBMITTED -> VERIFIED -> ACTIVE / REJECTED) and appends to transition log.
     * Enforces the status transition state machine.
     */
    @Transactional
    public StudentAcademicUpdate updateStatus(Long updateId, String newStatus, String remarks, String actorCode) {
        if (newStatus == null || newStatus.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target status is required.");
        }
        String normalizedStatus = newStatus.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status: " + normalizedStatus + ". Allowed statuses: " + ALLOWED_STATUSES);
        }

        StudentAcademicUpdate update = studentAcademicUpdateRepository.findById(updateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic update not found with ID: " + updateId));

        String oldStatus = update.getStatus() != null ? update.getStatus().trim().toUpperCase() : "";

        // Status transition state machine: cannot jump directly to ACTIVE without prior VERIFIED status
        if ("ACTIVE".equals(normalizedStatus) && !"VERIFIED".equals(oldStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status transition. Cannot transition directly to ACTIVE. Result must be in VERIFIED status first. Current status: " + oldStatus);
        }

        update.setStatus(normalizedStatus);
        update.setVerifiedBy(actorCode);
        update.setVerifiedAt(OffsetDateTime.now());
        update.setUpdatedAt(OffsetDateTime.now());
        if (remarks != null && !remarks.isBlank()) {
            update.setNotes(remarks);
        }

        StudentAcademicUpdate savedUpdate = studentAcademicUpdateRepository.save(update);

        // Append to status transition log
        AcademicStatusTransition transition = new AcademicStatusTransition();
        transition.setUpdateId(savedUpdate.getId());
        transition.setCitizenCode(savedUpdate.getCitizenCode());
        transition.setAcademicLevel(savedUpdate.getAcademicLevel());
        transition.setAcademicYear(savedUpdate.getAcademicYear());
        transition.setPreviousStatus(oldStatus);
        transition.setStatus(normalizedStatus);
        transition.setTransitionTimestamp(OffsetDateTime.now());
        transition.setPerformedBy(actorCode);
        transition.setNotes(remarks);

        academicStatusTransitionRepository.save(transition);

        return savedUpdate;
    }

    /**
     * Progression query: returns historical series of annual updates and timestamped status transitions.
     */
    @Transactional(readOnly = true)
    public AcademicProgressionResponse getAcademicProgression(String citizenCode) {
        if (citizenCode == null || citizenCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Citizen code is required.");
        }

        List<StudentAcademicUpdate> updates = studentAcademicUpdateRepository
                .findAllByCitizenCodeOrderBySubmittedAtDesc(citizenCode.trim());

        List<AcademicStatusTransition> transitions = academicStatusTransitionRepository
                .findAllByCitizenCodeOrderByTransitionTimestampAsc(citizenCode.trim());

        return new AcademicProgressionResponse(citizenCode.trim(), updates, transitions);
    }

    @Transactional(readOnly = true)
    public Optional<StudentAcademicUpdate> getUpdateById(Long id) {
        return studentAcademicUpdateRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<StudentAcademicUpdate> getUpdatesByCitizen(String citizenCode) {
        return studentAcademicUpdateRepository.findAllByCitizenCodeOrderBySubmittedAtDesc(citizenCode);
    }

    @Getter
    @Setter
    public static class AcademicProgressionResponse {
        private String citizenCode;
        private List<StudentAcademicUpdate> updates;
        private List<AcademicStatusTransition> transitions;

        public AcademicProgressionResponse(String citizenCode, List<StudentAcademicUpdate> updates, List<AcademicStatusTransition> transitions) {
            this.citizenCode = citizenCode;
            this.updates = updates;
            this.transitions = transitions;
        }
    }
}
