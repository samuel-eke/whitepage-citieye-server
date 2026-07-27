package com.goldbyte.citieye_server.education.internal.service;

import com.goldbyte.citieye_server.education.internal.model.EducationQuestion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EducationQuestionRegistry {

    private final List<EducationQuestion> primaryQuestions = new ArrayList<>();
    private final List<EducationQuestion> secondaryQuestions = new ArrayList<>();

    public EducationQuestionRegistry() {
        initializePrimaryQuestions();
        initializeSecondaryQuestions();
    }

    public List<EducationQuestion> getQuestionsForType(String type) {
        if ("primary".equalsIgnoreCase(type)) {
            return primaryQuestions;
        } else if ("secondary".equalsIgnoreCase(type)) {
            return secondaryQuestions;
        }
        throw new IllegalArgumentException("Invalid education category type. Must be 'primary' or 'secondary'.");
    }

    private void initializePrimaryQuestions() {
        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryClassGrade")
                .questionText("What is the student's current class/grade level?")
                .dataType("STRING")
                .choices(List.of("Primary 1", "Primary 2", "Primary 3", "Primary 4", "Primary 5", "Primary 6"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryAttendanceRateTier")
                .questionText("What is the student's attendance rate based on school records?")
                .dataType("STRING")
                .choices(List.of(">80%", "60–80%", "<60%"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryEnglishLiteracyPass")
                .questionText("Can the student read simple sentences in English?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryBasicNumeracyPass")
                .questionText("Can the student perform basic numeracy (add/subtract single digits)?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryMaterialsAccess")
                .questionText("Does the student have school-provided learning materials (textbooks, exercise books)?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryClassRepeating")
                .questionText("Is the student repeating this class?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryEverDroppedOut")
                .questionText("Has the student ever dropped out of school before?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primarySchoolFeedingRecipient")
                .questionText("Does the student receive the school feeding program?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryAbsentDueToHunger")
                .questionText("Has the student ever been absent due to hunger or malnutrition symptoms?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryCommuteDistance")
                .questionText("What is the walking distance/time to school?")
                .dataType("STRING")
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryGuardianEducation")
                .questionText("What is the guardian's highest level of education?")
                .dataType("STRING")
                .choices(List.of("Primary", "Secondary", "Tertiary", "None"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryGuardianHomeworkSupport")
                .questionText("Does the guardian support homework/reading at home?")
                .dataType("STRING")
                .choices(List.of("Rarely", "Sometimes", "Often"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primarySiblingInSchoolCount")
                .questionText("Are there any siblings currently in school? If yes, how many?")
                .dataType("NUMBER")
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primarySubjectStrengths")
                .questionText("What are the student's subject strengths? (Select top 2)")
                .dataType("STRING")
                .choices(List.of("Math", "English", "Science", "Social Studies", "Languages", "Arts"))
                .ndprSensitive(false)
                .build());

        primaryQuestions.add(EducationQuestion.builder()
                .fieldName("primaryVocationalInterest")
                .questionText("Is the student interested in learning outside traditional subjects?")
                .dataType("STRING")
                .choices(List.of("Sports", "Music", "ICT", "Crafts", "Agriculture", "None"))
                .ndprSensitive(false)
                .build());
    }

    private void initializeSecondaryQuestions() {
        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secClassGrade")
                .questionText("What is the student's current class/year?")
                .dataType("STRING")
                .choices(List.of("JSS 1", "JSS 2", "JSS 3", "SS 1", "SS 2", "SS 3"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secSchoolType")
                .questionText("What type of school does the student attend?")
                .dataType("STRING")
                .choices(List.of("Public", "Private", "Missionary", "Technical", "Boarding"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secEnrolled")
                .questionText("Is the student currently enrolled in school?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secAttendanceRateTier")
                .questionText("What is the student's attendance rate based on records?")
                .dataType("STRING")
                .choices(List.of(">80%", "60–80%", "<60%"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secSuspensionHistory")
                .questionText("Has the student ever been suspended or expelled?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secClassRepeating")
                .questionText("Is the student repeating this class?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secExamsRegistered")
                .questionText("Is the student taking national/technical exams this year?")
                .dataType("STRING")
                .choices(List.of("WAEC", "NECO", "JAMB", "Technical", "None"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secExamTutoringAccess")
                .questionText("Has the student received exam prep support/tutoring?")
                .dataType("STRING")
                .choices(List.of("School-based", "Private", "None"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secPostSecondaryPlan")
                .questionText("What are the student's plans after secondary school?")
                .dataType("STRING")
                .choices(List.of("University", "Polytechnic", "Technical Institute", "Apprenticeship", "Work", "Undecided"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secCareerInterests")
                .questionText("What are the student's top career interest areas?")
                .dataType("STRING")
                .choices(List.of("Health", "Education", "Engineering", "Business", "ICT", "Agriculture", "Trades", "Creative Arts", "Other"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secGuardianEmployment")
                .questionText("What is the guardian's current employment status?")
                .dataType("STRING")
                .choices(List.of("Employed", "Self-employed", "Farming", "Unemployed", "Deceased", "Other"))
                .ndprSensitive(true)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secFeesPaidInFull")
                .questionText("Have school fees been paid in full this term?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secWorkingWhileInSchool")
                .questionText("Is the student working while attending school?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(true)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secDailyChoresBurden")
                .questionText("How much time does the student spend on daily household chores/care duties?")
                .dataType("STRING")
                .choices(List.of("None", "<2 hours", "2–4 hours", ">4 hours"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secAttendanceBarriers")
                .questionText("Are there any barriers to attending school safely?")
                .dataType("STRING")
                .choices(List.of("Bullying", "Harassment", "Distance", "Cost", "Health", "None"))
                .ndprSensitive(true)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secCounselorAccess")
                .questionText("Does the student have access to a school-based counselor or mentor?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secDropoutPressureReasons")
                .questionText("Has the student ever felt pressured to drop out?")
                .dataType("STRING")
                .choices(List.of("Family Economics", "Early Marriage", "Pregnancy", "Peer Pressure", "No Pressure"))
                .ndprSensitive(true)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secExtracurricularActivities")
                .questionText("Is the student involved in any school club, sport, music, or debate?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secDigitalLiteracyCheck")
                .questionText("Can the student use a computer or phone for basic tasks (email, web search, doc creation)?")
                .dataType("BOOLEAN")
                .choices(List.of("Yes", "No"))
                .ndprSensitive(false)
                .build());

        secondaryQuestions.add(EducationQuestion.builder()
                .fieldName("secSpokenLanguages")
                .questionText("What languages are spoken by the student?")
                .dataType("STRING")
                .ndprSensitive(false)
                .build());
    }
}
