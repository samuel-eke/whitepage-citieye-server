package com.goldbyte.citieye_server.health.internal.service;

import com.goldbyte.citieye_server.health.internal.model.HealthQuestion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HealthQuestionRegistry {
 
    private final List<HealthQuestion> earlyChildhoodQuestions = new ArrayList<>();
    private final List<HealthQuestion> schoolAgeQuestions = new ArrayList<>();
    private final List<HealthQuestion> adolescentQuestions = new ArrayList<>();
    private final List<HealthQuestion> commonQuestions = new ArrayList<>();
 
    public HealthQuestionRegistry() {
        initializeCommonQuestions();
        initializeEarlyChildhoodQuestions();
        initializeSchoolAgeQuestions();
        initializeAdolescentQuestions();
    }
 
    public List<HealthQuestion> getQuestionsForType(String type) {
        List<HealthQuestion> combined = new ArrayList<>(commonQuestions);
        if ("early_childhood".equalsIgnoreCase(type)) {
            combined.addAll(earlyChildhoodQuestions);
            return combined;
        } else if ("school_age".equalsIgnoreCase(type)) {
            combined.addAll(schoolAgeQuestions);
            return combined;
        } else if ("adolescent".equalsIgnoreCase(type)) {
            combined.addAll(adolescentQuestions);
            return combined;
        } else if ("all".equalsIgnoreCase(type) || type == null) {
            return combined;
        }
        throw new IllegalArgumentException("Invalid health category type. Must be 'early_childhood', 'school_age', 'adolescent', or 'all'.");
    }

    private void initializeEarlyChildhoodQuestions() {
        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyHasReceivedVaccines")
                .questionText("Has the child received any vaccines?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyBcgVaccinationCompleted")
                .questionText("BCG vaccination completed?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyPolioVaccinationSeries")
                .questionText("Polio vaccination series (doses received)")
                .dataType("JSON")
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyDptPentavalentSeries")
                .questionText("DPT/Pentavalent series (doses received)")
                .dataType("JSON")
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyMeaslesMmrVaccination")
                .questionText("Measles/MMR vaccination")
                .dataType("JSON")
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyYellowFeverVaccination")
                .questionText("Yellow fever vaccination")
                .dataType("JSON")
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyAllVaccinesUptodate")
                .questionText("Up-to-date with all scheduled vaccines for age?")
                .dataType("JSON")
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyVaccinationCardAvailable")
                .questionText("Vaccination card/record available for verification?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyExclusivelyBreastfed6months")
                .questionText("Exclusively breastfed for first 6 months?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyCurrentlyBreastfeeding")
                .questionText("Currently breastfeeding?")
                .dataType("JSON")
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyReceivingSupplementaryFoods")
                .questionText("Receiving fortified/supplementary foods?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyBirthWeight")
                .questionText("Child's weight at birth")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyCurrentWeightHeight")
                .questionText("Current weight and height")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlySevereMalnutritionSigns")
                .questionText("Signs of severe malnutrition")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyReceivesSupplementaryNutritionProgram")
                .questionText("Receives supplementary nutrition from government program (e.g., WINNE)?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyEverHospitalized")
                .questionText("Ever hospitalized?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyCurrentHealthCondition")
                .questionText("Current health condition")
                .dataType("STRING")
                .choices(List.of("Healthy", "Cough/Cold", "Fever", "Diarrhea", "Rash", "Other"))
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyMalariaTreatment3months")
                .questionText("Received treatment for malaria in past 3 months?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyDiarrhealDiseaseTreatment3months")
                .questionText("Received treatment for diarrheal disease in past 3 months?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyCongenitalChronicCondition")
                .questionText("Any congenital/chronic health condition diagnosed?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyMotherAgeAtBirth")
                .questionText("Mother's age at child's birth")
                .dataType("NUMBER")
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyMotherReceivedAnc")
                .questionText("Mother received antenatal care (ANC)?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyDeliverySetting")
                .questionText("Mother had facility delivery or home delivery?")
                .dataType("STRING")
                .choices(List.of("Hospital/Clinic", "Home", "Traditional Birth Attendant", "Other"))
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyMotherReceivedPnc")
                .questionText("Mother received postnatal care (PNC)?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyDevelopmentalMilestonesMet")
                .questionText("Age-appropriate developmental milestones met?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        earlyChildhoodQuestions.add(HealthQuestion.builder()
                .fieldName("earlyDevelopmentalConcerns")
                .questionText("Any concerns about child's development?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());
    }

    private void initializeSchoolAgeQuestions() {
        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolDptTdBooster")
                .questionText("Received DPT/Td booster?")
                .dataType("JSON")
                .ndprSensitive(false)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolMeaslesYfBooster")
                .questionText("Received measles/yellow fever booster?")
                .dataType("JSON")
                .ndprSensitive(false)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolVaccinationCardAvailable")
                .questionText("Vaccination card still available?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolCurrentHeightWeight")
                .questionText("Current height and weight")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolMalnutritionSigns")
                .questionText("Signs of malnutrition")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolReceivesSchoolMeals")
                .questionText("Receives school meals?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolDietaryRestrictionsAllergies")
                .questionText("Any dietary restrictions or allergies?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolCurrentSymptoms")
                .questionText("Currently has symptoms?")
                .dataType("STRING")
                .choices(List.of("None", "Cough", "Fever", "Rash", "Diarrhea", "Other"))
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolParasiticInfectionTreatment")
                .questionText("Treated for parasitic infection (worms, schistosomiasis) in past year?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolMalariaDenguePastYear")
                .questionText("Had malaria/dengue fever in past year?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolPermanentHealthCondition")
                .questionText("Has permanent health condition?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolIllnessAbsencesPastTerm")
                .questionText("Any school absences due to illness in past term?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolFeelsSafeHealthyAtSchool")
                .questionText("Feels safe and healthy at school?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolWaterSanitationAtSchool")
                .questionText("Access to water and sanitation at school?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolCanSeeClearly")
                .questionText("Can see clearly (chalkboard from back of class)?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolCanHearClearly")
                .questionText("Can hear clearly in conversations/classroom?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolHealthAwareness")
                .questionText("Knows about handwashing / food hygiene / disease prevention?")
                .dataType("STRING")
                .choices(List.of("Yes", "Partial", "No"))
                .ndprSensitive(false)
                .build());

        schoolAgeQuestions.add(HealthQuestion.builder()
                .fieldName("schoolKnowsWhenToSeekHealthServices")
                .questionText("Aware of when to seek health services?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());
    }

    private void initializeAdolescentQuestions() {
        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentHpvCervicalCancerVaccine")
                .questionText("Received HPV/cervical cancer vaccine?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentMenstruationStarted")
                .questionText("Menstruation started?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentMenstrualCycleRegular")
                .questionText("Menstrual cycle regular?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentReceivedSexEducation")
                .questionText("Ever received sex education/reproductive health information?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentAwareContraceptionSafeSex")
                .questionText("Aware of contraception and safe sex practices?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentSexuallyActive")
                .questionText("Sexually active?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentContraceptionUse")
                .questionText("If sexually active: using contraception?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentEverPregnantFathered")
                .questionText("Ever pregnant or fathered a child?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentStiHistory")
                .questionText("Any history of sexually transmitted infection (STI)?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentDepressionAnxietyWorry")
                .questionText("Feels sad, anxious, or worried most of the time?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentSelfHarmSuicidalThoughts")
                .questionText("Ever had thoughts of self-harm or suicide?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentTraumaAbuseExperience")
                .questionText("Experienced trauma or abuse?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentAccessMentalHealthSupport")
                .questionText("Has access to mental health counselor/support?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentFeelsSupported")
                .questionText("Feels supported by family/friends?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentTobaccoCigaretteUse")
                .questionText("Ever used tobacco/cigarettes?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentAlcoholUse")
                .questionText("Ever used alcohol?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentDrugUse")
                .questionText("Ever used drugs?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentScreenTimeInternetUsage")
                .questionText("Screen time/internet usage hours per day?")
                .dataType("NUMBER")
                .ndprSensitive(false)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentCurrentHeightWeight")
                .questionText("Height and weight")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentFamilyHistoryChronicDisease")
                .questionText("Any family history of chronic disease?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentPhysicalActivityHours")
                .questionText("Physical activity: how many hours of exercise per week?")
                .dataType("NUMBER")
                .ndprSensitive(false)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentDietaryHabits")
                .questionText("Dietary habits")
                .dataType("STRING")
                .choices(List.of("Mostly Healthy", "Mixed", "Mostly Unhealthy"))
                .ndprSensitive(false)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentChronicHealthCondition")
                .questionText("Any chronic health condition?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentDisability")
                .questionText("Any disability?")
                .dataType("JSON")
                .ndprSensitive(true)
                .build());

        adolescentQuestions.add(HealthQuestion.builder()
                .fieldName("adolescentDisabilitySupportAtSchool")
                .questionText("If disabled, receiving appropriate support at school/home?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());
    }

    private void initializeCommonQuestions() {
        commonQuestions.add(HealthQuestion.builder()
                .fieldName("commonBloodGroup")
                .questionText("What is the citizen's blood group?")
                .dataType("STRING")
                .choices(List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"))
                .ndprSensitive(true)
                .build());

        commonQuestions.add(HealthQuestion.builder()
                .fieldName("commonBloodGenotype")
                .questionText("What is the citizen's blood genotype?")
                .dataType("STRING")
                .choices(List.of("AA", "AS", "SS", "AC", "SC"))
                .ndprSensitive(true)
                .build());

        commonQuestions.add(HealthQuestion.builder()
                .fieldName("commonIsExpectantParent")
                .questionText("Is the citizen an expectant parent?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        commonQuestions.add(HealthQuestion.builder()
                .fieldName("commonIsPregnant")
                .questionText("Is the citizen pregnant? (females only)")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        commonQuestions.add(HealthQuestion.builder()
                .fieldName("commonHistoryOfDiabetes")
                .questionText("Does the citizen have any history of diabetes?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        commonQuestions.add(HealthQuestion.builder()
                .fieldName("commonHereditaryDisease")
                .questionText("Does the citizen have any hereditary disease? (Specify details or 'None')")
                .dataType("STRING")
                .ndprSensitive(true)
                .build());

        commonQuestions.add(HealthQuestion.builder()
                .fieldName("commonNhisEnrolled")
                .questionText("Is the citizen enrolled in the National Health Insurance Scheme (NHIS)?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        commonQuestions.add(HealthQuestion.builder()
                .fieldName("commonNhisSeniorPlanActive")
                .questionText("Is the citizen's NHIS Senior Plan active?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());
    }
}
