package com.esb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esb.model.Building;
import com.esb.model.Equipment;
import com.esb.model.Room;
import com.esb.model.RoomStatus;
import com.esb.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@Mock
	RoomRepository roomRepo;

	@Mock
	BuildingService buildingService;

	@Mock
	EquipmentService equipmentService;

	@InjectMocks
	RoomService service;

	Room room;
	Equipment projector;

	@BeforeEach
	void setUp() {
		room = new Room();
		room.setId(1L);
		projector = new Equipment();
		projector.setId(1L);
		projector.setName("Projector");
	}

	@Test
	void addRoom_alwaysStartsAvailable() {
		Building building = new Building();
		building.setId(1L);
		room.setBuilding(building);
		room.setStatus(RoomStatus.RESERVED);
		when(buildingService.findBuildingById(1L)).thenReturn(building);
		when(roomRepo.save(any())).thenAnswer(i -> i.getArgument(0));

		assertEquals(RoomStatus.AVAILABLE, service.addRoom(room).getStatus());
	}

	@Test
	void addRoom_withoutBuildingId_isRejected() {
		room.setBuilding(new Building());
		assertThrows(IllegalStateException.class, () -> service.addRoom(room));
	}

	@Test
	void addEquipmentToRoom_addsIt() {
		when(roomRepo.findById(1L)).thenReturn(Optional.of(room));
		when(equipmentService.findEquipmentById(1L)).thenReturn(projector);
		when(roomRepo.save(any())).thenAnswer(i -> i.getArgument(0));

		Room result = service.addEquipmentToRoom(1L, 1L);

		assertTrue(result.getEquipments().contains(projector));
	}

	@Test
	void addEquipmentToRoom_twice_isRejected() {
		room.getEquipments().add(projector);
		when(roomRepo.findById(1L)).thenReturn(Optional.of(room));
		when(equipmentService.findEquipmentById(1L)).thenReturn(projector);

		assertThrows(IllegalStateException.class, () -> service.addEquipmentToRoom(1L, 1L));
	}

	@Test
	void removeEquipmentFromRoom_notInstalled_isRejected() {
		when(roomRepo.findById(1L)).thenReturn(Optional.of(room));
		when(equipmentService.findEquipmentById(1L)).thenReturn(projector);

		assertThrows(IllegalStateException.class, () -> service.removeEquipmentFromRoom(1L, 1L));
	}
}
