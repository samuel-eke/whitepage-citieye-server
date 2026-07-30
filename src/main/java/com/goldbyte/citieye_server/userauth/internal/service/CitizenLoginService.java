package com.goldbyte.citieye_server.userauth.internal.service;

import com.goldbyte.citieye_server.userauth.internal.dto.CitizenLoginRequest;
import com.goldbyte.citieye_server.userauth.internal.dto.CitizenLoginResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.context.ApplicationEventPublisher;
import com.goldbyte.citieye_server.userauth.FetchUserAuthoritiesEvent;
import com.goldbyte.citieye_server.userauth.ProgressiveQuestionsQueryEvent;

/**
 * Service handling citizen login logic.
 * Supports multi-identifier authentication (email, NIN, phone, citizen code)
 * and assembles a comprehensive response with profile and cohort data
 * via a native cross-schema SQL query.
 */
@Service
public class CitizenLoginService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Transactional(readOnly = true)
    public CitizenLoginResponse login(CitizenLoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier == null) {
            throw new BadCredentialsException("Identifier is required");
        }
        identifier = identifier.trim();

        String sql = """
            SELECT 
                u.id AS id, 
                u.email AS email, 
                u.phone_number AS phone_number, 
                u.nin AS nin, 
                u.citizen_code AS citizen_code, 
                u.first_name AS first_name, 
                u.middle_name AS middle_name, 
                u.last_name AS last_name, 
                u.gender AS gender, 
                u.address AS address, 
                u.state_of_residence AS state_of_residence, 
                u.state_of_origin AS state_of_origin, 
                u.residence_lga AS residence_lga,
                u.role AS role, 
                u.date_of_birth AS date_of_birth, 
                u.created_at AS created_at, 
                u.password AS password,
                p.age AS age, 
                p.lga AS lga, 
                p.hometown AS hometown, 
                p.current_educational_level AS current_educational_level, 
                p.highest_educational_qualification AS highest_educational_qualification, 
                p.course_of_study AS course_of_study, 
                p.university_attended AS university_attended, 
                p.degree_attained AS degree_attained, 
                p.year_of_graduation AS year_of_graduation, 
                p.is_expectant_parent AS is_expectant_parent, 
                p.household_size AS household_size, 
                p.is_pregnant AS is_pregnant, 
                p.blood_group AS blood_group, 
                p.blood_genotype AS blood_genotype, 
                p.current_employment_status AS current_employment_status, 
                p.place_of_employment_business AS place_of_employment_business, 
                p.name_of_employer_business AS name_of_employer_business, 
                p.nature_of_business_job AS nature_of_business_job, 
                c.name AS cohort_name, 
                c.description AS cohort_description, 
                cp.age_at_registration AS age_at_registration
            FROM auth.users_auth u
            LEFT JOIN citizen_profile.profiles p ON u.citizen_code = p.citizen_code
            LEFT JOIN cohort_placement.placements cp ON u.citizen_code = cp.citizen_code
            LEFT JOIN cohort_placement.cohorts c ON cp.cohort_id = c.id
            WHERE LOWER(u.email) = LOWER(:identifier) 
               OR u.phone_number = :identifier 
               OR u.nin = :identifier 
               OR u.citizen_code = :identifier
            """;

        java.util.List<Tuple> results = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("identifier", identifier)
                .getResultList();

        if (results.isEmpty()) {
            throw new BadCredentialsException("Invalid identifier or password");
        }

        Tuple tuple = results.get(0);
        String hashedPassword = tuple.get("password", String.class);

        if (!passwordEncoder.matches(request.getPassword(), hashedPassword)) {
            throw new BadCredentialsException("Invalid identifier or password");
        }


        CitizenLoginResponse response = mapTupleToResponse(tuple);
        
        // Publish event to query authorities from RBAC module to populate the response for client/UI usage
        FetchUserAuthoritiesEvent event = new FetchUserAuthoritiesEvent(response.getRole(), response.getCitizenCode());
        eventPublisher.publishEvent(event);
        response.setPermissions(event.getAuthorities());

        // Publish event to citizen_profile module to resolve pending progressive profile questions
        // for the citizen's current age cohort. Returns only fields that are still null on their profile.
        if (response.getAge() != null) {
            ProgressiveQuestionsQueryEvent questionsEvent =
                    new ProgressiveQuestionsQueryEvent(response.getCitizenCode(), response.getAge());
            eventPublisher.publishEvent(questionsEvent);
            response.setPendingQuestions(questionsEvent.getQuestions());
        }

        // Generate JWT Token
        java.util.List<String> tokenAuthorities = new java.util.ArrayList<>();
        if (response.getRole() != null) {
            tokenAuthorities.add(response.getRole());
        }
        if (event.getAuthorities() != null) {
            tokenAuthorities.addAll(event.getAuthorities());
        }
        String token = jwtTokenService.generateToken(response.getCitizenCode(), tokenAuthorities);
        response.setData(token);

        return response;
    }

    private CitizenLoginResponse mapTupleToResponse(Tuple tuple) {
        Long id = tuple.get("id") != null ? ((Number) tuple.get("id")).longValue() : null;
        String email = tuple.get("email", String.class);
        String phoneNumber = tuple.get("phone_number", String.class);
        String nin = tuple.get("nin", String.class);
        String citizenCode = tuple.get("citizen_code", String.class);
        String firstName = tuple.get("first_name", String.class);
        String middleName = tuple.get("middle_name", String.class);
        String lastName = tuple.get("last_name", String.class);
        String gender = tuple.get("gender") != null ? String.valueOf(tuple.get("gender")) : null;
        String address = tuple.get("address", String.class);
        String stateOfResidence = tuple.get("state_of_residence") != null ? String.valueOf(tuple.get("state_of_residence")) : null;
        String stateOfOrigin = tuple.get("state_of_origin") != null ? String.valueOf(tuple.get("state_of_origin")) : null;
        String residenceLga = tuple.get("residence_lga", String.class);
        String role = tuple.get("role") != null ? String.valueOf(tuple.get("role")) : null;

        // Safe Date Mapping
        Object dobObj = tuple.get("date_of_birth");
        java.time.LocalDate dateOfBirth = null;
        if (dobObj instanceof java.sql.Date) {
            dateOfBirth = ((java.sql.Date) dobObj).toLocalDate();
        } else if (dobObj instanceof java.time.LocalDate) {
            dateOfBirth = (java.time.LocalDate) dobObj;
        }

        // Safe Created At Mapping
        Object createdAtObj = tuple.get("created_at");
        java.time.OffsetDateTime createdAt = null;
        if (createdAtObj instanceof java.sql.Timestamp) {
            createdAt = ((java.sql.Timestamp) createdAtObj).toInstant().atOffset(java.time.ZoneOffset.UTC);
        } else if (createdAtObj instanceof java.time.OffsetDateTime) {
            createdAt = (java.time.OffsetDateTime) createdAtObj;
        } else if (createdAtObj instanceof java.time.Instant) {
            createdAt = ((java.time.Instant) createdAtObj).atOffset(java.time.ZoneOffset.UTC);
        }

        // Profile fields
        Integer age = tuple.get("age") != null ? ((Number) tuple.get("age")).intValue() : null;
        String lga = tuple.get("lga", String.class);
        String hometown = tuple.get("hometown", String.class);
        String currentEducationalLevel = tuple.get("current_educational_level", String.class);
        String highestEducationalQualification = tuple.get("highest_educational_qualification", String.class);
        String courseOfStudy = tuple.get("course_of_study", String.class);
        String universityAttended = tuple.get("university_attended", String.class);
        String degreeAttained = tuple.get("degree_attained", String.class);
        Integer yearOfGraduation = tuple.get("year_of_graduation") != null ? ((Number) tuple.get("year_of_graduation")).intValue() : null;
        Boolean isExpectantParent = tuple.get("is_expectant_parent") != null ? (Boolean) tuple.get("is_expectant_parent") : null;
        Integer householdSize = tuple.get("household_size") != null ? ((Number) tuple.get("household_size")).intValue() : null;
        Boolean isPregnant = tuple.get("is_pregnant") != null ? (Boolean) tuple.get("is_pregnant") : null;
        String bloodGroup = tuple.get("blood_group", String.class);
        String bloodGenotype = tuple.get("blood_genotype", String.class);
        String currentEmploymentStatus = tuple.get("current_employment_status") != null ? String.valueOf(tuple.get("current_employment_status")) : null;
        String placeOfEmploymentBusiness = tuple.get("place_of_employment_business", String.class);
        String nameOfEmployerBusiness = tuple.get("name_of_employer_business", String.class);
        String natureOfBusinessJob = tuple.get("nature_of_business_job", String.class);

        // Cohort Placement
        String cohortName = tuple.get("cohort_name", String.class);
        String cohortDescription = tuple.get("cohort_description", String.class);
        Integer ageAtRegistration = tuple.get("age_at_registration") != null ? ((Number) tuple.get("age_at_registration")).intValue() : null;

        return CitizenLoginResponse.builder()
                .id(id)
                .email(email)
                .phoneNumber(phoneNumber)
                .nin(nin)
                .citizenCode(citizenCode)
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .gender(gender)
                .address(address)
                .stateOfResidence(stateOfResidence)
                .stateOfOrigin(stateOfOrigin)
                .role(role)
                .dateOfBirth(dateOfBirth)
                .createdAt(createdAt)
                .age(age)
                .lga(lga)
                .residenceLga(residenceLga)
                .hometown(hometown)
                .currentEducationalLevel(currentEducationalLevel)
                .highestEducationalQualification(highestEducationalQualification)
                .courseOfStudy(courseOfStudy)
                .universityAttended(universityAttended)
                .degreeAttained(degreeAttained)
                .yearOfGraduation(yearOfGraduation)
                .isExpectantParent(isExpectantParent)
                .householdSize(householdSize)
                .isPregnant(isPregnant)
                .bloodGroup(bloodGroup)
                .bloodGenotype(bloodGenotype)
                .currentEmploymentStatus(currentEmploymentStatus)
                .placeOfEmploymentBusiness(placeOfEmploymentBusiness)
                .nameOfEmployerBusiness(nameOfEmployerBusiness)
                .natureOfBusinessJob(natureOfBusinessJob)
                .cohortName(cohortName)
                .cohortDescription(cohortDescription)
                .ageAtRegistration(ageAtRegistration)
                .message("Login successful")
                .build();
    }
}
