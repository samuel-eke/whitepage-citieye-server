# CitiEye Project - AGENTS.md

Welcome! This document defines the architectural guidelines, build/test commands, testing conventions, database strategies, and specialized agent roles for the CitiEye Modular Monolith.

---

## 1. Project Overview & Tech Stack

*   **Application Name**: `citieye_server`
*   **Architecture**: Modular Monolith using **Spring Modulith**
*   **Java Version**: `21`
*   **Spring Boot Version**: `4.0.6`
*   **Spring Modulith Version**: `2.0.6`
*   **Build Tool**: System Maven (`mvn`)
*   **Database**: PostgreSQL (driver-class-name: `org.postgresql.Driver`)
*   **Authentication & Security**: Spring Security & JWT (`jjwt` version `0.13.0`)
*   **Boilerplate reduction**: Project Lombok (compile scope to be set to `provided`)

---

## 2. Modular Architecture & Boundaries

The application is structured into distinct modules under `com.goldbyte.citieye_server`.

### Strict Module Isolation
*   Every package directly under `com.goldbyte.citieye_server` (e.g. `userauth`, `citizen_profile`) is an independent module.
*   **No Cross-Module Package Imports**: Access to another module's `internal` packages is strictly prohibited by modulith boundary verification.
*   **Decoupled Integration**: High-level cross-module dependencies should utilize **Spring Modulith Application Events** (event publishers and event listeners) rather than direct service injection.

### Database Schema Segregation
Each modulith module should map to its own dedicated PostgreSQL schema to maintain database-level boundaries. The naming convention for the schema should be the same as the module name.

---

## 3. Build & Test Commands

Agents must run these commands to compile and verify changes:
*   **Full Build**: `mvn clean package`
*   **Run All Tests**: `mvn clean test`
*   **Modulith Verification Test**: `mvn test -Dtest=*Modulith*` (or the specific test class that triggers `ApplicationModules.verify()`)

---

## 4. Testing Conventions

*   **Modulith Modularity Verification**: Every build must pass boundary verification via:
    ```java
    ApplicationModules.of(CitieyeServerApplication.class).verify();
    ```
*   **Slice Testing**: Avoid booting up the full application context when testing individual modules. Use specialized test annotations:
    *   `@ApplicationModuleTest` for Modulith integration testing.
    *   `@DataJpaTest` for database repository tests.
    *   `@WebMvcTest` for controller endpoint tests.
*   **Domain Test Isolation**: Test classes for one module must not instantiate services or entities from another module directly. Use API abstractions or mock listeners for published events.


---

## 5. Project Configuration Guidelines

*   **Lombok Dependency Scope**: Ensure `lombok` is configured with `provided` scope in `pom.xml` instead of `annotationProcessor` to maintain compiler and IDE annotation processing compatibility.

---

## 6. Specialized Agent Roles (for Collaborative Workflows)

When collaborative or parallel agents are spawned (e.g., via `/teamwork-preview`), they should be assigned specific roles according to the CitiEye implementation roadmap:

### Database Creation and Optimization Agent
*   **Focus**: Creation and optimization of database schemas and tables.
*   **Modules**: All modules.
*   **Key Responsibilities**: Creation of database schema, optimization of database schema and tables, ensuring database-level boundaries are maintained. Implement any necessary trigger or extra database functionality necessary for the application code been developed to function optimally.

### 👤 Identity, Access & RBAC (IAM) Developer
*   **Focus**: Phase 1 implementation.
*   **Modules**: `userauth`, `rbac`.
*   **Key Responsibilities**: Secure registration, login, role definitions, unique user code generation, JWT stateless authentication, duplicate citizen prevention.

### 👤 Citizen Profiles & Cohort Engine Developer
*   **Focus**: Phase 2 implementation.
*   **Modules**: `citizen_profile`, `cohort_placement`.
*   **Key Responsibilities**: Demographic profiles, DOB-derived age calculation, automated age-based and demographic-based cohort assignment, and the age transition scheduler.

### 👤 Personalized Content & Dashboard Developer
*   **Focus**: Phase 3 implementation.
*   **Modules**: `admin_content_cms`, `notification`.
*   **Key Responsibilities**: Targeted communication publishing, personalized citizen dashboard endpoints, content interaction tracking, and notification delivery.

### 👤 Field Operations & Offline Sync Developer
*   **Focus**: Phase 4 implementation.
*   **Modules**: `fieldofficer`, `offline_sync_engine`.
*   **Key Responsibilities**: Field officer assignments, offline-capable sync endpoint for batch uploads, conflict resolution (where field officer updates take precedence).

### 👤 Assessments & Milestone Flags Developer
*   **Focus**: Phase 5 implementation.
*   **Modules**: `child_assessment`, `autoflag`.
*   **Key Responsibilities**: Dynamic developmental/eligibility assessment evaluations, scoring engines, and milestone overdue alerting scheduler.

### 👤 Governance, Analytics & Auditing Developer
*   **Focus**: Phase 6 implementation.
*   **Modules**: `analytics`, `audit`, `export`.
*   **Key Responsibilities**: Admin search/filtering, query optimization (materialized views), deactivation controls, and comprehensive administrative audit logging.
