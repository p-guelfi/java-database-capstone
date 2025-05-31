# User Story Template

**Title:**  
_As a [user role], I want [feature/goal], so that [reason]._

**Acceptance Criteria:**  
1. [Criteria 1]  
2. [Criteria 2]  
3. [Criteria 3]

**Priority:** [High/Medium/Low]  
**Story Points:** [Estimated Effort in Points]  
**Notes:**  
- [Additional information or edge cases]

---

## Admin User Stories

### User Story 1

**Title:**  
_As an admin, I want to log into the portal with my username and password, so that I can manage the platform securely._

**Acceptance Criteria:**  
1. Admin can access the login page.  
2. Admin enters valid credentials and is authenticated.  
3. Successful login redirects to the admin dashboard.

**Priority:** High  
**Story Points:** 3  
**Notes:** Secure authentication with proper error messages.

---

### User Story 2

**Title:**  
_As an admin, I want to log out of the portal, so that I can protect system access._

**Acceptance Criteria:**  
1. Logout option is visible in the admin dashboard.  
2. Clicking logout ends the session and redirects to login page.  
3. Session is invalidated immediately on logout.

**Priority:** High  
**Story Points:** 2  
**Notes:** Prevent unauthorized access after logout.

---

### User Story 3

**Title:**  
_As an admin, I want to add doctors to the portal, so that new doctors can start managing appointments._

**Acceptance Criteria:**  
1. Admin can fill a form to add doctor details (name, specialization, contact info).  
2. New doctor profile is created and visible in the system.  
3. Validation errors shown for incomplete/incorrect data.

**Priority:** High  
**Story Points:** 5  
**Notes:** Notify doctors upon account creation.

---

### User Story 4

**Title:**  
_As an admin, I want to delete a doctor's profile from the portal, so that inactive doctors are removed._

**Acceptance Criteria:**  
1. Admin can select and delete a doctor profile.  
2. Confirmation prompt before deletion.  
3. Deleted doctors no longer appear in patient booking options.

**Priority:** Medium  
**Story Points:** 3  
**Notes:** Consider soft delete for recovery.

---

### User Story 5

**Title:**  
_As an admin, I want to run a stored procedure in MySQL CLI to get the number of appointments per month, so that I can track usage statistics._

**Acceptance Criteria:**  
1. Stored procedure returns correct appointment counts grouped by month.  
2. Admin can execute the procedure from CLI.  
3. Results are clear and formatted.

**Priority:** Medium  
**Story Points:** 3  
**Notes:** Include error handling if no data available.

---

## Patient User Stories

### User Story 1

**Title:**  
_As a patient, I want to view a list of doctors without logging in, so that I can explore options before registering._

**Acceptance Criteria:**  
1. Doctor list is visible on public pages.  
2. List shows doctor names and specializations.  
3. No personal patient info required to view.

**Priority:** High  
**Story Points:** 3  
**Notes:** Pagination or filtering optional.

---

### User Story 2

**Title:**  
_As a patient, I want to sign up using my email and password, so that I can book appointments._

**Acceptance Criteria:**  
1. Sign-up form accepts email and password.  
2. Email is validated and stored securely.  
3. Confirmation email sent after registration.

**Priority:** High  
**Story Points:** 5  
**Notes:** Password requirements enforced.

---

### User Story 3

**Title:**  
_As a patient, I want to log into the portal, so that I can manage my bookings._

**Acceptance Criteria:**  
1. Login page accepts email and password.  
2. Successful login redirects to patient dashboard.  
3. Patient sees their current and past appointments.

**Priority:** High  
**Story Points:** 3  
**Notes:** Implement session security.

---

### User Story 4

**Title:**  
_As a patient, I want to log out of the portal, so that I can secure my account._

**Acceptance Criteria:**  
1. Logout option available in dashboard.  
2. Logout terminates session and redirects to login.  
3. Session invalidation immediate.

**Priority:** High  
**Story Points:** 2  
**Notes:** Prevent unauthorized access after logout.

---

### User Story 5

**Title:**  
_As a patient, I want to log in and book an hour-long appointment to consult with a doctor, so that I can get medical advice._

**Acceptance Criteria:**  
1. Patient selects doctor and appointment slot of 1 hour.  
2. Booking confirmation is sent to patient and doctor.  
3. Appointment is saved in the system calendar.

**Priority:** High  
**Story Points:** 5  
**Notes:** Prevent double booking of same slot.

---

### User Story 6

**Title:**  
_As a patient, I want to view my upcoming appointments, so that I can prepare accordingly._

**Acceptance Criteria:**  
1. Upcoming appointments visible in patient dashboard.  
2. Details include doctor name, date, time, and location.  
3. Option to cancel or reschedule if allowed.

**Priority:** Medium  
**Story Points:** 3  
**Notes:** Notify patient of changes.

---

## Doctor User Stories

### User Story 1

**Title:**  
_As a doctor, I want to log into the portal, so that I can manage my appointments._

**Acceptance Criteria:**  
1. Doctor can access the login page.  
2. Doctor enters valid credentials (email and password).  
3. Successful login redirects to the doctor’s dashboard showing appointments.

**Priority:** High  
**Story Points:** 3  
**Notes:** Ensure secure authentication and session management.

---

### User Story 2

**Title:**  
_As a doctor, I want to log out of the portal, so that I can protect my data._

**Acceptance Criteria:**  
1. Logout button is visible and accessible in the dashboard.  
2. Clicking logout terminates the session and redirects to the login page.  
3. Session invalidation happens immediately on logout.

**Priority:** High  
**Story Points:** 2  
**Notes:** Prevent unauthorized access after logout.

---

### User Story 3

**Title:**  
_As a doctor, I want to view my appointment calendar, so that I can stay organized._

**Acceptance Criteria:**  
1. Doctor can view appointments in a calendar or list format.  
2. Appointments show date, time, and patient name.  
3. Doctor can filter appointments by day, week, or month.

**Priority:** High  
**Story Points:** 5  
**Notes:** Consider calendar integration for better UX.

---

### User Story 4

**Title:**  
_As a doctor, I want to mark my unavailability, so that patients only see available slots._

**Acceptance Criteria:**  
1. Doctor can select date/time ranges to mark as unavailable.  
2. Unavailable slots are not shown in patient booking options.  
3. Doctor can update or remove unavailability slots.

**Priority:** Medium  
**Story Points:** 5  
**Notes:** Handle recurring unavailability (e.g., weekly off days).

---

### User Story 5

**Title:**  
_As a doctor, I want to update my profile with specialization and contact information, so that patients have up-to-date information._

**Acceptance Criteria:**  
1. Doctor can edit profile fields: specialization, phone, email, and bio.  
2. Changes are saved and visible to patients when browsing doctors.  
3. System validates profile data before saving.

**Priority:** Medium  
**Story Points:** 3  
**Notes:** Allow adding multiple specializations.

---

### User Story 6

**Title:**  
_As a doctor, I want to view patient details for upcoming appointments, so that I can be prepared._

**Acceptance Criteria:**  
1. Doctor sees patient name, contact info, and brief medical notes (if available) for each appointment.  
2. Patient details are only accessible for confirmed appointments.  
3. Data is protected and only visible to the assigned doctor.

**Priority:** High  
**Story Points:** 5  
**Notes:** Ensure compliance with privacy and data protection standards.
