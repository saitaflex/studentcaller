package com.esb.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esb.model.Reservation;
import com.esb.model.ReservationStatus;
import com.esb.model.Room;
import com.esb.model.RoomStatus;
import com.esb.model.Teacher;
import com.esb.repository.PaymentRepository;
import com.esb.repository.ReservationRepository;
import com.esb.repository.RoomRepository;

@Service
public class ReservationService {

	@Autowired
	ReservationRepository reservationRepo;

	@Autowired
	private RoomRepository roomRepo;

	@Autowired
	private PaymentRepository paymentRepo;

	@Transactional
	public Reservation createReservation(Teacher teacher, Room room, int hours) {
		if (hours <= 0) {
			throw new IllegalStateException("Hours must be greater than 0");
		}
		if (room.getStatus() != RoomStatus.AVAILABLE) {
			throw new IllegalStateException("Room not available for reservation");
		}
		Reservation reservation = new Reservation();
		reservation.setTeacher(teacher);
		reservation.setRoom(room);
		LocalDateTime now = LocalDateTime.now();
		reservation.setStartTime(now);
		reservation.setEndTime(now.plusHours(hours));
		reservation.setReservationStatus(ReservationStatus.PENDING);
		BigDecimal totalAmount = room.getPricePerHour().multiply(BigDecimal.valueOf(hours));
		reservation.setTotalAmount(totalAmount);
		room.setStatus(RoomStatus.RESERVED);
		roomRepo.save(room);
		return reservationRepo.save(reservation);
	}

	@Transactional
	public Reservation completeReservation(Long reservationId) {
		Reservation reservation = findReservationById(reservationId);
		if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
			throw new IllegalStateException("Reservation is not active");
		}
		reservation.setReservationStatus(ReservationStatus.COMPLETED);
		freeRoom(reservation);
		return reservationRepo.save(reservation);
	}

	public Reservation findReservationById(Long reservationId) {
		return reservationRepo.findById(reservationId)
				.orElseThrow(() -> new NoSuchElementException("Reservation not found"));
	}

	public List<Reservation> listReservations() {
		return reservationRepo.findAll();
	}

	@Transactional
	public Reservation extendReservation(Long reservationId, int additionalHours) {
		Reservation reservation = findReservationById(reservationId);
		if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
			throw new IllegalStateException("Reservation not active for extension");
		}
		if (additionalHours <= 0) {
			throw new IllegalStateException("Additional hours must be greater than 0");
		}
		if (paymentRepo.existsByReservationId(reservationId)) {
			throw new IllegalStateException("Reservation is already paid and cannot be extended");
		}
		reservation.setEndTime(reservation.getEndTime().plusHours(additionalHours));
		BigDecimal additionalAmount = reservation.getRoom().getPricePerHour()
				.multiply(BigDecimal.valueOf(additionalHours));
		reservation.setTotalAmount(reservation.getTotalAmount().add(additionalAmount));
		return reservationRepo.save(reservation);
	}

	public List<Reservation> obtainReservationsByStatus(ReservationStatus status) {
		return reservationRepo.findByReservationStatus(status);
	}

	public List<Reservation> obtainReservationsByTeacher(Long teacherId) {
		return reservationRepo.findByTeacherId(teacherId);
	}

	@Transactional
	public Reservation cancelReservation(Long reservationId) {
		Reservation reservation = findReservationById(reservationId);
		if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
			throw new IllegalStateException("Reservation cannot be cancelled");
		}
		freeRoom(reservation);
		reservation.setReservationStatus(ReservationStatus.CANCELLED);
		return reservationRepo.save(reservation);
	}

	@Transactional
	public void deleteReservation(Long reservationId) {
		Reservation reservation = findReservationById(reservationId);
		// Deleting an active reservation must not leave its room locked forever
		if (reservation.getReservationStatus() == ReservationStatus.PENDING) {
			freeRoom(reservation);
		}
		reservationRepo.delete(reservation);
	}

	private void freeRoom(Reservation reservation) {
		Room room = reservation.getRoom();
		room.setStatus(RoomStatus.AVAILABLE);
		roomRepo.save(room);
	}
}
