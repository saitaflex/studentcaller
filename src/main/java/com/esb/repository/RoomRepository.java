package com.esb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esb.model.Room;
import com.esb.model.RoomStatus;

public interface RoomRepository extends JpaRepository<Room, Long> {
	// Custom query 1: rooms by status (available rooms)
	List<Room> findByStatus(RoomStatus status);

	// Custom query 2: available rooms with at least a given capacity
	List<Room> findByStatusAndCapacityGreaterThanEqual(RoomStatus status, int capacity);
}
