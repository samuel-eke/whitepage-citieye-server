package com.goldbyte.citieye_server.health.internal.controller;

import com.goldbyte.citieye_server.citizen_profile.CitizenProfilePublicService;
import com.goldbyte.citieye_server.citizen_profile.CitizenTargetingParams;
import com.goldbyte.citieye_server.health.internal.model.HealthRecord;
import com.goldbyte.citieye_server.health.internal.repo.HealthRecordRepository;
import com.goldbyte.citieye_server.health.internal.service.HealthQuestionRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;
import com.goldbyte.citieye_server.health.HealthRecordLoggedEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Map;

@RestController
@RequestMapping("/api/field-officers/health")
public class HealthRecordController {

    @PersistenceContext
    private EntityManager em;

    private final HealthRecordRepository healthRecordRepository;
    private final CitizenProfilePublicService citizenProfilePublicService;
    private final HealthQuestionRegistry healthQuestionRegistry;
    private final ApplicationEventPublisher eventPublisher;

    public HealthRecordController(HealthRecordRepository healthRecordRepository,
                                  CitizenProfilePublicService citizenProfilePublicService,
                                  HealthQuestionRegistry healthQuestionRegistry,
                                  ApplicationEventPublisher eventPublisher) {
        this.healthRecordRepository = healthRecordRepository;
        this.citizenProfilePublicService = citizenProfilePublicService;
        this.healthQuestionRegistry = healthQuestionRegistry;
        this.eventPublisher = eventPublisher;
    }

    private record OfficerStateInfo(String role, String stateOfResidence) {}

    private OfficerStateInfo resolveOfficerInfo(String officerCode) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT role::text, state_of_residence::text FROM auth.admin_user WHERE citizen_code = :code"
        ).setParameter("code", officerCode).getResultList();
        if (rows.isEmpty()) return null;
        Object[] row = rows.get(0);
        return new OfficerStateInfo((String) row[0], (String) row[1]);
    }

    private String resolveAgeGroup(LocalDate dob) {
        if (dob == null) return null;
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age >= 0 && age <= 5) return "early_childhood";
        if (age >= 6 && age <= 11) return "school_age";
        if (age >= 12 && age <= 18) return "adolescent";
        return null;
    }

    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions(@RequestParam("citizenCode") String citizenCode) {
        if (citizenCode == null || citizenCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Citizen code is required."));
        }

        CitizenTargetingParams citizenParams = citizenProfilePublicService.getTargetingParams(citizenCode.trim());
        if (citizenParams.getStateResidence() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Citizen profile not found."));
        }

        LocalDate dob = citizenParams.getDateOfBirth();
        if (dob == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Citizen date of birth is not registered."));
        }

        String type = resolveAgeGroup(dob);
        if (type == null) {
            return ResponseEntity.ok(healthQuestionRegistry.getQuestionsForType("all"));
        }

        return ResponseEntity.ok(healthQuestionRegistry.getQuestionsForType(type));
    }

    @GetMapping("/records/{citizenCode}")
    public ResponseEntity<?> getHealthRecords(@PathVariable String citizenCode) {
        if (citizenCode == null || citizenCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Citizen code is required."));
        }

        String officerCode = SecurityContextHolder.getContext().getAuthentication().getName();
        if (officerCode == null || "anonymousUser".equalsIgnoreCase(officerCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized. Please log in."));
        }

        OfficerStateInfo officerInfo = resolveOfficerInfo(officerCode);
        if (officerInfo == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Could not resolve officer details."));
        }

        CitizenTargetingParams citizenParams = citizenProfilePublicService.getTargetingParams(citizenCode);
        if (citizenParams.getStateResidence() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Citizen profile not found."));
        }

        boolean isSuperAdmin = officerInfo.role().contains("super");
        if (!isSuperAdmin && !officerInfo.stateOfResidence().equalsIgnoreCase(citizenParams.getStateResidence())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You can only access records for citizens in your state of residence: " + officerInfo.stateOfResidence()));
        }

        List<HealthRecord> records = healthRecordRepository.findAllByCitizenCode(citizenCode.trim());
        return ResponseEntity.ok(records);
    }

    @PostMapping("/records")
    public ResponseEntity<?> logHealthRecord(@Valid @RequestBody HealthRecordRequest request) {
        String officerCode = SecurityContextHolder.getContext().getAuthentication().getName();

        if (officerCode == null || "anonymousUser".equalsIgnoreCase(officerCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized. Please log in as a field officer."));
        }

        // Validate target citizen exists
        if (!citizenProfilePublicService.profileExists(request.getCitizenCode())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid citizen code. Citizen profile not found."));
        }

        OfficerStateInfo officerInfo = resolveOfficerInfo(officerCode);
        if (officerInfo == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Could not resolve officer details."));
        }

        CitizenTargetingParams citizenParams = citizenProfilePublicService.getTargetingParams(request.getCitizenCode());
        boolean isSuperAdmin = officerInfo.role().contains("super");
        if (!isSuperAdmin && !officerInfo.stateOfResidence().equalsIgnoreCase(citizenParams.getStateResidence())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You can only log records for citizens in your state of residence: " + officerInfo.stateOfResidence()));
        }

        // Validate age-group specific fields
        LocalDate dob = citizenParams.getDateOfBirth();
        String ageGroup = resolveAgeGroup(dob);

        if ("early_childhood".equals(ageGroup)) {
            if (request.hasSchoolAgeFields() || request.hasAdolescentFields()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid fields. Citizen age matches Early Childhood group. School age or Adolescent fields cannot be logged."));
            }
        } else if ("school_age".equals(ageGroup)) {
            if (request.hasEarlyChildhoodFields() || request.hasAdolescentFields()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid fields. Citizen age matches School Age group. Early childhood or Adolescent fields cannot be logged."));
            }
        } else if ("adolescent".equals(ageGroup)) {
            if (request.hasEarlyChildhoodFields() || request.hasSchoolAgeFields()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid fields. Citizen age matches Adolescent group. Early childhood or School age fields cannot be logged."));
            }
        } else {
            if (request.hasEarlyChildhoodFields() || request.hasSchoolAgeFields() || request.hasAdolescentFields()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid fields. Citizen age does not fall within active childhood/adolescent questionnaire cohorts. Questionnaire fields cannot be logged."));
            }
        }

        // Create and save health record
        HealthRecord record = new HealthRecord();
        record.setCitizenCode(request.getCitizenCode().trim());
        record.setOfficerCode(officerCode);
        record.setRecordType(request.getRecordType().trim().toUpperCase());
        record.setDescription(request.getDescription().trim());
        record.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
        record.setLoggedAt(OffsetDateTime.now());

        // Map questionnaire fields
        mapQuestionnaireFields(request, record);

        HealthRecord saved = healthRecordRepository.save(record);

        eventPublisher.publishEvent(new HealthRecordLoggedEvent(
                saved.getId(),
                saved.getCitizenCode(),
                saved.getRecordType(),
                saved.getOfficerCode(),
                saved.getLoggedAt()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Health record logged successfully.",
                "recordId", saved.getId()
        ));
    }

    @PutMapping("/records/{recordId}")
    public ResponseEntity<?> updateHealthRecord(@PathVariable Long recordId, @Valid @RequestBody HealthRecordRequest request) {
        // Enforce admin privileges
        boolean hasEditPrivilege = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("super_admin") || a.getAuthority().equals("agency_admin") || a.getAuthority().equals("EDIT_HEALTH_RECORD"));

        if (!hasEditPrivilege) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. You do not have permissions to edit health records."));
        }

        return healthRecordRepository.findById(recordId)
                .map(record -> {
                    // Fetch targeting params to validate age group constraints
                    CitizenTargetingParams citizenParams = citizenProfilePublicService.getTargetingParams(record.getCitizenCode());
                    LocalDate dob = citizenParams.getDateOfBirth();
                    String ageGroup = resolveAgeGroup(dob);

                    if ("early_childhood".equals(ageGroup)) {
                        if (request.hasSchoolAgeFields() || request.hasAdolescentFields()) {
                            return ResponseEntity.badRequest().body(Map.of("message", "Invalid fields. Citizen age matches Early Childhood group. School age or Adolescent fields cannot be updated."));
                        }
                    } else if ("school_age".equals(ageGroup)) {
                        if (request.hasEarlyChildhoodFields() || request.hasAdolescentFields()) {
                            return ResponseEntity.badRequest().body(Map.of("message", "Invalid fields. Citizen age matches School Age group. Early childhood or Adolescent fields cannot be updated."));
                        }
                    } else if ("adolescent".equals(ageGroup)) {
                        if (request.hasEarlyChildhoodFields() || request.hasSchoolAgeFields()) {
                            return ResponseEntity.badRequest().body(Map.of("message", "Invalid fields. Citizen age matches Adolescent group. Early childhood or School age fields cannot be updated."));
                        }
                    } else {
                        if (request.hasEarlyChildhoodFields() || request.hasSchoolAgeFields() || request.hasAdolescentFields()) {
                            return ResponseEntity.badRequest().body(Map.of("message", "Invalid fields. Citizen age does not fall within active childhood/adolescent questionnaire cohorts. Questionnaire fields cannot be updated."));
                        }
                    }

                    record.setRecordType(request.getRecordType().trim().toUpperCase());
                    record.setDescription(request.getDescription().trim());
                    record.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

                    // Map questionnaire fields
                    mapQuestionnaireFields(request, record);

                    healthRecordRepository.save(record);
                    return ResponseEntity.ok(Map.of("message", "Health record updated successfully."));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Health record not found with ID: " + recordId)));
    }

    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<?> deleteHealthRecord(@PathVariable String recordId) {
        // Enforce super admin privileges
        boolean isSuperAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("super_admin") || a.getAuthority().equals("DELETE_HEALTH_RECORD"));

        if (!isSuperAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden. Only super administrators can delete health records."));
        }

        try {
            Long rId = Long.parseLong(recordId);
            return healthRecordRepository.findById(rId)
                    .map(record -> {
                        healthRecordRepository.delete(record);
                        return ResponseEntity.ok(Map.of("message", "Health record deleted successfully."));
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("message", "Health record not found with ID: " + recordId)));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid record ID format."));
        }
    }

    private void mapQuestionnaireFields(HealthRecordRequest request, HealthRecord record) {
        record.setEarlyHasReceivedVaccines(request.getEarlyHasReceivedVaccines());
        record.setEarlyBcgVaccinationCompleted(request.getEarlyBcgVaccinationCompleted());
        record.setEarlyPolioVaccinationSeries(request.getEarlyPolioVaccinationSeries());
        record.setEarlyDptPentavalentSeries(request.getEarlyDptPentavalentSeries());
        record.setEarlyMeaslesMmrVaccination(request.getEarlyMeaslesMmrVaccination());
        record.setEarlyYellowFeverVaccination(request.getEarlyYellowFeverVaccination());
        record.setEarlyAllVaccinesUptodate(request.getEarlyAllVaccinesUptodate());
        record.setEarlyVaccinationCardAvailable(request.getEarlyVaccinationCardAvailable());
        record.setEarlyExclusivelyBreastfed6months(request.getEarlyExclusivelyBreastfed6months());
        record.setEarlyCurrentlyBreastfeeding(request.getEarlyCurrentlyBreastfeeding());
        record.setEarlyReceivingSupplementaryFoods(request.getEarlyReceivingSupplementaryFoods());
        record.setEarlyBirthWeight(request.getEarlyBirthWeight());
        record.setEarlyCurrentWeightHeight(request.getEarlyCurrentWeightHeight());
        record.setEarlySevereMalnutritionSigns(request.getEarlySevereMalnutritionSigns());
        record.setEarlyReceivesSupplementaryNutritionProgram(request.getEarlyReceivesSupplementaryNutritionProgram());
        record.setEarlyEverHospitalized(request.getEarlyEverHospitalized());
        record.setEarlyCurrentHealthCondition(request.getEarlyCurrentHealthCondition());
        record.setEarlyMalariaTreatment3months(request.getEarlyMalariaTreatment3months());
        record.setEarlyDiarrhealDiseaseTreatment3months(request.getEarlyDiarrhealDiseaseTreatment3months());
        record.setEarlyCongenitalChronicCondition(request.getEarlyCongenitalChronicCondition());
        record.setEarlyMotherAgeAtBirth(request.getEarlyMotherAgeAtBirth());
        record.setEarlyMotherReceivedAnc(request.getEarlyMotherReceivedAnc());
        record.setEarlyDeliverySetting(request.getEarlyDeliverySetting());
        record.setEarlyMotherReceivedPnc(request.getEarlyMotherReceivedPnc());
        record.setEarlyDevelopmentalMilestonesMet(request.getEarlyDevelopmentalMilestonesMet());
        record.setEarlyDevelopmentalConcerns(request.getEarlyDevelopmentalConcerns());

        record.setSchoolDptTdBooster(request.getSchoolDptTdBooster());
        record.setSchoolMeaslesYfBooster(request.getSchoolMeaslesYfBooster());
        record.setSchoolVaccinationCardAvailable(request.getSchoolVaccinationCardAvailable());
        record.setSchoolCurrentHeightWeight(request.getSchoolCurrentHeightWeight());
        record.setSchoolMalnutritionSigns(request.getSchoolMalnutritionSigns());
        record.setSchoolReceivesSchoolMeals(request.getSchoolReceivesSchoolMeals());
        record.setSchoolDietaryRestrictionsAllergies(request.getSchoolDietaryRestrictionsAllergies());
        record.setSchoolCurrentSymptoms(request.getSchoolCurrentSymptoms());
        record.setSchoolParasiticInfectionTreatment(request.getSchoolParasiticInfectionTreatment());
        record.setSchoolMalariaDenguePastYear(request.getSchoolMalariaDenguePastYear());
        record.setSchoolPermanentHealthCondition(request.getSchoolPermanentHealthCondition());
        record.setSchoolIllnessAbsencesPastTerm(request.getSchoolIllnessAbsencesPastTerm());
        record.setSchoolFeelsSafeHealthyAtSchool(request.getSchoolFeelsSafeHealthyAtSchool());
        record.setSchoolWaterSanitationAtSchool(request.getSchoolWaterSanitationAtSchool());
        record.setSchoolCanSeeClearly(request.getSchoolCanSeeClearly());
        record.setSchoolCanHearClearly(request.getSchoolCanHearClearly());
        record.setSchoolHealthAwareness(request.getSchoolHealthAwareness());
        record.setSchoolKnowsWhenToSeekHealthServices(request.getSchoolKnowsWhenToSeekHealthServices());

        record.setAdolescentHpvCervicalCancerVaccine(request.getAdolescentHpvCervicalCancerVaccine());
        record.setAdolescentMenstruationStarted(request.getAdolescentMenstruationStarted());
        record.setAdolescentMenstrualCycleRegular(request.getAdolescentMenstrualCycleRegular());
        record.setAdolescentReceivedSexEducation(request.getAdolescentReceivedSexEducation());
        record.setAdolescentAwareContraceptionSafeSex(request.getAdolescentAwareContraceptionSafeSex());
        record.setAdolescentSexuallyActive(request.getAdolescentSexuallyActive());
        record.setAdolescentContraceptionUse(request.getAdolescentContraceptionUse());
        record.setAdolescentEverPregnantFathered(request.getAdolescentEverPregnantFathered());
        record.setAdolescentStiHistory(request.getAdolescentStiHistory());
        record.setAdolescentDepressionAnxietyWorry(request.getAdolescentDepressionAnxietyWorry());
        record.setAdolescentSelfHarmSuicidalThoughts(request.getAdolescentSelfHarmSuicidalThoughts());
        record.setAdolescentTraumaAbuseExperience(request.getAdolescentTraumaAbuseExperience());
        record.setAdolescentAccessMentalHealthSupport(request.getAdolescentAccessMentalHealthSupport());
        record.setAdolescentFeelsSupported(request.getAdolescentFeelsSupported());
        record.setAdolescentTobaccoCigaretteUse(request.getAdolescentTobaccoCigaretteUse());
        record.setAdolescentAlcoholUse(request.getAdolescentAlcoholUse());
        record.setAdolescentDrugUse(request.getAdolescentDrugUse());
        record.setAdolescentScreenTimeInternetUsage(request.getAdolescentScreenTimeInternetUsage());
        record.setAdolescentCurrentHeightWeight(request.getAdolescentCurrentHeightWeight());
        record.setAdolescentFamilyHistoryChronicDisease(request.getAdolescentFamilyHistoryChronicDisease());
        record.setAdolescentPhysicalActivityHours(request.getAdolescentPhysicalActivityHours());
        record.setAdolescentDietaryHabits(request.getAdolescentDietaryHabits());
        record.setAdolescentChronicHealthCondition(request.getAdolescentChronicHealthCondition());
        record.setAdolescentDisability(request.getAdolescentDisability());
        record.setAdolescentDisabilitySupportAtSchool(request.getAdolescentDisabilitySupportAtSchool());

        record.setCommonBloodGroup(request.getCommonBloodGroup());
        record.setCommonBloodGenotype(request.getCommonBloodGenotype());
        record.setCommonIsExpectantParent(request.getCommonIsExpectantParent());
        record.setCommonIsPregnant(request.getCommonIsPregnant());
        record.setCommonHistoryOfDiabetes(request.getCommonHistoryOfDiabetes());
        record.setCommonHereditaryDisease(request.getCommonHereditaryDisease());
        record.setCommonNhisEnrolled(request.getCommonNhisEnrolled());
        record.setCommonNhisSeniorPlanActive(request.getCommonNhisSeniorPlanActive());
    }

    @Getter
    @Setter
    public static class HealthRecordRequest {
        @NotBlank(message = "Citizen code is required")
        @Size(max = 30)
        private String citizenCode;

        @NotBlank(message = "Record type is required")
        @Size(max = 50)
        private String recordType; // VACCINATION, SCREENING, NUTRITION

        @NotBlank(message = "Description is required")
        private String description;

        private String notes;

        // --- Early Childhood Fields (Ages 0-5) ---
        private Boolean earlyHasReceivedVaccines;
        private Boolean earlyBcgVaccinationCompleted;
        private Map<String, Object> earlyPolioVaccinationSeries;
        private Map<String, Object> earlyDptPentavalentSeries;
        private Map<String, Object> earlyMeaslesMmrVaccination;
        private Map<String, Object> earlyYellowFeverVaccination;
        private Map<String, Object> earlyAllVaccinesUptodate;
        private Boolean earlyVaccinationCardAvailable;
        private Boolean earlyExclusivelyBreastfed6months;
        private Map<String, Object> earlyCurrentlyBreastfeeding;
        private Boolean earlyReceivingSupplementaryFoods;
        private Map<String, Object> earlyBirthWeight;
        private Map<String, Object> earlyCurrentWeightHeight;
        private Map<String, Object> earlySevereMalnutritionSigns;
        private Boolean earlyReceivesSupplementaryNutritionProgram;
        private Map<String, Object> earlyEverHospitalized;
        private String earlyCurrentHealthCondition;
        private Boolean earlyMalariaTreatment3months;
        private Boolean earlyDiarrhealDiseaseTreatment3months;
        private Map<String, Object> earlyCongenitalChronicCondition;
        private Integer earlyMotherAgeAtBirth;
        private Map<String, Object> earlyMotherReceivedAnc;
        private String earlyDeliverySetting;
        private Map<String, Object> earlyMotherReceivedPnc;
        private Map<String, Object> earlyDevelopmentalMilestonesMet;
        private Map<String, Object> earlyDevelopmentalConcerns;

        // --- School Age Fields (Ages 6-11) ---
        private Map<String, Object> schoolDptTdBooster;
        private Map<String, Object> schoolMeaslesYfBooster;
        private Boolean schoolVaccinationCardAvailable;
        private Map<String, Object> schoolCurrentHeightWeight;
        private Map<String, Object> schoolMalnutritionSigns;
        private Boolean schoolReceivesSchoolMeals;
        private Map<String, Object> schoolDietaryRestrictionsAllergies;
        private String schoolCurrentSymptoms;
        private Boolean schoolParasiticInfectionTreatment;
        private Map<String, Object> schoolMalariaDenguePastYear;
        private Map<String, Object> schoolPermanentHealthCondition;
        private Map<String, Object> schoolIllnessAbsencesPastTerm;
        private Map<String, Object> schoolFeelsSafeHealthyAtSchool;
        private Boolean schoolWaterSanitationAtSchool;
        private Boolean schoolCanSeeClearly;
        private Boolean schoolCanHearClearly;
        private String schoolHealthAwareness;
        private Boolean schoolKnowsWhenToSeekHealthServices;

        // --- Adolescent Fields (Ages 12-18) ---
        private Boolean adolescentHpvCervicalCancerVaccine;
        private Map<String, Object> adolescentMenstruationStarted;
        private Boolean adolescentMenstrualCycleRegular;
        private Boolean adolescentReceivedSexEducation;
        private Boolean adolescentAwareContraceptionSafeSex;
        private Boolean adolescentSexuallyActive;
        private Map<String, Object> adolescentContraceptionUse;
        private Map<String, Object> adolescentEverPregnantFathered;
        private Map<String, Object> adolescentStiHistory;
        private Map<String, Object> adolescentDepressionAnxietyWorry;
        private Boolean adolescentSelfHarmSuicidalThoughts;
        private Map<String, Object> adolescentTraumaAbuseExperience;
        private Boolean adolescentAccessMentalHealthSupport;
        private Boolean adolescentFeelsSupported;
        private Map<String, Object> adolescentTobaccoCigaretteUse;
        private Map<String, Object> adolescentAlcoholUse;
        private Boolean adolescentDrugUse;
        private BigDecimal adolescentScreenTimeInternetUsage;
        private Map<String, Object> adolescentCurrentHeightWeight;
        private Map<String, Object> adolescentFamilyHistoryChronicDisease;
        private BigDecimal adolescentPhysicalActivityHours;
        private String adolescentDietaryHabits;
        private Map<String, Object> adolescentChronicHealthCondition;
        private Map<String, Object> adolescentDisability;
        private Boolean adolescentDisabilitySupportAtSchool;

        // --- Common / All-Ages Fields ---
        private String commonBloodGroup;
        private String commonBloodGenotype;
        private Boolean commonIsExpectantParent;
        private Boolean commonIsPregnant;
        private Boolean commonHistoryOfDiabetes;
        private String commonHereditaryDisease;
        private Boolean commonNhisEnrolled;
        private Boolean commonNhisSeniorPlanActive;

        public boolean hasEarlyChildhoodFields() {
            return earlyHasReceivedVaccines != null ||
                    earlyBcgVaccinationCompleted != null ||
                    earlyPolioVaccinationSeries != null ||
                    earlyDptPentavalentSeries != null ||
                    earlyMeaslesMmrVaccination != null ||
                    earlyYellowFeverVaccination != null ||
                    earlyAllVaccinesUptodate != null ||
                    earlyVaccinationCardAvailable != null ||
                    earlyExclusivelyBreastfed6months != null ||
                    earlyCurrentlyBreastfeeding != null ||
                    earlyReceivingSupplementaryFoods != null ||
                    earlyBirthWeight != null ||
                    earlyCurrentWeightHeight != null ||
                    earlySevereMalnutritionSigns != null ||
                    earlyReceivesSupplementaryNutritionProgram != null ||
                    earlyEverHospitalized != null ||
                    earlyCurrentHealthCondition != null ||
                    earlyMalariaTreatment3months != null ||
                    earlyDiarrhealDiseaseTreatment3months != null ||
                    earlyCongenitalChronicCondition != null ||
                    earlyMotherAgeAtBirth != null ||
                    earlyMotherReceivedAnc != null ||
                    earlyDeliverySetting != null ||
                    earlyMotherReceivedPnc != null ||
                    earlyDevelopmentalMilestonesMet != null ||
                    earlyDevelopmentalConcerns != null;
        }

        public boolean hasSchoolAgeFields() {
            return schoolDptTdBooster != null ||
                    schoolMeaslesYfBooster != null ||
                    schoolVaccinationCardAvailable != null ||
                    schoolCurrentHeightWeight != null ||
                    schoolMalnutritionSigns != null ||
                    schoolReceivesSchoolMeals != null ||
                    schoolDietaryRestrictionsAllergies != null ||
                    schoolCurrentSymptoms != null ||
                    schoolParasiticInfectionTreatment != null ||
                    schoolMalariaDenguePastYear != null ||
                    schoolPermanentHealthCondition != null ||
                    schoolIllnessAbsencesPastTerm != null ||
                    schoolFeelsSafeHealthyAtSchool != null ||
                    schoolWaterSanitationAtSchool != null ||
                    schoolCanSeeClearly != null ||
                    schoolCanHearClearly != null ||
                    schoolHealthAwareness != null ||
                    schoolKnowsWhenToSeekHealthServices != null;
        }

        public boolean hasAdolescentFields() {
            return adolescentHpvCervicalCancerVaccine != null ||
                    adolescentMenstruationStarted != null ||
                    adolescentMenstrualCycleRegular != null ||
                    adolescentReceivedSexEducation != null ||
                    adolescentAwareContraceptionSafeSex != null ||
                    adolescentSexuallyActive != null ||
                    adolescentContraceptionUse != null ||
                    adolescentEverPregnantFathered != null ||
                    adolescentStiHistory != null ||
                    adolescentDepressionAnxietyWorry != null ||
                    adolescentSelfHarmSuicidalThoughts != null ||
                    adolescentTraumaAbuseExperience != null ||
                    adolescentAccessMentalHealthSupport != null ||
                    adolescentFeelsSupported != null ||
                    adolescentTobaccoCigaretteUse != null ||
                    adolescentAlcoholUse != null ||
                    adolescentDrugUse != null ||
                    adolescentScreenTimeInternetUsage != null ||
                    adolescentCurrentHeightWeight != null ||
                    adolescentFamilyHistoryChronicDisease != null ||
                    adolescentPhysicalActivityHours != null ||
                    adolescentDietaryHabits != null ||
                    adolescentChronicHealthCondition != null ||
                    adolescentDisability != null ||
                    adolescentDisabilitySupportAtSchool != null;
        }
    }
}
