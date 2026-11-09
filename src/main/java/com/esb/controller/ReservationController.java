package com.esb.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.esb.config.ApiDocs;
import com.esb.model.Reservation;
import com.esb.model.ReservationStatus;
import com.esb.service.ReservationService;
import com.esb.service.RoomService;
import com.esb.service.TeacherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "5. Reservations")
public class ReservationController {

	private static final String NOT_PENDING = "{\"error\": \"Reservation is not active\"}";

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private TeacherService teacherService;

	@Autowired
	private RoomService roomService;

	@Operation(summary = "Reserve a room", description = """
			A teacher reserves an **available** room for a number of hours, starting now.

			- `totalAmount` = room `pricePerHour` × `hours`
			- the reservation starts as `PENDING`
			- the room becomes `RESERVED` until the reservation is completed, cancelled or deleted
			""")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Reservation created"),
			@ApiResponse(responseCode = "400", description = "Room already reserved or hours ≤ 0", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Room reserved", value = "{\"error\": \"Room not available for reservation\"}"),
							@ExampleObject(name = "Bad hours", value = "{\"error\": \"Hours must be greater than 0\"}") })),
			@ApiResponse(responseCode = "404", description = "Teacher or room not found", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Teacher", value = ApiDocs.TEACHER_NOT_FOUND),
							@ExampleObject(name = "Room", value = ApiDocs.ROOM_NOT_FOUND) })) })
	@PostMapping("/{teacherId}/{roomId}/{hours}")
	@ResponseStatus(HttpStatus.CREATED)
	public Reservation createReservation(@Parameter(description = "Teacher who reserves", example = "1") @PathVariable Long teacherId,
			@Parameter(description = "Room to reserve (must be AVAILABLE)", example = "1") @PathVariable Long roomId,
			@Parameter(description = "Number of hours (> 0)", example = "2") @PathVariable int hours) {
		return reservationService.createReservation(teacherService.findTeacherById(teacherId),
				roomService.findRoomById(roomId), hours);
	}

	@Operation(summary = "List all reservations", description = "Each reservation includes its room, building, teacher and department.")
	@GetMapping
	public List<Reservation> listReservations() {
		return reservationService.listReservations();
	}

	@Operation(summary = "Find reservations by status", description = "Custom query 3.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Matching reservations"),
			@ApiResponse(responseCode = "400", description = "Unknown status", content = @Content(mediaType = ApiDocs.JSON,
					examples = @ExampleObject(value = "{\"error\": \"Invalid value 'FOO' for parameter 'reservationStatus'\"}"))) })
	@GetMapping("/status")
	public List<Reservation> obtainReservationsByStatus(
			@Parameter(description = "PENDING, COMPLETED or CANCELLED", example = "PENDING") @RequestParam ReservationStatus reservationStatus) {
		return reservationService.obtainReservationsByStatus(reservationStatus);
	}

	@Operation(summary = "Find reservations of a teacher", description = "Custom query 4. Returns an empty list if the teacher has none.")
	@GetMapping("/teacher/{teacherId}")
	public List<Reservation> obtainReservationsByTeacher(
			@Parameter(description = "Teacher id", example = "1") @PathVariable Long teacherId) {
		return reservationService.obtainReservationsByTeacher(teacherId);
	}

	@Operation(summary = "Get one reservation")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Reservation found"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.RESERVATION_NOT_FOUND))) })
	@GetMapping("/{reservationId}")
	public Reservation getReservation(@Parameter(description = "Reservation id", example = "1") @PathVariable Long reservationId) {
		return reservationService.findReservationById(reservationId);
	}

	@Operation(summary = "Extend a reservation", description = """
			Adds hours to a `PENDING` reservation: the end time moves and `totalAmount` increases by
			`pricePerHour × additionalHours`. Refused once the reservation is paid.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Reservation extended"),
			@ApiResponse(responseCode = "400", description = "Not pending, already paid or hours ≤ 0", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Already paid", value = "{\"error\": \"Reservation is already paid and cannot be extended\"}"),
							@ExampleObject(name = "Not pending", value = "{\"error\": \"Reservation not active for extension\"}"),
							@ExampleObject(name = "Bad hours", value = "{\"error\": \"Additional hours must be greater than 0\"}") })),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.RESERVATION_NOT_FOUND))) })
	@PutMapping("/{reservationId}/extend")
	public Reservation extendReservation(@Parameter(description = "Reservation id", example = "1") @PathVariable Long reservationId,
			@Parameter(description = "Hours to add (> 0)", example = "1") @RequestParam int additionalHours) {
		return reservationService.extendReservation(reservationId, additionalHours);
	}

	@Operation(summary = "Complete a reservation", description = "Marks a `PENDING` reservation as `COMPLETED` and makes the room `AVAILABLE` again.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Reservation completed, room freed"),
			@ApiResponse(responseCode = "400", description = "Reservation is not pending",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = NOT_PENDING))),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.RESERVATION_NOT_FOUND))) })
	@PutMapping("/{reservationId}/complete")
	public Reservation completeReservation(@Parameter(description = "Reservation id", example = "1") @PathVariable Long reservationId) {
		return reservationService.completeReservation(reservationId);
	}

	@Operation(summary = "Cancel a reservation", description = "Marks a `PENDING` reservation as `CANCELLED` and makes the room `AVAILABLE` again. A cancelled reservation cannot be paid.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Reservation cancelled, room freed"),
			@ApiResponse(responseCode = "400", description = "Reservation is not pending",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = "{\"error\": \"Reservation cannot be cancelled\"}"))),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.RESERVATION_NOT_FOUND))) })
	@PutMapping("/{reservationId}/cancel")
	public Reservation cancelReservation(@Parameter(description = "Reservation id", example = "1") @PathVariable Long reservationId) {
		return reservationService.cancelReservation(reservationId);
	}

	@Operation(summary = "Delete a reservation", description = "Frees the room if the reservation was still `PENDING`. Refused (409) if a payment exists for it.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Reservation deleted"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.RESERVATION_NOT_FOUND))),
			@ApiResponse(responseCode = "409", description = "Reservation has a payment",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.CONFLICT))) })
	@DeleteMapping("/{reservationId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteReservation(@Parameter(description = "Reservation id", example = "1") @PathVariable Long reservationId) {
		reservationService.deleteReservation(reservationId);
	}
}
