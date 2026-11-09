package com.esb.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Swagger / OpenAPI configuration: the text shown at the top of http://localhost:8080/swagger-ui.html
 * and the order of the endpoint groups.
 */
@Configuration
public class OpenApiConfig {

	private static final String DESCRIPTION = """
			**StudentCaller** manages the reservation of school rooms by teachers.

			A school has **buildings** containing **rooms**. **Teachers** belong to a **department** and reserve a room
			for a number of hours. The price is computed automatically (`room price per hour × hours`) and the
			reservation is then settled with a **payment**.

			---

			### Try it in 2 minutes
			Open each group below, click an endpoint → **Try it out** (already enabled) → **Execute**.
			Every `id` you need is returned in the response of the previous step.

			| Step | Endpoint | What you will see |
			|---|---|---|
			| 1 | `POST /api/departments` | a department with its new `id` |
			| 2 | `POST /api/buildings` | a building with its new `id` |
			| 3 | `POST /api/teachers` | a teacher linked to the department |
			| 4 | `POST /api/rooms` | a room, status `AVAILABLE` |
			| 4b | `POST /api/equipments` then `PUT /api/rooms/{roomId}/equipments/{equipmentId}` | the room now lists its equipment |
			| 5 | `POST /api/reservations/{teacherId}/{roomId}/{hours}` | `totalAmount` computed, room becomes `RESERVED` |
			| 6 | `PUT /api/reservations/{id}/extend` | end time and total increased |
			| 7 | `POST /api/payments/{reservationId}?amount=…` | payment `PAID` (amount must match the total) |
			| 8 | `PUT /api/reservations/{id}/complete` | reservation `COMPLETED`, room `AVAILABLE` again |

			---

			### Business rules
			- A room can only be reserved when it is `AVAILABLE`; it becomes `RESERVED` until the reservation is completed, cancelled or deleted.
			- Hours and extra hours must be greater than 0.
			- Only a `PENDING` reservation can be completed, extended or cancelled.
			- A reservation is paid **once**, with the **exact** total; a cancelled reservation cannot be paid.
			- A paid reservation cannot be extended, and a `PAID` payment cannot go back to `PENDING`.
			- A teacher email and an equipment name are unique.
			- Equipment is installed in rooms (Many-to-Many); a room cannot get the same equipment twice.
			- A record still used by another one (e.g. a building with rooms) cannot be deleted.

			### Status codes
			| Code | Meaning |
			|---|---|
			| `200` / `201` / `204` | OK / created / deleted |
			| `400` | validation error or business rule broken — body: `{"error": "…"}` or `{"field": "message"}` |
			| `404` | record not found — body: `{"error": "… not found"}` |
			| `409` | record linked to other data, cannot be deleted |
			""";

	@Bean
	public OpenAPI studentCallerOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("StudentCaller – School Room Reservation API")
						.version("1.0")
						.description(DESCRIPTION)
						.contact(new Contact().name("DS1 Web Development – Spring Boot mini-project")))
				.servers(List.of(new Server().url("http://localhost:8080").description("Local server")))
				.tags(List.of(
						new Tag().name("1. Departments").description("Groups of teachers (create these first)"),
						new Tag().name("2. Teachers").description("Teachers who reserve rooms"),
						new Tag().name("3. Buildings").description("School buildings that contain rooms"),
						new Tag().name("4. Rooms").description("Classrooms, labs and halls, with capacity and hourly price"),
						new Tag().name("5. Equipment").description("Equipment installed in rooms (Many-to-Many with rooms)"),
						new Tag().name("6. Reservations").description("Reserve, extend, complete or cancel a room reservation"),
						new Tag().name("7. Payments").description("Settle a reservation")));
	}
}
