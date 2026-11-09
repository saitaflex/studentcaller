package com.esb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esb.model.Reservation;
import com.esb.model.ReservationStatus;
import com.esb.model.Room;
import com.esb.model.RoomStatus;
import com.esb.model.Teacher;
import com.esb.repository.PaymentRepository;
import com.esb.repository.ReservationRepository;
import com.esb.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	@Mock
	ReservationRepository reservationRepo;

	@Mock
	RoomRepository roomRepo;

	@Mock
	PaymentRepository paymentRepo;

	@InjectMocks
	ReservationService service;

	Room room;
	Teacher teacher;

	@BeforeEach
	void setUp() {
		room = new Room();
		room.setId(1L);
		room.setStatus(RoomStatus.AVAILABLE);
		room.setPricePerHour(new BigDecimal("10.00"));
		teacher = new Teacher();
		teacher.setId(1L);
	}

	private Reservation pendingReservation() {
		Reservation r = new Reservation();
		r.setId(5L);
		r.setRoom(room);
		r.setReservationStatus(ReservationStatus.PENDING);
		r.setEndTime(LocalDateTime.now().plusHours(2));
		r.setTotalAmount(new BigDecimal("20.00"));
		room.setStatus(RoomStatus.RESERVED);
		return r;
	}

	@Test
	void createReservation_computesPriceAndLocksRoom() {
		when(reservationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

		Reservation r = service.createReservation(teacher, room, 3);

		assertEquals(new BigDecimal("30.00"), r.getTotalAmount());
		assertEquals(ReservationStatus.PENDING, r.getReservationStatus());
		assertEquals(RoomStatus.RESERVED, room.getStatus());
	}

	@Test
	void createReservation_rejectsReservedRoom() {
		room.setStatus(RoomStatus.RESERVED);
		assertThrows(IllegalStateException.class, () -> service.createReservation(teacher, room, 2));
		verify(reservationRepo, never()).save(any());
	}

	@Test
	void createReservation_rejectsZeroHours() {
		assertThrows(IllegalStateException.class, () -> service.createReservation(teacher, room, 0));
	}

	@Test
	void extendReservation_rejectedWhenAlreadyPaid() {
		when(reservationRepo.findById(5L)).thenReturn(Optional.of(pendingReservation()));
		when(paymentRepo.existsByReservationId(5L)).thenReturn(true);

		assertThrows(IllegalStateException.class, () -> service.extendReservation(5L, 1));
	}

	@Test
	void extendReservation_addsPriceWhenUnpaid() {
		when(reservationRepo.findById(5L)).thenReturn(Optional.of(pendingReservation()));
		when(paymentRepo.existsByReservationId(5L)).thenReturn(false);
		when(reservationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

		Reservation r = service.extendReservation(5L, 2);

		assertEquals(new BigDecimal("40.00"), r.getTotalAmount());
	}

	@Test
	void deletePendingReservation_freesRoom() {
		when(reservationRepo.findById(5L)).thenReturn(Optional.of(pendingReservation()));

		service.deleteReservation(5L);

		assertEquals(RoomStatus.AVAILABLE, room.getStatus());
		verify(roomRepo).save(room);
	}

	@Test
	void cancelReservation_freesRoom() {
		when(reservationRepo.findById(5L)).thenReturn(Optional.of(pendingReservation()));
		when(reservationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

		Reservation r = service.cancelReservation(5L);

		assertEquals(ReservationStatus.CANCELLED, r.getReservationStatus());
		assertEquals(RoomStatus.AVAILABLE, room.getStatus());
	}
}
