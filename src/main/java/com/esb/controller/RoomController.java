package com.esb.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.esb.config.ApiDocs;
import com.esb.model.Room;
import com.esb.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "4. Rooms")
public class RoomController {

	private static final String CLASSROOM = "{\"name\": \"A101\", \"capacity\": 30, \"pricePerHour\": 10, \"building\": {\"id\": 1}}";
	private static final String LAB = "{\"name\": \"Lab Info 2\", \"capacity\": 20, \"pricePerHour\": 25.5, \"building\": {\"id\": 1}}";
	private static final String VALIDATION = "{\"capacity\": \"Capacity must be at least 1\", \"name\": \"Room name is required\"}";
	private static final String NO_BUILDING = "{\"error\": \"Building id is required\"}";

	@Autowired
	RoomService roomService;

	@Operation(summary = "Create a room", description = "Only the building `id` is needed inside `building`. "
			+ "A new room is always `AVAILABLE` (the status is managed by reservations and cannot be sent).")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON, examples = {
			@ExampleObject(name = "Classroom", value = CLASSROOM), @ExampleObject(name = "Lab", value = LAB) }))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Room created, status AVAILABLE"),
			@ApiResponse(responseCode = "400", description = "Invalid field or missing building", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Validation", value = VALIDATION),
							@ExampleObject(name = "No building id", value = NO_BUILDING) })),
			@ApiResponse(responseCode = "404", description = "Building does not exist",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.BUILDING_NOT_FOUND))) })
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Room addRoom(@Valid @RequestBody Room room) {
		return roomService.addRoom(room);
	}

	@Operation(summary = "List all rooms", description = "Includes each room's building and current status.")
	@GetMapping
	public List<Room> listRooms() {
		return roomService.listRooms();
	}

	@Operation(summary = "List available rooms", description = "Rooms whose status is `AVAILABLE` (custom query 1).")
	@GetMapping("/available")
	public List<Room> listAvailableRooms() {
		return roomService.listAvailableRooms();
	}

	@Operation(summary = "Find available rooms by capacity",
			description = "Available rooms with at least `minCapacity` seats (custom query 2).")
	@GetMapping("/available/capacity")
	public List<Room> listAvailableRoomsByCapacity(
			@Parameter(description = "Minimum number of seats", example = "25") @RequestParam int minCapacity) {
		return roomService.listAvailableRoomsByCapacity(minCapacity);
	}

	@Operation(summary = "Find available rooms by equipment",
			description = "Available rooms that have the given equipment, e.g. `Projector` (custom query 6, navigates the Many-to-Many).")
	@GetMapping("/available/equipment")
	public List<Room> listAvailableRoomsByEquipment(
			@Parameter(description = "Equipment name (case-insensitive)", example = "Projector") @RequestParam String name) {
		return roomService.listAvailableRoomsByEquipment(name);
	}

	@Operation(summary = "Install an equipment in a room", description = "Adds a row in the Many-to-Many join table `room_equipment`.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Equipment added; the room is returned with its equipment list"),
			@ApiResponse(responseCode = "400", description = "Equipment already in the room",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = "{\"error\": \"Room already has this equipment\"}"))),
			@ApiResponse(responseCode = "404", description = "Room or equipment not found", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Room", value = ApiDocs.ROOM_NOT_FOUND),
							@ExampleObject(name = "Equipment", value = ApiDocs.EQUIPMENT_NOT_FOUND) })) })
	@PutMapping("/{roomId}/equipments/{equipmentId}")
	public Room addEquipmentToRoom(@Parameter(description = "Room id", example = "1") @PathVariable Long roomId,
			@Parameter(description = "Equipment id", example = "1") @PathVariable Long equipmentId) {
		return roomService.addEquipmentToRoom(roomId, equipmentId);
	}

	@Operation(summary = "Remove an equipment from a room")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Equipment removed"),
			@ApiResponse(responseCode = "400", description = "Equipment not in the room",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = "{\"error\": \"Room does not have this equipment\"}"))),
			@ApiResponse(responseCode = "404", description = "Room or equipment not found", content = @Content(mediaType = ApiDocs.JSON,
					examples = { @ExampleObject(name = "Room", value = ApiDocs.ROOM_NOT_FOUND),
							@ExampleObject(name = "Equipment", value = ApiDocs.EQUIPMENT_NOT_FOUND) })) })
	@DeleteMapping("/{roomId}/equipments/{equipmentId}")
	public Room removeEquipmentFromRoom(@Parameter(description = "Room id", example = "1") @PathVariable Long roomId,
			@Parameter(description = "Equipment id", example = "1") @PathVariable Long equipmentId) {
		return roomService.removeEquipmentFromRoom(roomId, equipmentId);
	}

	@Operation(summary = "Get one room")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Room found"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.ROOM_NOT_FOUND))) })
	@GetMapping("/{id}")
	public Room getRoom(@Parameter(description = "Room id", example = "1") @PathVariable Long id) {
		return roomService.findRoomById(id);
	}

	@Operation(summary = "Update a room", description = "Replaces name, capacity, price and building. The status is not changed.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = ApiDocs.JSON,
			examples = @ExampleObject(name = "Update", value = CLASSROOM)))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Room updated"),
			@ApiResponse(responseCode = "400", description = "Invalid field",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = VALIDATION))),
			@ApiResponse(responseCode = "404", description = "Room or building not found",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.ROOM_NOT_FOUND))) })
	@PutMapping("/{id}")
	public Room updateRoom(@Parameter(description = "Room id", example = "1") @PathVariable Long id,
			@Valid @RequestBody Room room) {
		return roomService.updateRoom(id, room);
	}

	@Operation(summary = "Delete a room", description = "Refused (409) while the room has reservations.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Room deleted"),
			@ApiResponse(responseCode = "404", description = "Unknown id",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.ROOM_NOT_FOUND))),
			@ApiResponse(responseCode = "409", description = "Room has reservations",
					content = @Content(mediaType = ApiDocs.JSON, examples = @ExampleObject(value = ApiDocs.CONFLICT))) })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRoom(@Parameter(description = "Room id", example = "1") @PathVariable Long id) {
		roomService.deleteRoom(id);
	}
}
