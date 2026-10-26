package com.goldbyte.citieye_server.education.internal.service;

import com.goldbyte.citieye_server.education.internal.model.OfficerSchoolAssignment;
import com.goldbyte.citieye_server.education.internal.model.School;
import com.goldbyte.citieye_server.education.internal.model.StudentRecord;
import com.goldbyte.citieye_server.education.internal.repo.OfficerSchoolAssignmentRepository;
import com.goldbyte.citieye_server.education.internal.repo.SchoolRepository;
import com.goldbyte.citieye_server.education.internal.repo.StudentRecordRepository;
import com.goldbyte.citieye_server.userauth.UserAuthPublicService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing school and student records logged by educational officers.
 * Enforces state-scoped data isolation: educational officers can view and retrieve
 * all schools registered within their state, and manage student records within their
 * jurisdiction. Super Admin retains global access across all states.
 */
@Service
public class EducationalOfficerService {

    private final SchoolRepository schoolRepository;
    private final OfficerSchoolAssignmentRepository assignmentRepository;
    private final StudentRecordRepository studentRecordRepository;
    private final UserAuthPublicService userAuthPublicService;

    public EducationalOfficerService(
            SchoolRepository schoolRepository,
            OfficerSchoolAssignmentRepository assignmentRepository,
            StudentRecordRepository studentRecordRepository,
            UserAuthPublicService userAuthPublicService) {
        this.schoolRepository = schoolRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRecordRepository = studentRecordRepository;
        this.userAuthPublicService = userAuthPublicService;
    }

    public boolean isSuperAdmin(Authentication auth) {
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> {
            String authority = a.getAuthority();
            return authority.equalsIgnoreCase("super_admin") ||
                    authority.equalsIgnoreCase("ROLE_super_admin") ||
                    authority.equalsIgnoreCase("agency_admin") ||
                    authority.equalsIgnoreCase("ROLE_agency_admin");
        });
    }

    /**
     * Resolves the jurisdiction state for an officer by checking their AdminUser profile,
     * active school assignments, or fallback.
     */
    public String resolveOfficerState(String officerCode) {
        if (officerCode == null || officerCode.isBlank()) {
            return null;
        }

        Optional<String> officerState = userAuthPublicService.getOfficerStateOfResidence(officerCode.trim());
        if (officerState.isPresent() && !officerState.get().isBlank()) {
            return officerState.get();
        }

        return assignmentRepository.findAllByOfficerCodeAndActiveTrue(officerCode.trim()).stream()
                .map(OfficerSchoolAssignment::getState)
                .filter(s -> s != null && !s.isBlank())
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates a new school record.
     */
    @Transactional
    public School createSchool(School school, String officerCode, Authentication auth) {
        if (school.getSchoolName() == null || school.getSchoolName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "School name is required.");
        }
        if (school.getLga() == null || school.getLga().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "School LGA is required.");
        }
        if (school.getTotalStudentCount() == null || school.getTotalStudentCount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total student count must be greater than zero.");
        }

        if (school.getState() == null || school.getState().isBlank()) {
            String officerState = resolveOfficerState(officerCode);
            school.setState(officerState != null ? officerState : "Edo");
        }

        school.setCreatedAt(OffsetDateTime.now());
        return schoolRepository.save(school);
    }

    /**
     * Lists schools filtered by officer jurisdiction state, assignment, or query parameter.
     * Allows a field officer to view and retrieve all schools registered within their state.
     */
    @Transactional(readOnly = true)
    public List<School> getSchools(String lga, String officerCode, Authentication auth) {
        if (isSuperAdmin(auth)) {
            if (lga != null && !lga.isBlank()) {
                return schoolRepository.findAllByLgaIgnoreCase(lga.trim());
            }
            return schoolRepository.findAll();
        }

        String officerState = resolveOfficerState(officerCode);

        // Field officer with identified jurisdiction state: retrieve all schools within their state
        if (officerState != null && !officerState.isBlank()) {
            if (lga != null && !lga.isBlank()) {
                return schoolRepository.findAllByStateIgnoreCaseAndLgaIgnoreCase(officerState, lga.trim());
            }
            return schoolRepository.findAllByStateIgnoreCase(officerState);
        }

        // Fallback: if state is not on profile, inspect assignments
        List<OfficerSchoolAssignment> assignments = assignmentRepository.findAllByOfficerCodeAndActiveTrue(officerCode);
        if (!assignments.isEmpty()) {
            Set<String> assignedLgas = assignments.stream()
                    .map(a -> a.getLga().toLowerCase())
                    .collect(Collectors.toSet());

            Set<Long> assignedSchoolIds = assignments.stream()
                    .map(OfficerSchoolAssignment::getSchoolId)
                    .collect(Collectors.toSet());

            if (lga != null && !lga.isBlank()) {
                if (assignedLgas.contains(lga.trim().toLowerCase())) {
                    return schoolRepository.findAllByLgaIgnoreCase(lga.trim());
                }
                return Collections.emptyList();
            }

            return schoolRepository.findByIdIn(assignedSchoolIds);
        }

        if (lga != null && !lga.isBlank()) {
            return schoolRepository.findAllByLgaIgnoreCase(lga.trim());
        }

        return schoolRepository.findAll();
    }

    /**
     * Retrieves a single school by ID, enforcing jurisdiction check.
     */
    @Transactional(readOnly = true)
    public School getSchoolById(Long schoolId, String officerCode, Authentication auth) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "School not found with ID: " + schoolId));

        if (!isSuperAdmin(auth)) {
            String officerState = resolveOfficerState(officerCode);
            boolean stateMatches = officerState != null && school.getState() != null
                    && officerState.equalsIgnoreCase(school.getState().trim());
            boolean assigned = assignmentRepository.existsByOfficerCodeAndSchoolIdAndActiveTrue(officerCode, schoolId);

            if (!stateMatches && !assigned) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied. School is outside your assigned jurisdiction.");
            }
        }

        return school;
    }

    /**
     * Logs a comprehensive student record.
     * Enforces that the officer is assigned to the specified School and LGA, or that the school is in their state.
     */
    @Transactional
    public StudentRecord createStudentRecord(StudentRecord record, String officerCode, Authentication auth) {
        if (record.getCitizenCode() == null || record.getCitizenCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Citizen code is required.");
        }
        if (record.getSchoolId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "School ID is required.");
        }
        if (record.getLga() == null || record.getLga().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "LGA is required.");
        }

        if (!isSuperAdmin(auth)) {
            String officerState = resolveOfficerState(officerCode);
            Optional<School> schoolOpt = schoolRepository.findById(record.getSchoolId());
            boolean schoolInState = schoolOpt.isPresent() && officerState != null && schoolOpt.get().getState() != null
                    && officerState.equalsIgnoreCase(schoolOpt.get().getState().trim());
            boolean hasAssignment = assignmentRepository.existsByOfficerCodeAndSchoolIdAndActiveTrue(officerCode,
                    record.getSchoolId());

            if (!schoolInState && !hasAssignment) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied. Educational officers are isolated to schools in their state jurisdiction.");
            }

            Optional<OfficerSchoolAssignment> assignmentOpt = assignmentRepository
                    .findByOfficerCodeAndSchoolIdAndActiveTrue(officerCode, record.getSchoolId());
            if (assignmentOpt.isPresent() && !assignmentOpt.get().getLga().equalsIgnoreCase(record.getLga().trim())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied. Specified LGA (" + record.getLga()
                                + ") does not match assigned school jurisdiction LGA: " + assignmentOpt.get().getLga());
            }
        }

        if (record.getSchoolName() == null || record.getSchoolName().isBlank()) {
            schoolRepository.findById(record.getSchoolId()).ifPresent(s -> record.setSchoolName(s.getSchoolName()));
        }

        record.setOfficerCode(officerCode);
        record.setCreatedAt(OffsetDateTime.now());

        return studentRecordRepository.save(record);
    }

    /**
     * Updates an existing student record within officer's jurisdiction.
     */
    @Transactional
    public StudentRecord updateStudentRecord(Long id, StudentRecord updatedData, String officerCode,
            Authentication auth) {
        StudentRecord existing = studentRecordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Student record not found with ID: " + id));

        if (!isSuperAdmin(auth)) {
            String officerState = resolveOfficerState(officerCode);
            Optional<School> schoolOpt = schoolRepository.findById(existing.getSchoolId());
            boolean schoolInState = schoolOpt.isPresent() && officerState != null && schoolOpt.get().getState() != null
                    && officerState.equalsIgnoreCase(schoolOpt.get().getState().trim());
            boolean hasAssignment = assignmentRepository.existsByOfficerCodeAndSchoolIdAndActiveTrue(officerCode,
                    existing.getSchoolId());

            if (!schoolInState && !hasAssignment) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied. You cannot modify records outside your state jurisdiction.");
            }
        }

        if (updatedData.getStudentName() != null) existing.setStudentName(updatedData.getStudentName());
        if (updatedData.getStateOfOrigin() != null) existing.setStateOfOrigin(updatedData.getStateOfOrigin());
        if (updatedData.getHealthConditionsSpecialNeeds() != null)
            existing.setHealthConditionsSpecialNeeds(updatedData.getHealthConditionsSpecialNeeds());
        if (updatedData.getAttendanceRate() != null) existing.setAttendanceRate(updatedData.getAttendanceRate());
        if (updatedData.getFamilyGuardianBackground() != null)
            existing.setFamilyGuardianBackground(updatedData.getFamilyGuardianBackground());
        if (updatedData.getAcademicYear() != null) existing.setAcademicYear(updatedData.getAcademicYear());
        if (updatedData.getClassGrade() != null) existing.setClassGrade(updatedData.getClassGrade());
        existing.setUpdatedAt(OffsetDateTime.now());

        return studentRecordRepository.save(existing);
    }

    /**
     * Retrieves a single student record by ID, enforcing jurisdiction checks.
     */
    @Transactional(readOnly = true)
    public StudentRecord getStudentRecordById(Long id, String officerCode, Authentication auth) {
        StudentRecord record = studentRecordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Student record not found with ID: " + id));

        if (!isSuperAdmin(auth)) {
            String officerState = resolveOfficerState(officerCode);
            Optional<School> schoolOpt = schoolRepository.findById(record.getSchoolId());
            boolean schoolInState = schoolOpt.isPresent() && officerState != null && schoolOpt.get().getState() != null
                    && officerState.equalsIgnoreCase(schoolOpt.get().getState().trim());
            boolean hasAssignment = assignmentRepository.existsByOfficerCodeAndSchoolIdAndActiveTrue(officerCode,
                    record.getSchoolId());

            if (!schoolInState && !hasAssignment) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied. Record outside your state jurisdiction.");
            }
        }

        return record;
    }

    /**
     * Queries student records scoped to the officer's assigned School and LGA.
     */
    @Transactional(readOnly = true)
    public List<StudentRecord> getStudentRecords(Long schoolId, String lga, String officerCode, Authentication auth) {
        if (isSuperAdmin(auth)) {
            if (schoolId != null && lga != null && !lga.isBlank()) {
                return studentRecordRepository.findAllBySchoolIdAndLgaIgnoreCase(schoolId, lga.trim());
            } else if (schoolId != null) {
                return studentRecordRepository.findAllBySchoolId(schoolId);
            } else if (lga != null && !lga.isBlank()) {
                return studentRecordRepository.findAllByLgaIgnoreCase(lga.trim());
            }
            return studentRecordRepository.findAll();
        }

        List<OfficerSchoolAssignment> assignments = assignmentRepository.findAllByOfficerCodeAndActiveTrue(officerCode);
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> assignedSchoolIds = assignments.stream()
                .map(OfficerSchoolAssignment::getSchoolId)
                .collect(Collectors.toSet());

        Set<String> assignedLgas = assignments.stream()
                .map(a -> a.getLga().toLowerCase())
                .collect(Collectors.toSet());

        if (schoolId != null && !assignedSchoolIds.contains(schoolId)) {
            return Collections.emptyList();
        }
        if (lga != null && !lga.isBlank() && !assignedLgas.contains(lga.trim().toLowerCase())) {
            return Collections.emptyList();
        }

        if (schoolId != null) {
            return studentRecordRepository.findAllBySchoolId(schoolId);
        }

        if (lga != null && !lga.isBlank()) {
            return studentRecordRepository.findAllBySchoolIdIn(assignedSchoolIds).stream()
                    .filter(r -> r.getLga().equalsIgnoreCase(lga.trim()))
                    .collect(Collectors.toList());
        }

        return studentRecordRepository.findAllBySchoolIdIn(assignedSchoolIds);
    }

    /**
     * Administrative method to assign an educational officer to a school & LGA jurisdiction.
     */
    @Transactional
    public OfficerSchoolAssignment assignOfficerToSchool(String officerCode, Long schoolId, String schoolName,
            String lga, String state) {
        OfficerSchoolAssignment assignment = new OfficerSchoolAssignment();
        assignment.setOfficerCode(officerCode.trim());
        assignment.setSchoolId(schoolId);
        assignment.setSchoolName(schoolName);
        assignment.setLga(lga.trim());
        assignment.setState(state);
        assignment.setAssignedAt(OffsetDateTime.now());
        assignment.setActive(true);

        return assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<OfficerSchoolAssignment> getOfficerAssignments(String officerCode) {
        return assignmentRepository.findAllByOfficerCodeAndActiveTrue(officerCode);
    }

    @Transactional(readOnly = true)
    public List<OfficerSchoolAssignment> getAllActiveAssignments() {
        return assignmentRepository.findAllByActiveTrue();
    }
}
