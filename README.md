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
11. [Troubleshooting](#11-troubleshooting)

---

## 1. Context
This project manages the **reservation of school rooms by teachers**.

A school has several **buildings**, and each building contains **rooms** (classrooms, labs, reading halls). **Teachers**, who belong to a **department**, reserve a room for a number of hours. The total price is computed automatically from the room's hourly price. Once a room is reserved it is no longer available until the reservation is completed or cancelled. Each reservation is then settled with a **payment**.

The application is a **web-oriented REST API** built with Spring Boot and tested with Postman. It follows the layered architecture `model → repository → service → controller`.

## 2. Technologies
| Technology | Usage |
|---|---|
| Java 17+ | Language |
| Spring Boot | Application framework |
| Spring Web | REST controllers |
| Spring Data JPA (Hibernate) | Persistence layer |
| Jakarta Validation | Data validation |
| MySQL (XAMPP) | Database |
| Postman | API testing |
| Maven | Build tool |

## 3. Project structure
```
studentcaller/
├─ src/
│  ├─ main/
│  │  ├─ java/com/esb/
│  │  │  ├─ model/
│  │  │  │  ├─ Department.java
│  │  │  │  ├─ Teacher.java
│  │  │  │  ├─ Building.java
│  │  │  │  ├─ Room.java
│  │  │  │  ├─ Reservation.java
│  │  │  │  ├─ Payment.java
│  │  │  │  ├─ RoomStatus.java
│  │  │  │  ├─ ReservationStatus.java
│  │  │  │  └─ PaymentStatus.java
│  │  │  ├─ repository/
│  │  │  │  ├─ DepartmentRepository.java
│  │  │  │  ├─ TeacherRepository.java
│  │  │  │  ├─ BuildingRepository.java
│  │  │  │  ├─ RoomRepository.java
│  │  │  │  ├─ ReservationRepository.java
│  │  │  │  └─ PaymentRepository.java
│  │  │  ├─ service/
│  │  │  │  ├─ DepartmentService.java
│  │  │  │  ├─ TeacherService.java
│  │  │  │  ├─ BuildingService.java
│  │  │  │  ├─ RoomService.java
│  │  │  │  ├─ ReservationService.java
│  │  │  │  └─ PaymentService.java
│  │  │  ├─ controller/
│  │  │  │  ├─ DepartmentController.java
│  │  │  │  ├─ TeacherController.java
│  │  │  │  ├─ BuildingController.java
│  │  │  │  ├─ RoomController.java
│  │  │  │  ├─ ReservationController.java
│  │  │  │  ├─ PaymentController.java
│  │  │  │  └─ GlobalExceptionHandler.java
│  │  │  └─ ProjetApplication.java
│  │  └─ resources/
│  │     └─ application.properties
│  └─ test/
├─ postman/
│  ├─ School_Reservation.postman_collection.json
│  └─ screenshots/
├─ pom.xml
└─ README.md
```

## 4. Entities and relationships

The domain contains **6 entities** linked together.

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
| email | String | `@NotBlank`, `@Email` |
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
| status | RoomStatus | `AVAILABLE` or `RESERVED` (set automatically) |
| pricePerHour | BigDecimal | `@NotNull`, `@PositiveOrZero` |
| building | Building | `@NotNull`, `@ManyToOne` |

### 4.5 Reservation
| Field | Type | Description |
|---|---|---|
| id | Long | Primary key, auto-generated |
| room | Room | `@ManyToOne` |
| teacher | Teacher | `@ManyToOne` |
| startTime | LocalDateTime | Set to the moment of creation |
| endTime | LocalDateTime | startTime + number of hours |
| reservationStatus | ReservationStatus | `PENDING`, `COMPLETED` or `CANCELLED` |
| totalAmount | BigDecimal | pricePerHour × hours |

### 4.6 Payment
| Field | Type | Description |
|---|---|---|
| id | Long | Primary key, auto-generated |
| reservation | Reservation | `@ManyToOne` |
| paymentDate | LocalDate | Date of the payment |
| amount | BigDecimal | Amount paid |
| paymentStatus | PaymentStatus | `PENDING` or `PAID` |

### 4.7 Relationships
| Relationship | Type |
|---|---|
| Department → Teacher | One-to-Many |
| Building → Room | One-to-Many |
| Teacher → Reservation | One-to-Many |
| Room → Reservation | One-to-Many |
| Reservation → Payment | One-to-Many |

The `@OneToMany` sides use `mappedBy` and are annotated with `@JsonIgnore` to avoid infinite loops when entities are serialized to JSON.

```
Department 1 ──── N Teacher 1 ──── N Reservation N ──── 1 Room N ──── 1 Building
                                        1
                                        │
                                        N
                                     Payment
```

## 5. Business rules
- A reservation can only be created if the room status is `AVAILABLE`.
- The number of hours must be greater than 0.
- `totalAmount = pricePerHour × hours`.
- Creating a reservation sets the room to `RESERVED`.
- Completing or cancelling a reservation sets the room back to `AVAILABLE`.
- Only a `PENDING` reservation can be extended, completed or cancelled.
- Extending a reservation adds hours to `endTime` and increases `totalAmount`.
- A payment amount must match the reservation total exactly (compared with `compareTo`, so `50.0` equals `50.00`).
- A cancelled reservation cannot be paid.
- A new room always starts as `AVAILABLE`.

## 6. Validation and error handling

### Validation
Bean Validation annotations (`@NotBlank`, `@Email`, `@Min`, `@NotNull`, `@PositiveOrZero`) are applied on the entities and triggered with `@Valid` in the controllers.

### Global exception handler
`GlobalExceptionHandler` (`@RestControllerAdvice`) converts exceptions to JSON responses:

| Situation | Exception | HTTP status |
|---|---|---|
| Resource created | – | **201 Created** |
| Resource deleted | – | **204 No Content** |
| Successful read / update | – | **200 OK** |
| Validation failed | `MethodArgumentNotValidException` | **400 Bad Request** |
| Business rule broken (room not available, wrong amount…) | `IllegalStateException` | **400 Bad Request** |
| Resource not found | `NoSuchElementException` | **404 Not Found** |
| Record linked to other data (cannot delete) | `DataIntegrityViolationException` | **409 Conflict** |

Error response examples:
```json
{ "error": "Room not found" }
```
```json
{ "name": "Department name is required" }
```

## 7. Custom queries

| # | Repository method | Business need |
|---|---|---|
| 1 | `RoomRepository.findByStatus(RoomStatus)` | List the available rooms |
| 2 | `RoomRepository.findByStatusAndCapacityGreaterThanEqual(RoomStatus, int)` | Find available rooms for a minimum number of people |
| 3 | `ReservationRepository.findByReservationStatus(ReservationStatus)` | List reservations by status (pending, completed, cancelled) |
| 4 | `ReservationRepository.findByTeacherId(Long)` | Show all reservations of one teacher |

Every repository also provides the standard CRUD operations through `JpaRepository`.

## 8. REST endpoints

Base URL: `http://localhost:8080`

### 8.1 Departments – `/api/departments`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/departments` | Create a department | 201 |
| GET | `/api/departments` | List all departments | 200 |
| GET | `/api/departments/{id}` | Get one department | 200 |
| PUT | `/api/departments/{id}` | Update a department | 200 |
| DELETE | `/api/departments/{id}` | Delete a department | 204 |

### 8.2 Buildings – `/api/buildings`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/buildings` | Create a building | 201 |
| GET | `/api/buildings` | List all buildings | 200 |
| GET | `/api/buildings/{id}` | Get one building | 200 |
| PUT | `/api/buildings/{id}` | Update a building | 200 |
| DELETE | `/api/buildings/{id}` | Delete a building | 204 |

### 8.3 Teachers – `/api/teachers`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/teachers` | Create a teacher | 201 |
| GET | `/api/teachers` | List all teachers | 200 |
| GET | `/api/teachers/{id}` | Get one teacher | 200 |
| PUT | `/api/teachers/{id}` | Update a teacher | 200 |
| DELETE | `/api/teachers/{id}` | Delete a teacher | 204 |

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

### 8.5 Reservations – `/api/reservations`
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

### 8.6 Payments – `/api/payments`
| Method | URL | Description | Success |
|---|---|---|---|
| POST | `/api/payments/{reservationId}?amount=50` | Pay a reservation | 201 |
| GET | `/api/payments` | List all payments | 200 |
| GET | `/api/payments/{id}` | Get one payment | 200 |
| PUT | `/api/payments/{id}/status?status=PENDING` | Change a payment status | 200 |
| DELETE | `/api/payments/{id}` | Delete a payment | 204 |

### 8.7 Request and response examples

**Create a department** – `POST /api/departments`
```json
{ "name": "Computer Science" }
```
Response `201 Created`:
```json
{ "id": 1, "name": "Computer Science" }
```

**Create a building** – `POST /api/buildings`
```json
{ "name": "Block A", "address": "Main Campus" }
```

**Create a teacher** – `POST /api/teachers`
```json
{ "name": "Ahmed Ben Ali", "email": "ahmed@school.com", "department": { "id": 1 } }
```

**Create a room** – `POST /api/rooms`
```json
{ "name": "A101", "capacity": 30, "pricePerHour": 10, "building": { "id": 1 } }
```
Response `201 Created`:
```json
{
  "id": 1,
  "name": "A101",
  "capacity": 30,
  "status": "AVAILABLE",
  "pricePerHour": 10,
  "building": { "id": 1, "name": "Block A", "address": "Main Campus" }
}
```

**Create a reservation** – `POST /api/reservations/1/1/3` (teacher 1, room 1, 3 hours, no body)

Response `201 Created`:
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

**Pay a reservation** – `POST /api/payments/1?amount=30` (no body)

Response `201 Created`:
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
git clone https://github.com/YOUR-USERNAME/school-reservation.git
cd school-reservation
```

### 9.3 Start the database
1. Open the **XAMPP Control Panel**.
2. Click **Start** next to **MySQL** (it turns green).

The database `school_db` is created automatically at the first start.

### 9.4 Configure `application.properties`
File: `src/main/resources/application.properties`
```properties
spring.application.name=school-reservation
spring.datasource.url=jdbc:mysql://localhost:3306/school_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
With XAMPP the root password is empty by default. Change it if yours is different.

### 9.5 Required Maven dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 9.6 Run the application
**From the IDE:** right-click `ProjetApplication.java` → *Run As* → *Spring Boot App*.

**From a terminal:**
```
./mvnw spring-boot:run
```
(on Windows: `mvnw spring-boot:run`)

Wait for the console line `Started ProjetApplication in X seconds`. The API is then available at `http://localhost:8080`.

### 9.7 Stop the application
Click the red square (Terminate) in the Console tab of the IDE, or press `Ctrl + C` in the terminal.

### 9.8 Reset the database
To restart from empty tables, change `spring.jpa.hibernate.ddl-auto=update` to `create`, run the app once, then set it back to `update`. Alternatively drop `school_db` in phpMyAdmin (`http://localhost/phpmyadmin`).

## 10. Postman tests

### 10.1 Files
- Collection: `postman/School_Reservation.postman_collection.json` (import it in Postman with *Import*)
- Screenshots: `postman/screenshots/`

### 10.2 Test scenario
All POST requests use *Body → raw → JSON*, one JSON object per request. IDs assume an empty database.

| # | Request | Body | Expected |
|---|---|---|---|
| 1 | `POST /api/departments` | `{"name":"Computer Science"}` | 201 |
| 2 | `POST /api/buildings` | `{"name":"Block A","address":"Main Campus"}` | 201 |
| 3 | `POST /api/teachers` | `{"name":"Ahmed Ben Ali","email":"ahmed@school.com","department":{"id":1}}` | 201 |
| 4 | `POST /api/rooms` | `{"name":"A101","capacity":30,"pricePerHour":10,"building":{"id":1}}` | 201 |
| 5 | `POST /api/reservations/1/1/3` | – | 201 |
| 6 | `PUT /api/reservations/1/extend?additionalHours=2` | – | 200 |
| 7 | `POST /api/payments/1?amount=50` | – | 201 |
| 8 | `GET /api/rooms/available/capacity?minCapacity=20` | – | 200 |
| 9 | `GET /api/reservations/status?reservationStatus=PENDING` | – | 200 |
| 10 | `GET /api/reservations/teacher/1` | – | 200 |
| 11 | `PUT /api/departments/1` | `{"name":"Mathematics"}` | 200 |
| 12 | `PUT /api/reservations/1/complete` | – | 200 |

### 10.3 Error tests
| # | Request | Expected |
|---|---|---|
| 13 | `POST /api/departments` with `{"name":""}` | 400 (validation) |
| 14 | `GET /api/rooms/999` | 404 |
| 15 | Create two reservations on the same room (`POST /api/reservations/1/1/2` twice) | 201 then 400 |
| 16 | `POST /api/payments/1?amount=5` | 400 (incorrect amount) |
| 17 | `DELETE /api/departments/1` while a teacher uses it | 409 |

### 10.4 Screenshots of successful tests

> Replace each file name below with your real screenshot, saved in `postman/screenshots/`.

**Create a department (201)**

![Create department](postman/screenshots/01_create_department.png)

**Create a building (201)**

![Create building](postman/screenshots/02_create_building.png)

**Create a teacher (201)**

![Create teacher](postman/screenshots/03_create_teacher.png)

**Create a room (201)**

![Create room](postman/screenshots/04_create_room.png)

**Create a reservation (201)**

![Create reservation](postman/screenshots/05_create_reservation.png)

**Extend a reservation (200)**

![Extend reservation](postman/screenshots/06_extend_reservation.png)

**Pay a reservation (201)**

![Payment](postman/screenshots/07_payment.png)

**Custom query – rooms by capacity (200)**

![Rooms by capacity](postman/screenshots/08_rooms_by_capacity.png)

**Custom query – reservations by status (200)**

![Reservations by status](postman/screenshots/09_reservations_by_status.png)

**Custom query – reservations of a teacher (200)**

![Reservations by teacher](postman/screenshots/10_reservations_by_teacher.png)

**Update a department (200)**

![Update department](postman/screenshots/11_update_department.png)

**Complete a reservation (200)**

![Complete reservation](postman/screenshots/12_complete_reservation.png)

### 10.5 Screenshots of error tests

**Validation error (400)**

![Validation error](postman/screenshots/13_error_validation.png)

**Not found (404)**

![Not found](postman/screenshots/14_error_not_found.png)

**Room already reserved (400)**

![Room not available](postman/screenshots/15_error_room_reserved.png)

**Incorrect payment amount (400)**

![Incorrect amount](postman/screenshots/16_error_wrong_amount.png)

**Delete a linked record (409)**

![Conflict](postman/screenshots/17_error_conflict.png)

## 11. Troubleshooting

| Problem | Cause and fix |
|---|---|
| `Could not send request` in Postman | The application is not running. Start it and wait for `Started ProjetApplication`. |
| `Unable to determine Dialect without JDBC metadata` | MySQL is not running or the password is wrong. Start MySQL in XAMPP and check `application.properties`. |
| `400 Bad Request` on POST | Invalid JSON. Send one JSON object per request and select *raw → JSON*. |
| Red X marks on all files in the IDE | Maven dependencies are missing. Check `pom.xml`, then *Maven → Update Project*. |
| `409 Conflict` on DELETE | Another record uses it. Delete in this order: payments, reservations, rooms, teachers, buildings, departments. |
| Port 8080 already in use | Stop the previous run of the application. |

---

*Project realized by: Mohamed Aziz Selmi, Souliman Cheeri, Oussema Laabidi, Ahmed Hammami.*
