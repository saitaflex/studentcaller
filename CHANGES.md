# Changes & Fixes – School Room Reservation API

Date: 2026-11-09

This document lists the gaps found during a review of the project and what was changed to fix them.
All REST endpoints keep the **same URLs and parameters**, so the Postman collection still works.

Verification: `./mvnw test` → **BUILD SUCCESS, 12 tests, 0 failures**.

---

## Summary

| # | Gap | Severity | Status |
|---|-----|----------|--------|
| 1 | Project did not compile (`java.version` 27) | Blocker | Fixed |
| 2 | Payment business-rule holes (double payment, extend after payment, PAID → PENDING) | High | Fixed |
| 3 | Room/reservation updates not transactional; deleting a reservation left the room locked | High | Fixed |
| 4 | Validation & error-handling gaps (duplicate email, NPE → 500, bad params) | Medium | Fixed |
| 5 | No tests at all | Medium | Fixed (12 unit tests) |
| 6 | Reservations ignore real time slots | Design | Not changed – recommendation |
| 7 | Entities exposed directly in the API (no DTOs) | Design | Not changed – recommendation |

---

## 1. Build fixed – Java version

**Problem:** `pom.xml` had `<java.version>27</java.version>`. No installed JDK supports release 27, so
`mvn compile` failed with `release version 27 not supported`.

**Fix:** `pom.xml` → `<java.version>17</java.version>` (matches the README "Java 17+", runs on any JDK ≥ 17).

---

## 2. Payment business rules

**Problems**
- The same reservation could be paid **several times**.
- `Payment → Reservation` was `@ManyToOne`, which contradicts "one payment per reservation".
- A reservation could be **extended after being paid**: the total increased but the extra amount was never paid.
- `PUT /api/payments/{id}/status?status=PENDING` could set a paid payment back to pending.

**Fixes**
- `PaymentRepository`: new query `existsByReservationId(Long reservationId)`.
- `PaymentService.doPayment`: rejects a second payment → `400 "Reservation is already paid"`.
- `Payment.reservation`: `@ManyToOne` → `@OneToOne` with `@JoinColumn(name = "reservation_id", unique = true)`.
- `ReservationService.extendReservation`: rejects extension of a paid reservation →
  `400 "Reservation is already paid and cannot be extended"`.
- `PaymentService.updatePaymentStatus`: PAID → PENDING is refused →
  `400 "A paid payment cannot be set back to pending"`.

---

## 3. Data consistency

**Problems**
- Creating / completing / cancelling a reservation saved the room and the reservation in two separate
  operations without a transaction. If the second save failed, the room stayed `RESERVED` forever.
- Deleting a `PENDING` reservation did not free its room.

**Fixes** (`ReservationService`)
- `@Transactional` on `createReservation`, `completeReservation`, `extendReservation`,
  `cancelReservation`, `deleteReservation` (and on `PaymentService.doPayment`).
- `deleteReservation` now sets the room back to `AVAILABLE` when the reservation was still `PENDING`.
- Duplicate "free the room" code extracted into a private `freeRoom(...)` helper.
- `createReservation` uses one `LocalDateTime.now()` so `endTime - startTime` is exactly `hours`.

---

## 4. Validation & error handling

**Problems**
- Two teachers could share the same email.
- Sending a Room without `building.id` or a Teacher without `department.id` threw a
  `NullPointerException` → HTTP 500.
- Wrong parameter types (`?reservationStatus=FOO`, `/reservations/abc`), missing parameters or
  malformed JSON returned Spring's default error format instead of the project's `{"error": ...}` format.
- Any unexpected exception returned a raw 500.
- A client could send `"status": "RESERVED"` in a Room body.

**Fixes**
- `Teacher.email`: `@Column(unique = true)`.
- `TeacherRepository`: `existsByEmailIgnoreCase`, `existsByEmailIgnoreCaseAndIdNot`.
- `TeacherService.addTeacher / updateTeacher`: duplicate email → `400 "Email is already used by another teacher"`;
  missing department id → `400 "Department id is required"`.
- `RoomService.addRoom / updateRoom`: missing building id → `400 "Building id is required"`.
- `Room.status`: `@JsonProperty(access = READ_ONLY)` – shown in responses, ignored in requests
  (status is managed by the reservation logic only).
- `GlobalExceptionHandler` – new handlers, all returning `{"error": "..."}`:
  | Exception | Status | Example |
  |---|---|---|
  | `MethodArgumentTypeMismatchException` | 400 | `?reservationStatus=FOO` |
  | `MissingServletRequestParameterException` | 400 | `/extend` without `additionalHours` |
  | `HttpMessageNotReadableException` | 400 | broken JSON body |
  | Any other `Exception` | keeps Spring's status (404 unknown URL, 405 wrong method) or 500 | – |

---

## 5. Tests added

`src/test` did not exist. Added JUnit 5 + Mockito unit tests (no database needed):

**`ReservationServiceTest`** (7 tests)
- price is computed and the room is locked on creation
- reserved room is rejected
- 0 hours is rejected
- extension is rejected when already paid
- extension adds the right price when unpaid
- deleting a pending reservation frees the room
- cancelling frees the room

**`PaymentServiceTest`** (5 tests)
- exact amount → PAID
- second payment rejected
- wrong amount rejected
- cancelled reservation rejected
- PAID → PENDING rejected

Run with:
```bash
./mvnw test
```

---

## Files changed

| File | Change |
|---|---|
| `pom.xml` | Java 27 → 17 |
| `model/Payment.java` | `@OneToOne`, unique `reservation_id` |
| `model/Teacher.java` | unique email |
| `model/Room.java` | `status` read-only in JSON |
| `repository/PaymentRepository.java` | `existsByReservationId` |
| `repository/TeacherRepository.java` | email-exists queries |
| `service/PaymentService.java` | double payment / status rules, `@Transactional` |
| `service/ReservationService.java` | `@Transactional`, paid-extension rule, delete frees room, `freeRoom` helper |
| `service/TeacherService.java` | duplicate email + null department checks |
| `service/RoomService.java` | null building check |
| `controller/GlobalExceptionHandler.java` | 4 new handlers |
| `src/test/.../ReservationServiceTest.java` | new |
| `src/test/.../PaymentServiceTest.java` | new |

---

## Notes for running

- **Existing database:** `ddl-auto=update` will try to add the new unique constraints
  (`payment.reservation_id`, `teacher.email`). If `school_db` already contains duplicate payments or
  emails, Hibernate logs a warning and skips the constraint. The simplest fix is to drop
  `school_db` and let the app recreate it.
- **MySQL password:** `application.properties` uses `root` with an empty password (XAMPP default). If
  your MySQL root has a password, set `spring.datasource.password`.
- **New Postman cases worth adding:** pay the same reservation twice (expect 400), extend a paid
  reservation (expect 400), create a teacher with an existing email (expect 400).

---

## Not changed (recommendations for a next version)

These would change the API contract and invalidate the Postman collection and screenshots, so they were
left out:

1. **Real time slots:** let the client choose `startTime` and check overlapping reservations
   (`startTime < newEnd AND endTime > newStart`) instead of locking the whole room with `RoomStatus`.
2. **Request DTOs:** e.g. `CreateReservationRequest { teacherId, roomId, startTime, hours }` as a JSON body
   instead of `POST /api/reservations/{teacherId}/{roomId}/{hours}`, and no entities as request bodies.
3. **Constructor injection** (`private final` fields) instead of `@Autowired` on fields.
4. **Custom exceptions** (`NotFoundException`, `BusinessException`) instead of reusing `IllegalStateException`.

---

## 6. Swagger UI added

- `pom.xml`: `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0` (Spring Boot 4 compatible).
- New `controller/HomeController.java`: `http://localhost:8080/` redirects to the Swagger UI.
- Pages:
  - `http://localhost:8080/swagger-ui.html` – interactive documentation of all endpoints ("Try it out")
  - `http://localhost:8080/v3/api-docs` – OpenAPI JSON

### Swagger documentation enriched
- `config/OpenApiConfig.java`: API title, a homepage guide (2-minute scenario, business rules, status codes) and the endpoint groups numbered in workflow order (Departments → Teachers → Buildings → Rooms → Reservations → Payments).
- `config/ApiDocs.java`: shared example error bodies.
- Controllers: `@Tag`, `@Operation` (summary + description), every response code documented with example bodies, parameters with descriptions and example values, ready-to-run request body examples.
- Models: `@Schema` descriptions and examples on every field; generated/managed fields (`id`, room `status`) marked read-only.
- `application.properties`: "Try it out" enabled by default, endpoint search filter, request duration, JSON as default media type.

---

## 7. Alignment with the DS1 brief (DS1_Spring_Boot_Project.docx)

| Brief requirement | What was done |
|---|---|
| Relationships "OneToMany, ManyToMany, etc." | New 7th entity **Equipment**, `@ManyToMany` with Room (join table `room_equipment`); install/remove endpoints `PUT/DELETE /api/rooms/{roomId}/equipments/{equipmentId}` |
| At least 3 relevant custom queries | Now 6, including a **JPQL `@Query`** (`TeacherRepository.findByDepartmentName`) and a query through the Many-to-Many (`RoomRepository.findByStatusAndEquipmentsNameIgnoreCase`) |
| Full CRUD | `EquipmentController` with full CRUD; new endpoint `GET /api/teachers/department/{departmentName}` |
| Postman: functional + error tests, exported collection | New `Postman/StudentCaller.postman_collection.json`: 32 requests (19 functional, 13 error), each with an automatic status test; verified end to end on an empty database (0 failures). Old request 16 (wrong amount on an already-paid reservation) fixed to target an unpaid one |
| README: context, entities, endpoints, execution | README rewritten: 7 entities, relationships table, 6 custom queries, all endpoints, Swagger, unit tests, port/password notes; screenshot links fixed (they pointed to files that did not exist) |
| `test/` folder | `RoomServiceTest` added (17 unit tests in total) |
