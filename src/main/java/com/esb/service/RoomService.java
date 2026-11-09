package com.esb.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esb.model.Room;
import com.esb.model.RoomStatus;
import com.esb.repository.RoomRepository;

@Service
public class RoomService {
	@Autowired
	RoomRepository roomRepo;

	@Autowired
	BuildingService buildingService;

	public Room addRoom(Room room) {
		room.setBuilding(buildingService.findBuildingById(buildingId(room)));
		room.setStatus(RoomStatus.AVAILABLE);
		return roomRepo.save(room);
	}

	public List<Room> listRooms() {
		return roomRepo.findAll();
	}

	public List<Room> listAvailableRooms() {
		return roomRepo.findByStatus(RoomStatus.AVAILABLE);
	}

	public List<Room> listAvailableRoomsByCapacity(int capacity) {
		return roomRepo.findByStatusAndCapacityGreaterThanEqual(RoomStatus.AVAILABLE, capacity);
	}

	public Room findRoomById(Long id) {
		return roomRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Room not found"));
	}

	public Room updateRoom(Long id, Room newData) {
		Room room = findRoomById(id);
		room.setName(newData.getName());
		room.setCapacity(newData.getCapacity());
		room.setPricePerHour(newData.getPricePerHour());
		room.setBuilding(buildingService.findBuildingById(buildingId(newData)));
		return roomRepo.save(room);
	}

	public Room updateRoomStatus(Long id, RoomStatus status) {
		Room room = findRoomById(id);
		room.setStatus(status);
		return roomRepo.save(room);
	}

	public void deleteRoom(Long id) {
		roomRepo.delete(findRoomById(id));
	}

	private Long buildingId(Room room) {
		if (room.getBuilding() == null || room.getBuilding().getId() == null) {
			throw new IllegalStateException("Building id is required");
		}
		return room.getBuilding().getId();
	}
}
