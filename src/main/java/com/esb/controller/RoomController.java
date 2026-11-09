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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.esb.model.Room;
import com.esb.service.RoomService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
	@Autowired
	RoomService roomService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Room addRoom(@Valid @RequestBody Room room) {
		return roomService.addRoom(room);
	}

	@GetMapping
	public List<Room> listRooms() {
		return roomService.listRooms();
	}

	@GetMapping("/{id}")
	public Room getRoom(@PathVariable Long id) {
		return roomService.findRoomById(id);
	}

	@PutMapping("/{id}")
	public Room updateRoom(@PathVariable Long id, @Valid @RequestBody Room room) {
		return roomService.updateRoom(id, room);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRoom(@PathVariable Long id) {
		roomService.deleteRoom(id);
	}

	@GetMapping("/available")
	public List<Room> listAvailableRooms() {
		return roomService.listAvailableRooms();
	}

	@GetMapping("/available/capacity")
	public List<Room> listAvailableRoomsByCapacity(@RequestParam int minCapacity) {
		return roomService.listAvailableRoomsByCapacity(minCapacity);
	}
}
