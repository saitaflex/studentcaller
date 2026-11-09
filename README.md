# StudentCaller – School Room Reservation REST API

DS1 – Web Development | Spring Boot Mini-Project

## Group members
- Mohamed Aziz Selmi
- Souliman Cheeri
- Oussema Laabidi
- Ahmed Hammami

---

## Table of contents
1. [Context](#1-context)
2. [Technologies](#2-technologies)
3. [Project structure](#3-project-structure)
4. [Entities and relationships](#4-entities-and-relationships)
5. [Business rules](#5-business-rules)
6. [Validation and error handling](#6-validation-and-error-handling)
7. [Custom queries](#7-custom-queries)
8. [REST endpoints](#8-rest-endpoints)
9. [Execution instructions](#9-execution-instructions)
10. [Postman tests](#10-postman-tests)
11. [Unit tests](#11-unit-tests)
12. [Troubleshooting](#12-troubleshooting)

---

## 1. Context
This project manages the **reservation of school rooms by teachers**.

A school has several **buildings**, and each building contains **rooms** (classrooms, labs, reading halls) equipped with **equipment** (projector, computers, whiteboard…). **Teachers**, who belong to a **department**, reserve a room for a number of hours. The total price is computed automatically from the room's hourly price. Once a room is reserved it is no longer available until the reservation is completed or cancelled. Each reservation is then settled with a **payment**.

The application is a **REST API** built with Spring Boot, tested with Postman and documented with Swagger UI. It follows the layered architecture `model → repository → service → controller`.

## 2. Technologies
| Technology | Usage |
|---|---|
| Java 17+ | Language |
| Spring Boot 4 | Application framework |
| Spring Web MVC | REST controllers |
| Spring Data JPA (Hibernate) | Persistence layer |
| Jakarta Validation | Data validation |
| MySQL (XAMPP) | Database |
| springdoc-openapi (Swagger UI) | Interactive API documentation |
| JUnit 5 + Mockito | Unit tests of the business logic |
| Postman | API testing |
| Maven | Build tool |

## 3. Project structure
```
studentcaller/
├─ src/
│  ├─ main/
│  │  ├─ java/com/esb/
│  │  │  ├─ model/          Department, Teacher, Building, Room, Equipment, Reservation, Payment
│  │  │  │                  + enums RoomStatus, ReservationStatus, PaymentStatus
│  │  │  ├─ repository/     one JpaRepository per entity (+ custom queries)
│  │  │  ├─ service/        business logic and rules
│  │  │  ├─ controller/     REST endpoints + GlobalExceptionHandler + HomeController
│  │  │  ├─ config/         Swagger / OpenAPI documentation (OpenApiConfig, ApiDocs)
│  │  │  └─ ProjetApplication.java
│  │  └─ resources/
│  │     └─ application.properties
│  └─ test/java/com/esb/service/   ReservationServiceTest, PaymentServiceTest, RoomServiceTest
├─ Postman/
│  ├─ StudentCaller.postman_collection.json
│  └─ Screenshots/
├─ CHANGES.md
├─ pom.xml
└─ README.md
```

## 4. Entities and relationships

The domain contains **7 entities** linked together.

### 4.1 Department
| Field | Type | Constraints |
|---|---|---|
| id | Long | Primary key, auto-generated |
| name | String | `@NotBlank` |

### 4.2 Teacher
| Field | Type | Constraints |
|---|---|---|
| id | Long | Primary key, auto-generated |
| name | String | `@NotBlank` |
| email | String | `@NotBlank`, `@Email`, unique |
| department | Department | `@NotNull`, `@ManyToOne` |

### 4.3 Building
| Field | Type | Constraints |
|---|---|---|
| id | Long | Primary key, auto-generated |
| name | String | `@NotBlank` |
| address | String | – |

### 4.4 Room
| Field | Type | Constraints |
|---|---|---|
| id | Long | Primary key, auto-generated |
| name | String | `@NotBlank` |
| capacity | int | `@Min(1)` |
| status | RoomStatus | `AVAILABLE` or `RESERVED` (set automatically, read-only in JSON) |
| pricePerHour | BigDecimal | `@NotNull`, `@PositiveOrZero` |
| building | Building | `@NotNull`, `@ManyToOne` |
| equipments | Set&lt;Equipment&gt; | `@ManyToMany` + `@JoinTable(room_equipment)` |

### 4.5 Equipment
| Field | Type | Constraints |
|---|---|---|
| id | Long | Primary key, auto-generated |
| name | String | `@NotBlank`, unique |
| description | String | – |
| rooms | Set&lt;Room&gt; | `@ManyToMany(mappedBy = "equipments")`, `@JsonIgnore` |

### 4.6 Reservation
| Field | Type | Description |
|---|---|---|
| id | Long | Primary key, auto-generated |
| room | Room | `@ManyToOne` |
| teacher | Teacher | `@ManyToOne` |
| startTime | LocalDateTime | Set to the moment of creation |
| endTime | LocalDateTime | startTime + number of hours |
| reservationStatus | ReservationStatus | `PENDING`, `COMPLETED` or `CANCELLED` |
| totalAmount | BigDecimal | pricePerHour × hours |

### 4.7 Payment
| Field | Type | Description |
|---|---|---|
| id | Long | Primary key, auto-generated |
| reservation | Reservation | `@OneToOne`, unique (one payment per reservation) |
| paymentDate | LocalDate | Date of the payment |
| amount | BigDecimal | Amount paid |
| paymentStatus | PaymentStatus | `PENDING` or `PAID` |

### 4.8 Relationships
| Relationship | Type | Mapping |
|---|---|---|
| Department → Teacher | One-to-Many | `Teacher.department` `@ManyToOne` / `Department.teachers` `@OneToMany(mappedBy)` |
| Building → Room | One-to-Many | `Room.building` / `Building.rooms` |
| Teacher → Reservation | One-to-Many | `Reservation.teacher` / `Teacher.reservations` |
| Room → Reservation | One-to-Many | `Reservation.room` |
| Room ↔ Equipment | **Many-to-Many** | `Room.equipments` (owning side, join table `room_equipment`) / `Equipment.rooms` (`mappedBy`) |
| Reservation → Payment | **One-to-One** | `Payment.reservation` with a unique foreign key |

The inverse sides (`mappedBy`) are annotated with `@JsonIgnore` to avoid infinite loops when entities are serialized to JSON.

```
Department 1 ── N Teacher 1 ── N Reservation N ── 1 Room N ── 1 Building
                                    1                N
                                    │                │
                                    1                M
                                 Payment         Equipment
```

## 5. Business rules
- A reservation can only be created if the room status is `AVAILABLE`.
- The number of hours must be greater than 0.
- `totalAmount = pricePerHour × hours`.
- Creating a reservation sets the room to `RESERVED`.
- Completing, cancelling or deleting a pending reservation sets the room back to `AVAILABLE`.
- Only a `PENDING` reservation can be extended, completed or cancelled.
- Extending a reservation adds hours to `endTime` and increases `totalAmount`.
- A payment amount must match the reservation total exactly (compared with `compareTo`, so `50.0` equals `50.00`).
- A reservation can be paid **only once**; a cancelled reservation cannot be paid.
- A paid reservation cannot be extended, and a `PAID` payment cannot be set back to `PENDING`.
- A new room always starts as `AVAILABLE`; its status cannot be sent by the client.
- A teacher email and an equipment name are unique.
- A room cannot receive the same equipment twice.
- Operations that modify a room and a reservation together are `@Transactional` (all or nothing).

## 6. Validation and error handling

### Validation
Bean Validation annotations (`@NotBlank`, `@Email`, `@Min`, `@NotNull`, `@PositiveOrZero`) are applied on the entities and triggered with `@Valid` in the controllers. The services add business checks (unique email, missing building/department id, …).

### Global exception handler
`GlobalExceptionHandler` (`@RestControllerAdvice`) converts exceptions to JSON responses:

| Situation | Exception | HTTP status |
|---|---|---|
| Resource created | – | **201 Created** |
| Resource deleted | – | **204 No Content** |
| Successful read / update | – | **200 OK** |
| Validation failed | `MethodArgumentNotValidException` | **400 Bad Request** |
| Business rule broken (room not available, wrong amount, already paid…) | `IllegalStateException` | **400 Bad Request** |
| Wrong parameter type (`?reservationStatus=FOO`) | `MethodArgumentTypeMismatchException` | **400 Bad Request** |
| Missing request parameter | `MissingServletRequestParameterException` | **400 Bad Request** |
| Malformed JSON body | `HttpMessageNotReadableException` | **400 Bad Request** |
| Resource not found | `NoSuchElementException` | **404 Not Found** |
| Record linked to other data (cannot delete) | `DataIntegrityViolationException` | **409 Conflict** |
| Anything else | `Exception` | **500** with a clean message |

Error response examples:
```json
{ "error": "Room not found" }
```
```json
{ "name": "Department name is required" }
```

## 7. Custom queries

| # | Repository method | Type | Business need |
|---|---|---|---|
| 1 | `RoomRepository.findByStatus(RoomStatus)` | Derived query | List the available rooms |
| 2 | `RoomRepository.findByStatusAndCapacityGreaterThanEqual(RoomStatus, int)` | Derived query | Find available rooms for a minimum number of people |
| 3 | `ReservationRepository.findByReservationStatus(ReservationStatus)` | Derived query | List reservations by status (pending, completed, cancelled) |
| 4 | `ReservationRepository.findByTeacherId(Long)` | Derived query | Show all reservations of one teacher |
| 5 | `TeacherRepository.findByDepartmentName(String)` | **JPQL `@Query`** | List the teachers of a department by its name (case-insensitive) |
| 6 | `RoomRepository.findByStatusAndEquipmentsNameIgnoreCase(RoomStatus, String)` | Derived query through the Many-to-Many | Find available rooms that have a given equipment (e.g. a projector) |

```java
@Query("SELECT t FROM Teacher t WHERE LOWER(t.department.name) = LOWER(:departmentName)")
List<Teacher> findByDepartmentName(@Param("departmentName") String departmentName);
```

Helper queries used by the business rules: `PaymentRepository.existsByReservationId`, `TeacherRepository.existsByEmailIgnoreCase`, `EquipmentRepository.existsByNameIgnoreCase`.
Every repository also provides the standard CRUD operations through `JpaRepository`.

## 8. REST endpoints

Base URL: `http://localhost:8080` — interactive documentation: **`http://localhost:8080/swagger-ui.html`** (opening `/` redirects there).

### 8.1 Departments – `/api/departments`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/departments` | Create a department | 201 |
| GET | `/api/departments` | List all departments | 200 |
| GET | `/api/departments/{id}` | Get one department | 200 |
| PUT | `/api/departments/{id}` | Update a department | 200 |
| DELETE | `/api/departments/{id}` | Delete a department | 204 |

### 8.2 Teachers – `/api/teachers`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/teachers` | Create a teacher (assigned to a department) | 201 |
| GET | `/api/teachers` | List all teachers | 200 |
| GET | `/api/teachers/{id}` | Get one teacher | 200 |
| GET | `/api/teachers/department/{departmentName}` | **Custom query (JPQL):** teachers of a department | 200 |
| PUT | `/api/teachers/{id}` | Update a teacher (including its department) | 200 |
| DELETE | `/api/teachers/{id}` | Delete a teacher | 204 |

### 8.3 Buildings – `/api/buildings`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/buildings` | Create a building | 201 |
| GET | `/api/buildings` | List all buildings | 200 |
| GET | `/api/buildings/{id}` | Get one building | 200 |
| PUT | `/api/buildings/{id}` | Update a building | 200 |
| DELETE | `/api/buildings/{id}` | Delete a building | 204 |

### 8.4 Rooms – `/api/rooms`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/rooms` | Create a room | 201 |
| GET | `/api/rooms` | List all rooms | 200 |
| GET | `/api/rooms/{id}` | Get one room | 200 |
| PUT | `/api/rooms/{id}` | Update a room | 200 |
| DELETE | `/api/rooms/{id}` | Delete a room | 204 |
| GET | `/api/rooms/available` | **Custom query:** available rooms | 200 |
| GET | `/api/rooms/available/capacity?minCapacity=40` | **Custom query:** available rooms with at least 40 seats | 200 |
| GET | `/api/rooms/available/equipment?name=Projector` | **Custom query:** available rooms with a projector | 200 |
| PUT | `/api/rooms/{roomId}/equipments/{equipmentId}` | Install an equipment in a room (Many-to-Many) | 200 |
| DELETE | `/api/rooms/{roomId}/equipments/{equipmentId}` | Remove an equipment from a room | 200 |

### 8.5 Equipment – `/api/equipments`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/equipments` | Create an equipment | 201 |
| GET | `/api/equipments` | List all equipment | 200 |
| GET | `/api/equipments/{id}` | Get one equipment | 200 |
| PUT | `/api/equipments/{id}` | Update an equipment | 200 |
| DELETE | `/api/equipments/{id}` | Delete an equipment | 204 |

### 8.6 Reservations – `/api/reservations`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/reservations/{teacherId}/{roomId}/{hours}` | Create a reservation | 201 |
| GET | `/api/reservations` | List all reservations | 200 |
| GET | `/api/reservations/{id}` | Get one reservation | 200 |
| PUT | `/api/reservations/{id}/extend?additionalHours=2` | Extend a reservation | 200 |
| PUT | `/api/reservations/{id}/complete` | Complete a reservation | 200 |
| PUT | `/api/reservations/{id}/cancel` | Cancel a reservation | 200 |
| GET | `/api/reservations/status?reservationStatus=PENDING` | **Custom query:** reservations by status | 200 |
| GET | `/api/reservations/teacher/{teacherId}` | **Custom query:** reservations of a teacher | 200 |
| DELETE | `/api/reservations/{id}` | Delete a reservation | 204 |

### 8.7 Payments – `/api/payments`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/payments/{reservationId}?amount=50` | Pay a reservation | 201 |
| GET | `/api/payments` | List all payments | 200 |
| GET | `/api/payments/{id}` | Get one payment | 200 |
| PUT | `/api/payments/{id}/status?status=PAID` | Change a payment status | 200 |
| DELETE | `/api/payments/{id}` | Delete a payment | 204 |

### 8.8 Request and response examples

**Create a department** – `POST /api/departments`
```json
{ "name": "Computer Science" }
```
Response `201 Created`:
```json
{ "id": 1, "name": "Computer Science" }
```

**Create a teacher** – `POST /api/teachers`
```json
{ "name": "Ahmed Ben Ali", "email": "ahmed@school.com", "department": { "id": 1 } }
```

**Create a room** – `POST /api/rooms`
```json
{ "name": "A101", "capacity": 30, "pricePerHour": 10, "building": { "id": 1 } }
```

**Create an equipment** – `POST /api/equipments`
```json
{ "name": "Projector", "description": "HD video projector" }
```

**Install it in the room** – `PUT /api/rooms/1/equipments/1` (no body). Response `200 OK`:
```json
{
  "id": 1,
  "name": "A101",
  "capacity": 30,
  "status": "AVAILABLE",
  "pricePerHour": 10.00,
  "building": { "id": 1, "name": "Block A", "address": "Main Campus" },
  "equipments": [ { "id": 1, "name": "Projector", "description": "HD video projector" } ]
}
```

**Create a reservation** – `POST /api/reservations/1/1/3` (teacher 1, room 1, 3 hours, no body). Response `201 Created`:
```json
{
  "id": 1,
  "room": { "id": 1, "name": "A101", "status": "RESERVED" },
  "teacher": { "id": 1, "name": "Ahmed Ben Ali" },
  "startTime": "2026-11-08T14:00:00",
  "endTime": "2026-11-08T17:00:00",
  "reservationStatus": "PENDING",
  "totalAmount": 30.00
}
```

**Pay a reservation** – `POST /api/payments/1?amount=30` (no body). Response `201 Created`:
```json
{ "id": 1, "reservation": { "id": 1 }, "paymentDate": "2026-11-08", "amount": 30, "paymentStatus": "PAID" }
```

## 9. Execution instructions

### 9.1 Prerequisites
- Java 17 or newer (JDK)
- Maven (or the included Maven wrapper)
- XAMPP (for MySQL)
- An IDE (Spring Tools / Eclipse, IntelliJ or VS Code)
- Postman

### 9.2 Clone the project
```
git clone https://github.com/saitaflex/studentcaller.git
cd studentcaller
```

### 9.3 Start the database
1. Open the **XAMPP Control Panel**.
2. Click **Start** next to **MySQL** (it turns green).

The database `school_db` is created automatically at the first start.

### 9.4 Configure `application.properties`
File: `src/main/resources/application.properties`
```properties
spring.application.name=studentcaller
spring.datasource.url=jdbc:mysql://localhost:3306/school_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
- With XAMPP the root password is empty by default. Change it if yours is different.
- If XAMPP's MySQL runs on another port (e.g. `3307` because another MySQL already uses `3306`), change the port in the URL.

### 9.5 Maven dependencies
`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `mysql-connector-j`, `springdoc-openapi-starter-webmvc-ui` and `spring-boot-starter-test` (see `pom.xml`).

### 9.6 Run the application
**From the IDE:** right-click `ProjetApplication.java` → *Run As* → *Spring Boot App*.

**From a terminal:**
```
./mvnw spring-boot:run
```
(on Windows: `mvnw spring-boot:run`)

Wait for the console line `Started ProjetApplication in X seconds`. Then:
- API: `http://localhost:8080/api/...`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (or simply `http://localhost:8080/`)

### 9.7 Stop the application
Click the red square (Terminate) in the Console tab of the IDE, or press `Ctrl + C` in the terminal.

### 9.8 Reset the database
The Postman collection expects an empty database (ids start at 1). Drop `school_db` in phpMyAdmin (`http://localhost/phpmyadmin`) and restart the application, or set `spring.jpa.hibernate.ddl-auto=create` for one run, then back to `update`.

## 10. Postman tests

### 10.1 Files
- Collection: `Postman/StudentCaller.postman_collection.json` (import it in Postman with *Import*)
- Screenshots: `Postman/Screenshots/`

Every request contains a **test script** that checks the HTTP status (and, for key requests, the computed values such as `totalAmount`). Run the whole collection with **Run collection** on an empty database: all tests should be green.

### 10.2 Functional tests
All POST/PUT requests with a body use *Body → raw → JSON*.

| # | Request | Body | Expected |
|---|---|---|---|
| 01 | `POST /api/departments` | `{"name":"Computer Science"}` | 201 |
| 02 | `POST /api/buildings` | `{"name":"Block A","address":"Main Campus"}` | 201 |
| 03 | `POST /api/teachers` | `{"name":"Ahmed Ben Ali","email":"ahmed@school.com","department":{"id":1}}` | 201 |
| 04 | `POST /api/rooms` | `{"name":"A101","capacity":30,"pricePerHour":10,"building":{"id":1}}` | 201 |
| 05 | `POST /api/equipments` | `{"name":"Projector","description":"HD video projector"}` | 201 |
| 06 | `PUT /api/rooms/1/equipments/1` | – | 200 |
| 07 | `GET /api/rooms` | – | 200 |
| 08 | `POST /api/reservations/1/1/3` | – | 201 (total 30) |
| 09 | `PUT /api/reservations/1/extend?additionalHours=2` | – | 200 (total 50) |
| 10 | `POST /api/payments/1?amount=50` | – | 201 |
| 11 | `GET /api/rooms/available/capacity?minCapacity=20` | – | 200 |
| 12 | `GET /api/reservations/status?reservationStatus=PENDING` | – | 200 |
| 13 | `GET /api/reservations/teacher/1` | – | 200 |
| 14 | `GET /api/teachers/department/Computer Science` | – | 200 |
| 15 | `PUT /api/departments/1` | `{"name":"Mathematics"}` | 200 |
| 16 | `PUT /api/reservations/1/complete` | – | 200 |
| 17 | `GET /api/rooms/available/equipment?name=Projector` | – | 200 |
| 18 | `POST /api/equipments` | `{"name":"Whiteboard","description":"Magnetic whiteboard"}` | 201 |
| 19 | `DELETE /api/equipments/2` | – | 204 |

### 10.3 Error tests
| # | Request | Expected |
|---|---|---|
| 20 | `POST /api/departments` with `{"name":""}` | 400 (validation) |
| 21 | `GET /api/rooms/999` | 404 |
| 22 | `POST /api/reservations/1/1/2` twice | 201 then 400 (room already reserved) |
| 23 | `POST /api/payments/2?amount=5` | 400 (incorrect amount) |
| 24 | `POST /api/payments/2?amount=20` twice | 201 then 400 (already paid) |
| 25 | `PUT /api/reservations/2/extend?additionalHours=1` | 400 (paid reservation cannot be extended) |
| 26 | `POST /api/teachers` with an existing email | 400 |
| 27 | `PUT /api/rooms/1/equipments/1` again | 400 (equipment already in the room) |
| 28 | `GET /api/reservations/status?reservationStatus=FOO` | 400 (invalid value) |
| 29 | `DELETE /api/departments/1` while a teacher uses it | 409 |
| 30 | `DELETE /api/equipments/1` while installed in a room | 409 |

### 10.4 Screenshots of successful tests

**Create a department (201)**

![Create department](Postman/Screenshots/Post%20department.png)

**Create a building (201)**

![Create building](Postman/Screenshots/Post%20create%20buildings.png)

**Create a teacher (201)**

![Create teacher](Postman/Screenshots/Post%20teachers.png)

**Create a room (201)**

![Create room](Postman/Screenshots/Post%20create%20room.png)

**Create a reservation (201)**

![Create reservation](Postman/Screenshots/Post%20create%20reservation.png)

**Extend a reservation (200)**

![Extend reservation](Postman/Screenshots/Put%20extend%20reservation.png)

**Pay a reservation (201)**

![Payment](Postman/Screenshots/Post%20pay%20reservation.png)

**Custom query – reservations by status (200)**

![Reservations by status](Postman/Screenshots/Reservation%20by%20status.png)

**Custom query – reservations of a teacher (200)**

![Reservations by teacher](Postman/Screenshots/Get%20reservation%20of%20a%20teacher.png)

**Update a department (200)**

![Update department](Postman/Screenshots/Put%20update%20epartment.png)

**Complete a reservation (200)**

![Complete reservation](Postman/Screenshots/Put%20complete%20reservation.png)

**Create a reservation again (201)**

![Create reservation again](Postman/Screenshots/Post%20create%20reservation%20again.png)

### 10.5 Screenshots of error tests

**Validation error (400)**

![Validation error](Postman/Screenshots/Post%20error-%20validation.png)

**Not found (404)**

![Not found](Postman/Screenshots/Get%20eroor%20not%20found.png)

**Room already reserved (400)**

![Room not available](Postman/Screenshots/post%20error%20room%20alreay%20reserved.png)

**Incorrect payment amount (400)**

![Incorrect amount](Postman/Screenshots/Post%20error%20wrong%20payment%20amount.png)

**Delete a linked record (409)**

![Conflict](Postman/Screenshots/Del%20error%20elete%20linked%20record.png)

## 11. Unit tests
The business rules are covered by JUnit 5 + Mockito tests (no database needed):

- `ReservationServiceTest` – price computation, room locked/freed, reserved room refused, 0 hours refused, extension refused once paid
- `PaymentServiceTest` – exact amount, double payment refused, wrong amount refused, cancelled reservation refused, PAID → PENDING refused
- `RoomServiceTest` – new room always available, missing building refused, equipment added once, removing a missing equipment refused

```
./mvnw test
```

## 12. Troubleshooting

| Problem | Cause and fix |
|---|---|
| `Could not send request` in Postman | The application is not running. Start it and wait for `Started ProjetApplication`. |
| `Unable to determine Dialect without JDBC metadata` / `Access denied for user 'root'` | MySQL is not running, the password is wrong or the port is different. Start MySQL in XAMPP and check `application.properties`. |
| `400 Bad Request` on POST | Invalid JSON. Send one JSON object per request and select *raw → JSON*. |
| Postman tests fail on ids | The database was not empty. Reset it (section 9.8). |
| Red X marks on all files in the IDE | Maven dependencies are missing. Check `pom.xml`, then *Maven → Update Project*. |
| `409 Conflict` on DELETE | Another record uses it. Delete in this order: payments, reservations, room equipment, rooms, equipment, teachers, buildings, departments. |
| Port 8080 already in use | Stop the previous run of the application. |

---

*Project realized by: Mohamed Aziz Selmi, Souliman Cheeri, Oussema Laabidi, Ahmed Hammami.*
