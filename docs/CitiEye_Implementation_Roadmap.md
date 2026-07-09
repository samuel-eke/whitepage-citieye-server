# CitiEye Application Implementation Roadmap

This document serves as the comprehensive development and implementation roadmap for the CitiEye system. It organizes the 19 functional requirements (FRs) and 5 non-functional requirements (NFRs) specified in the SRS into a logical, phase-by-phase implementation plan.

## Spring Modulith & Modular Monolith Architecture
This application is designed as a **Modular Monolith** using the **Spring Modulith** framework.
- **Encapsulation**: Each package directly under the root `com.goldbyte.citieye_server` is defined as a standalone module (e.g. `userauth`, `citizen_profile`, `cohort_placement`). Access to `internal` packages of other modules is strictly forbidden by modulith boundary verification.
- **Decoupled Integration**: High-level cross-module dependencies should utilize **Spring Modulith Application Events** (event publishers and event listeners) rather than direct service injection. This ensures modules can be tested and run independently.
- **Verification**: Modulith verification will be enforced via unit test executions using `ApplicationModules.of(CitieyeServerApplication.class).verify();`.
- **Database Schema Encapsulation**: Each Modulith module must map to its own dedicated PostgreSQL schema:
  - `userauth` module -> `auth` schema.
  - `content_cms` module -> `content_cms` schema (unified Content CMS handled by admin, consumed by citizens).
  - `cohort_placement` and `fieldofficer` modules -> `officers` schema.
  - `citizen_profile` module -> `citizen` schema.
  - `child_assessment` module -> `assessments` schema.
  - `audit` module -> `audit` schema.
  - **Dynamic Clean Database Strategy**: The database is starting completely clean (wiped). Schemas and tables will be created dynamically at runtime/tests based on what is implemented in the active phase, so only what is needed is built.



---


## Roadmap Overview & Requirements Mapping

The development is divided into 6 distinct phases to prioritize foundational dependencies (Identity, Core Profiles) before building user-facing features (Dashboards, Field Apps, Assessments) and administrative layers (Analytics, Auditing).

| Phase | Title | Primary Focus | Addressed Requirements |
| :--- | :--- | :--- | :--- |
| **Phase 1** | **Identity, Access & RBAC (IAM)** | Secure registration, login, role definitions, and duplicate entry blocking. | FR 1, FR 2, FR 9, FR 12, FR 18, NFR: Data Integrity (duplicates) |
| **Phase 2** | **Citizen Profiles & Cohort Engine** | Demographic profiles, DOB-derived age, automated cohort assignment. | FR 3, FR 4, FR 8, NFR: Data Integrity (cohort assignment) |
| **Phase 3** | **Personalized Content & Dashboard** | Target criteria publishing, citizen dashboard, content interaction tracking. | FR 5, FR 6, FR 13 |
| **Phase 4** | **Field Operations & Offline Sync** | Field officer demographics viewing, status reporting, offline capability. | FR 10, FR 11, NFR: Usability (connectivity) |
| **Phase 5** | **Assessments & Milestone Flags** | Dynamic assessments, score evaluations, automated milestone flagging. | FR 7, FR 19 |
| **Phase 6** | **Governance, Analytics & Auditing** | Admin controls, predictive modeling, exporting, performance optimization. | FR 14, FR 15, FR 16, FR 17, NFR: Performance, Availability, Scalability |

---

## Detailed Phase Breakdown

### Phase 1: Identity, Access & Role-Based Access Control (IAM)
#### Objective
Establish a secure, stateless, and role-based authentication system. Provide registration APIs for citizens, field officers, and administrative users while enforcing entry-point validation to prevent duplicates.

#### Architectural Components
- **User Roles**: `super_admin`, `agency_admin`, `field_officer`, `citizen`.
- **JWT stateless auth**: Replace HTTP Basic with Bearer tokens containing user roles and ID.
- **Unique Citizen Code Generation**: Automatically generated from the database side (e.g., via sequence, trigger, or DB function) on record insertion. The registration service queries the database to receive the generated code post-insertion.


#### Database Changes
We will update `auth.users` to support:
- `user_code`: `VARCHAR(50) UNIQUE`
- Unique constraint on `nin`, `email`, and `phone_number` to prevent duplicate registrations.

#### Core API Contracts
- `POST /api/auth/register/citizen`
  - *Request*: `CitizenRegDTO` (first/middle/last name, phone, email, password, nin).
  - *Response*: Returns user details including `user_code`.
- `POST /api/auth/register/field-officer` (restricted to Super Admin)
- `POST /api/auth/login`
  - *Request*: `LoginRequest` (username/email/phone, password).
  - *Response*: `{ token: "jwt-string", role: "citizen", code: "..." }`

#### Build Configuration Changes
- Modify `pom.xml` to set the `lombok` dependency scope from `annotationProcessor` to `provided` so that IDE and compiler Lombok processing works properly.

---


### Phase 2: Citizen Profiles & Cohort Engine
#### Objective
Introduce structured citizen profile data (demographics, age, state parameters) and a rules engine to automatically categorize them into cohorts. Manage cohort progression as citizens age.

#### Architectural Components
- **Citizen Profile**: Stored separately from credential tables, securing PII.
- **Placement Service**: Processes age and state parameter rules to assign `cohort_id`s.
- **Age Transition Scheduler**: Periodic task (Spring Scheduler) checking for citizens whose age group has transitioned.

#### Database Changes
- Create `citizen.profiles`:
  - `id`: `UUID PRIMARY KEY`
  - `user_id`: `UUID FOREIGN KEY -> auth.users(id)`
  - `dob`: `DATE NOT NULL`
  - `gender`: `VARCHAR(10)`
  - `economic_status`: `VARCHAR(50)` (e.g., unemployed, student, low-income)
  - `state_id`, `lga_id`: Location tracking
- Create `officers.cohorts`:
  - `id`: `UUID PRIMARY KEY`
  - `name`: `VARCHAR(100)`
  - `min_age_years`, `max_age_years`: `INT`
  - `demographic_rules`: `JSONB`

#### Core API Contracts
- `POST /api/citizen/profile`
  - *Request*: Profile data (DOB, gender, location, indicators).
  - *Response*: Profile status + assigned Cohort.

---

### Phase 3: Personalized Content & Dashboard
#### Objective
Enable Agency Admins to publish targeted communications and programmes. Give citizens a personalized dashboard to interact with relevant content based on their cohort.

#### Architectural Components
- **Targeting Engine**: Resolves published content against the citizen's active cohort, gender, and location.
- **Interaction Tracker**: Records user interactions (views, applications, status).

#### Database Changes
- Create `content_cms.interactions`:
  - `id`: `UUID PRIMARY KEY`
  - `user_id`: `UUID FOREIGN KEY`
  - `item_id`: `UUID FOREIGN KEY -> content_cms.items(id)`
  - `interaction_type`: `VARCHAR(50)` (view, apply, dismissed)
  - `interacted_at`: `TIMESTAMP`


#### Core API Contracts
- `GET /api/citizen/dashboard`
  - *Response*: List of `ContentItemDTO` targeted at the current citizen's age group and demographic parameters.
- `POST /api/citizen/content/{id}/interact`
  - *Request*: `{ type: "apply" }`
  - *Response*: `{ status: "success" }`

---

### Phase 4: Field Operations & Offline Sync Engine
#### Objective
Provide Field Officers with the tools to view their assigned demographics and report on citizen socio-economic status. Design a sync API that allows the field app to run offline and upload batched reports once internet connection is restored.

#### Architectural Components
- **Officer Cohort Assignments**: Links field officers to specific cohorts or local government areas (LGAs).
- **Offline Sync Controller**: Receives batched, timestamped logs, handles conflicts, and records updates idempotently.
- **Conflict Precedence**: The updates from the Field Officer take complete precedence. Local storage on the Field Officer device acts as the primary source of truth in conflict resolution.


#### Database Changes
- Create `officers.assignments`:
  - `id`: `UUID PRIMARY KEY`
  - `officer_id`: `UUID FOREIGN KEY`
  - `cohort_id`: `UUID FOREIGN KEY`
  - `lga_id`: `UUID`
- Create `officers.reports`:
  - `id`: `UUID PRIMARY KEY`
  - `citizen_id`: `UUID FOREIGN KEY`
  - `officer_id`: `UUID FOREIGN KEY`
  - `status_data`: `JSONB` (socio-economic dimensions)
  - `submitted_at`: `TIMESTAMP`

#### Core API Contracts
- `GET /api/officer/cohorts`
  - *Response*: Returns cohorts and citizens assigned to the authenticated field officer.
- `POST /api/officer/sync`
  - *Request*: Batch of status reports collected offline with local timestamps.
  - *Response*: Status of synced items and list of conflict items.

---

### Phase 5: Assessments & Milestone Tracking
#### Objective
Provide citizens with easy-to-take eligibility assessments. Configure background audits that flag citizens who are overdue for critical developmental or program milestones.

#### Architectural Components
- **Assessment Engine**: Computes scores and profiles citizens for program eligibility based on answers.
- **Milestone Engine**: Monitors profile age progression and triggers alerts when milestones are overdue.

#### Database Changes
- Create `assessments.templates` & `assessments.answers`.
- Create `citizen.milestones`:
  - `id`: `UUID PRIMARY KEY`
  - `citizen_id`: `UUID FOREIGN KEY`
  - `milestone_type`: `VARCHAR(100)`
  - `due_date`: `DATE`
  - `completed_at`: `TIMESTAMP`
  - `status`: `VARCHAR(20)` (pending, completed, flagged)

#### Core API Contracts
- `GET /api/citizen/assessments/eligible`
  - *Response*: List of assessments the citizen can take.
- `POST /api/citizen/assessments/{id}/submit`
  - *Request*: Question-answer map.
  - *Response*: Assessment results and eligibility recommendation.

---

### Phase 6: Governance, Analytics & Auditing
#### Objective
Provide Super Admins and Agency Admins with tools to view data, manage security permissions, configure policies, and get predictive model outputs while maintaining strict audit trails.

#### Architectural Components
- **Analytics View Optimizer**: Database indexing and query optimization (Materialized Views or read-replicas) to keep response times under 3s.
- **Predictive Analytics Model Integration**: (Kept in view/deferred) To be addressed in later stages. Focus will initially remain on raw tracking metrics and dashboard views.
- **Audit Logger**: Captures every administrative action (deactivation, assignment adjustments, data export).


#### Database Changes
- Create `admins.audit_logs`:
  - `id`: `UUID PRIMARY KEY`
  - `admin_id`: `UUID FOREIGN KEY`
  - `action`: `VARCHAR(255)`
  - `target_entity`: `VARCHAR(100)`
  - `target_id`: `UUID`
  - `timestamp`: `TIMESTAMP`

#### Core API Contracts
- `GET /api/admin/users`
  - *Query parameters*: name, role, cohort, location (paginated search).
- `POST /api/admin/users/{id}/deactivate` (Super Admin action)
- `GET /api/admin/analytics/prediction`
  - *Response*: Cohort projection trends and officer workload analytics.

---

## Verification & Boundary Enforcement

To ensure modular integrity is maintained, the following testing rules apply:
1. **Automated Verification**: Run `./mvnw.cmd clean test` after implementing each phase.
2. **Spring Modulith Verification**: A dedicated test checking boundaries must be run and pass at the completion of each phase:
   ```java
   ApplicationModules.of(CitieyeServerApplication.class).verify();
   ```
3. **Domain Test Isolation**: Ensure test classes for one module do not directly instantiate internal services or entities from another module, relying instead on API abstractions or published Spring application events.

